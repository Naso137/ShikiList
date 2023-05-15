package com.naso.restapi.controllers;

import com.naso.restapi.dto.ProfileDto;
import com.naso.restapi.exception.Message;
import com.naso.restapi.security.jwt.JwtUtils;
import com.naso.restapi.service.ProfileService;
import com.naso.restapi.model.Profile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "/api/v1/profile", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProfileController {
    private final ProfileService profileService;
    private final JwtUtils jwtUtils;

    @Autowired
    public ProfileController(ProfileService profileService, JwtUtils jwtUtils) {
        this.profileService = profileService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping(value = "/profile/{login}")
    public String getProfile(@PathVariable String login) {
        JsonObject result = profileService.getProfile(login);
        return new Message<>(true, "Success", result).toString();
    }

    @GetMapping(value = "/is-correct-password/{password}")
    public String isCorrectPassword(@PathVariable String password) throws IOException {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        boolean isCorrect = profileService.isCorrectPassword(mail, password);
        return new Message<>(true, "Success", isCorrect).toString();
    }

    @PatchMapping(value = "/profile", consumes = "application/json")
    public String changeProfile(@RequestBody String dataJson) throws IOException {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        ProfileDto profileDto = gson.fromJson(dataJson, ProfileDto.class);
        JsonObject result = profileService.changeProfile(mail, profileDto);
        return new Message<>(true, "Success", result).toString();
    }

    @PostMapping(value = "/subscribe/{anime-id}")
    public String subscribe(@PathVariable(name = "anime-id") int animeId) throws IOException {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        Profile profile = profileService.addAnimePage(mail, animeId);
        return new Message<>(true, "Success", profile.getAnimePageListJson()).toString();
    }

    @DeleteMapping(value = "/unsubscribe/{anime-id}")
    public String unsubscribe(@PathVariable(name = "anime-id") int animeId) throws IOException {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        profileService.unsubscribeFromAnimePage(mail, animeId);
        return new Message<>(true, "Success", 1).toString();
    }

}

