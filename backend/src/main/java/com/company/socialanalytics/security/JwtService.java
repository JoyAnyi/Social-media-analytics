package com.company.socialanalytics.security;

import com.company.socialanalytics.user.Role;
import com.company.socialanalytics.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final Duration accessTokenTtl;
    private final Clock clock;

    public JwtService(
            @Value("${app.security.jwt-secret}") String secret,
            @Value("${app.security.access-token-ttl}") Duration accessTokenTtl,
            Clock clock
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = accessTokenTtl;
        this.clock = clock;
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now(clock);
        Set<String> roles = user.getRoles().stream().map(Role::getName).map(Enum::name).collect(Collectors.toSet());
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(secretKey)
                .compact();
    }

    public CurrentUser parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);
            Collection<?> roleClaims = claims.get("roles", Collection.class);
            Set<String> roles = roleClaims == null
                    ? Set.of()
                    : roleClaims.stream().map(String::valueOf).collect(Collectors.toUnmodifiableSet());
            return new CurrentUser(userId, email, roles);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtAuthenticationException("JWT is invalid or expired");
        }
    }

    public Duration accessTokenTtl() {
        return accessTokenTtl;
    }
}
