package com.capstone.enableu.custom.config;

import com.capstone.enableu.custom.enums.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

@Configuration
public class RoleConfig {

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(
                Role.ADMIN + " > " + Role.TRAINER
                        + "\n" + Role.TRAINER + " > " + Role.TRAINEE
        );
        return roleHierarchy;
    }
}
