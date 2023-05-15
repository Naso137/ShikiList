package com.naso.restapi;

import com.naso.restapi.service.RoleService;
import com.naso.restapi.model.Roles;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RestapiApplication {
    private final RoleService roleService;

    @Autowired
    public RestapiApplication(RoleService roleService) {
        this.roleService = roleService;
    }

    public static void main(String[] args) {
        SpringApplication.run(RestapiApplication.class, args);
    }

    @Bean
    InitializingBean sendDatabase() {
        return () -> {
            roleService.saveRole(new Roles(1, "USER"));
            roleService.saveRole(new Roles(2, "ADMIN"));
            roleService.saveRole(new Roles(3, "SUPERVISOR"));
            roleService.saveRole(new Roles(4, "SHIKIMORI"));
            roleService.saveRole(new Roles(5, "MYANIMELIST"));
        };
    }
}
