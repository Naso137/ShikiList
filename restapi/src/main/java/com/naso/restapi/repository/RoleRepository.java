package com.naso.restapi.repository;

import com.naso.restapi.model.Roles;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleRepository extends JpaRepository<Roles, Long> {
    Roles findByName(String name);
}
