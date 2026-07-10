package com.company.socialanalytics.user;

import com.company.socialanalytics.audit.AuditService;
import com.company.socialanalytics.common.ResourceConflictException;
import com.company.socialanalytics.common.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final ConfiguredRoleService configuredRoleService;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            AuditService auditService,
            ConfiguredRoleService configuredRoleService
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.configuredRoleService = configuredRoleService;
    }

    @Transactional
    public UserResponse currentUser(UUID userId) {
        User user = findById(userId);
        configuredRoleService.applyConfiguredRoles(user);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findById(userId);
        userRepository.findByUsername(request.username())
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new ResourceConflictException("Username is already in use");
                });
        user.updateProfile(request.username(), request.displayName());
        auditService.record(userId, "USER_PROFILE_UPDATED", "User updated profile");
        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findById(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResourceConflictException("Current password is incorrect");
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        auditService.record(userId, "USER_PASSWORD_CHANGED", "User changed password");
    }

    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
