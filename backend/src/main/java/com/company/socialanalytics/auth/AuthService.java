package com.company.socialanalytics.auth;

import com.company.socialanalytics.audit.AuditService;
import com.company.socialanalytics.common.InvalidTokenException;
import com.company.socialanalytics.common.ResourceConflictException;
import com.company.socialanalytics.security.JwtService;
import com.company.socialanalytics.user.Role;
import com.company.socialanalytics.user.RoleName;
import com.company.socialanalytics.user.RoleRepository;
import com.company.socialanalytics.user.User;
import com.company.socialanalytics.user.UserMapper;
import com.company.socialanalytics.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuditService auditService;
    private final Clock clock;
    private final Duration refreshTokenTtl;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserMapper userMapper,
            AuditService auditService,
            Clock clock,
            @Value("${app.security.refresh-token-ttl}") Duration refreshTokenTtl
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.auditService = auditService;
        this.clock = clock;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResourceConflictException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ResourceConflictException("Username is already in use");
        }
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_USER, "Standard application user")));
        User user = userRepository.save(new User(
                request.email(),
                request.username(),
                request.displayName(),
                passwordEncoder.encode(request.password()),
                userRole
        ));
        auditService.record(user.getId(), "AUTH_REGISTERED", "User registered");
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> {
                    auditService.record(null, "AUTH_LOGIN_FAILED", "Login failed for unknown email");
                    return new BadCredentialsException("Invalid email or password");
                });
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            auditService.record(user.getId(), "AUTH_LOGIN_FAILED", "Login failed");
            throw new BadCredentialsException("Invalid email or password");
        }
        user.recordLogin(Instant.now(clock));
        auditService.record(user.getId(), "AUTH_LOGIN", "User logged in");
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashToken(request.refreshToken()))
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid"));
        if (!refreshToken.isUsable(Instant.now(clock))) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }
        refreshToken.revoke(Instant.now(clock));
        auditService.record(refreshToken.getUser().getId(), "AUTH_REFRESH", "Refresh token rotated");
        return issueTokens(refreshToken.getUser());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenHash(hashToken(request.refreshToken()))
                .ifPresent(token -> {
                    token.revoke(Instant.now(clock));
                    auditService.record(token.getUser().getId(), "AUTH_LOGOUT", "User logged out");
                });
    }

    private AuthResponse issueTokens(User user) {
        String refreshToken = UUID.randomUUID() + "." + UUID.randomUUID();
        refreshTokenRepository.save(new RefreshToken(
                hashToken(refreshToken),
                user,
                Instant.now(clock).plus(refreshTokenTtl)
        ));
        String accessToken = jwtService.createAccessToken(user);
        return new AuthResponse(accessToken, refreshToken, jwtService.accessTokenTtl().toSeconds(), userMapper.toResponse(user));
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 hashing is unavailable", ex);
        }
    }
}
