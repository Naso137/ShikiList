package com.naso.restapi.controllers;

import com.naso.restapi.dto.LoginDto;
import com.naso.restapi.dto.RegisterDto;
import com.naso.restapi.exception.Message;
import com.naso.restapi.security.jwt.JwtResponse;
import com.naso.restapi.security.jwt.JwtUtils;
import com.naso.restapi.security.jwt.TokenRefreshResponse;
import com.naso.restapi.service.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthorizationController {
    private final ProfileService profileService;
    private final JwtUtils jwtUtils;
    private final ShikimoriService shikimoriService;
    private final MyAnimeListService myAnimeListService;

    @Autowired
    public AuthorizationController(ProfileService profileService, JwtUtils jwtUtils, ShikimoriService shikimoriService,
                                    MyAnimeListService myAnimeListService) {
        this.profileService = profileService;
        this.jwtUtils = jwtUtils;
        this.shikimoriService = shikimoriService;
        this.myAnimeListService = myAnimeListService;
    }

    @PostMapping(value = "/sign-in", consumes = "application/json")
    public String signIn(@RequestBody String dataJson) throws IOException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        LoginDto loginUser = gson.fromJson(dataJson, LoginDto.class);
        JwtResponse result = profileService.authenticateUser(loginUser.getMail(), loginUser.getPassword());
        return new Message<>(true, "Success", result.toJson()).toString();
    }

    @PostMapping(value = "/registration", consumes = "application/json")
    public String addProfile(@RequestBody String dataJson) throws IOException, MessagingException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        RegisterDto registerUser = gson.fromJson(dataJson, RegisterDto.class);
        profileService.addProfile(registerUser);
        return new Message<>(true, "Success", "").toString();
    }

    @GetMapping(value = "/activate/{code}")
    public String activate(@PathVariable String code) throws IOException {
        boolean isActivated = profileService.activateUser(code);
        if (!isActivated) {
            throw new IOException("Profile can't activate");
        }
        return new Message<>(true, "Success", true).toString();
    }

    @PostMapping(value = "/refresh-token/{token}")
    public String refreshToken(@PathVariable String token) throws IOException {
        TokenRefreshResponse result = profileService.refreshToken(token);
        return new Message<>(true, "Success", result.toJson()).toString();
    }

    @GetMapping(value = "/authorization-shikimori/{code}")
    public String authorizationShikimori(@PathVariable String code) throws IOException {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        String tokenResponse = shikimoriService.authorization(code, mail);
        JsonObject JSONObject = JsonParser.parseString(tokenResponse).getAsJsonObject();
        return new Message<>(true, "Success", JSONObject).toString();
    }

    @GetMapping(value = "/authorization-myanimelist/{code}")
    public String authorizationMyAnimeList(@PathVariable String code) throws IOException {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        String tokenResponse = myAnimeListService.authorization(code, mail);
        JsonObject JSONObject = JsonParser.parseString(tokenResponse).getAsJsonObject();
        return new Message<>(true, "Success", JSONObject).toString();
    }

    @PostMapping("/sign-out/{token}")
    public String signOut(@PathVariable String token) throws IOException {
        profileService.signOut(token);
        return new Message<>(true, "Success", "Log-out complete").toString();
    }
}
