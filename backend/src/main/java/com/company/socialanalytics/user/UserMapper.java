package com.company.socialanalytics.user;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getDisplayName(),
                mapRoles(user.getRoles()),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private Set<RoleName> mapRoles(Set<Role> roles) {
        return roles.stream().map(Role::getName).collect(Collectors.toUnmodifiableSet());
    }
}
