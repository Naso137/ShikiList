package com.naso.restapi.controllers;

import com.naso.restapi.exception.Message;
import com.naso.restapi.model.Profile;
import com.naso.restapi.security.jwt.JwtUtils;
import com.naso.restapi.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/supervisor", produces = MediaType.APPLICATION_JSON_VALUE)
public class SupervisorControl {
    private final ProfileService profileService;
    private final JwtUtils jwtUtils;

    @Autowired
    public SupervisorControl(ProfileService profileService, JwtUtils jwtUtils) {
        this.profileService = profileService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping(value = "/profiles/{word}/{previous-login}")
    public String getProfiles(@PathVariable String word,
                                  @PathVariable(name = "previous-login", required = false) String prevProfileLogin) {
        List<Profile> profileList = profileService.getUserProfiles(word, prevProfileLogin);
        return new Message<>(true, "Success", profileList).toString();
    }

    @PostMapping(value = "/admin/{login}")
    public String setAdminRole(@PathVariable String login) throws Exception {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        Profile profile = profileService.setAdminRole(login, mail);
        return new Message<>(true, "Success", profile).toString();
    }

    @DeleteMapping(value = "/admin/{login}")
    public String removeAdminRole(@PathVariable String login) throws Exception {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        Profile profile = profileService.removeAdminRole(login, mail);
        return new Message<>(true, "Success", profile).toString();
    }
}
