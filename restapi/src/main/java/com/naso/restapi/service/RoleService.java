package com.naso.restapi.service;

import com.naso.restapi.repository.RoleRepository;
import com.naso.restapi.model.Roles;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional
    public void saveRole(Roles roles) {
        roleRepository.save(roles);
    }

    @Transactional
    public Roles findByName(String name) {
        return roleRepository.findByName(name);
    }
}
