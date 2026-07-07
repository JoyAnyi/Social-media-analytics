package com.company.socialanalytics.config;

import com.company.socialanalytics.user.Role;
import com.company.socialanalytics.user.RoleName;
import com.company.socialanalytics.user.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements ApplicationRunner {
    private final RoleRepository roleRepository;

    public DataSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed(RoleName.ROLE_USER, "Standard application user");
        seed(RoleName.ROLE_ADMIN, "Administrator with audit and user management privileges");
    }

    private void seed(RoleName roleName, String description) {
        roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(new Role(roleName, description)));
    }
}
