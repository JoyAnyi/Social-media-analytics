package com.company.socialanalytics.user;

import com.company.socialanalytics.audit.AuditService;
import com.company.socialanalytics.security.SecurityProperties;
import org.springframework.stereotype.Service;

@Service
public class ConfiguredRoleService {
    private final RoleRepository roleRepository;
    private final SecurityProperties securityProperties;
    private final AuditService auditService;

    public ConfiguredRoleService(
            RoleRepository roleRepository,
            SecurityProperties securityProperties,
            AuditService auditService
    ) {
        this.roleRepository = roleRepository;
        this.securityProperties = securityProperties;
        this.auditService = auditService;
    }

    public Role role(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName, roleDescription(roleName))));
    }

    public void applyConfiguredRoles(User user) {
        user.getRoles().add(role(RoleName.ROLE_USER));
        boolean configuredAdmin = securityProperties.getAdminEmails().stream()
                .anyMatch(email -> email.equalsIgnoreCase(user.getEmail()));
        boolean currentlyAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN);
        if (configuredAdmin && !currentlyAdmin) {
            user.getRoles().add(role(RoleName.ROLE_ADMIN));
            auditService.record(user.getId(), "AUTH_ROLE_GRANTED", "Configured admin role granted");
            return;
        }
        if (!configuredAdmin && currentlyAdmin) {
            user.getRoles().removeIf(role -> role.getName() == RoleName.ROLE_ADMIN);
            auditService.record(user.getId(), "AUTH_ROLE_REVOKED", "Configured admin role revoked");
        }
    }

    private String roleDescription(RoleName roleName) {
        return roleName == RoleName.ROLE_ADMIN
                ? "Administrator with audit and user management privileges"
                : "Standard application user";
    }
}
