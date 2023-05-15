package com.naso.restapi.controllers;

import com.naso.restapi.dto.LibrariesDto;
import com.naso.restapi.exception.Message;
import com.naso.restapi.security.jwt.JwtUtils;
import com.naso.restapi.service.ShikimoriService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/shikimori", produces = MediaType.APPLICATION_JSON_VALUE)
public class ShikimoriControl {
    private final ShikimoriService shikimoriService;
    private final JwtUtils jwtUtils;

    @Autowired
    public ShikimoriControl(ShikimoriService shikimoriService, JwtUtils jwtUtils) {
        this.shikimoriService = shikimoriService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping(value = "/refresh-token/{token}")
    public String refreshToken(@PathVariable String token) {
        String tokenResponse = shikimoriService.refreshToken(token);
        JsonObject JSONObject = JsonParser.parseString(tokenResponse).getAsJsonObject();
        return new Message<>(true, "Success", JSONObject).toString();
    }

    @PatchMapping(value = "/add-shikimori-user-id/{token}")
    public String addShikimoriUserId(@PathVariable String token) throws Exception {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        int idShikimori = Integer.parseInt(String.valueOf(shikimoriService.addShikimoriUserId(token, mail)));
        return new Message<>(true, "Success", idShikimori).toString();
    }

    @PatchMapping(value = "/increase-series", consumes = "application/json")
    public String increaseSeries(@RequestBody String dataJson) throws Exception {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        LibrariesDto librariesDto = gson.fromJson(dataJson, LibrariesDto.class);
        String result = shikimoriService.increaseSeries(mail, librariesDto);
        JsonObject JSONObject = JsonParser.parseString(result).getAsJsonObject();
        return new Message<>(true, "Success", JSONObject).toString();
    }

    @PatchMapping(value = "/decrease-series", consumes = "application/json")
    public String decreaseSeries(@RequestBody String dataJson) throws Exception {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        LibrariesDto librariesDto = gson.fromJson(dataJson, LibrariesDto.class);
        String result = shikimoriService.decreaseSeries(mail, librariesDto);
        JsonObject JSONObject = JsonParser.parseString(result).getAsJsonObject();
        return new Message<>(true, "Success", JSONObject).toString();
    }

    @PatchMapping(value = "/rewatch", consumes = "application/json")
    public String rewatchAnime(@RequestBody String dataJson) throws Exception {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        LibrariesDto librariesDto = gson.fromJson(dataJson, LibrariesDto.class);
        String result = shikimoriService.rewatchAnime(mail, librariesDto);
        JsonObject JSONObject = JsonParser.parseString(result).getAsJsonObject();
        return new Message<>(true, "Success", JSONObject).toString();
    }

    @GetMapping(value = "/status", consumes = "application/json")
    public String getUserStatusAboutAnime(@RequestParam long animeId) throws Exception {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        String usersStatus = shikimoriService.getUserStatusAboutAnime(mail, animeId);
        return new Message<>(true, "Success", usersStatus).toString();
    }

    @PatchMapping(value = "/score", consumes = "application/json")
    public String setScore(@RequestBody String dataJson) throws Exception {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        LibrariesDto librariesDto = gson.fromJson(dataJson, LibrariesDto.class);
        String result = shikimoriService.setScore(mail, librariesDto);
        JsonObject JSONObject = JsonParser.parseString(result).getAsJsonObject();
        return new Message<>(true, "Success", JSONObject).toString();
    }

    @DeleteMapping(value = "/role")
    public String removeRole() throws Exception {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        shikimoriService.removeRole(mail);
        return new Message<>(true, "Success", "Shikimori's role has been removed successfully").toString();
    }
}
