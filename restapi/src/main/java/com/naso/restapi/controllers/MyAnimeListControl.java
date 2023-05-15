package com.naso.restapi.controllers;

import com.naso.restapi.dto.LibrariesDto;
import com.naso.restapi.exception.Message;
import com.naso.restapi.security.jwt.JwtUtils;
import com.naso.restapi.service.MyAnimeListService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/myanimelist", produces = MediaType.APPLICATION_JSON_VALUE)
public class MyAnimeListControl {
    private final MyAnimeListService myAnimeListService;
    private final JwtUtils jwtUtils;

    @Autowired
    public MyAnimeListControl(MyAnimeListService myAnimeListService, JwtUtils jwtUtils) {
        this.myAnimeListService = myAnimeListService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping(value = "/refresh-token/{token}")
    public String refreshToken(@PathVariable String token) {
        String tokenResponse = myAnimeListService.refreshToken(token);
        JsonObject JSONObject = JsonParser.parseString(tokenResponse).getAsJsonObject();
        return new Message<>(true, "Success", JSONObject).toString();
    }

    @PatchMapping(value = "/increase-series", consumes = "application/json")
    public String increaseSeries(@RequestBody String dataJson) throws Exception {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        LibrariesDto librariesDto = gson.fromJson(dataJson, LibrariesDto.class);
        String result = myAnimeListService.increaseSeries(librariesDto);
        JsonObject JSONObject = JsonParser.parseString(result).getAsJsonObject();
        return new Message<>(true, "Success", JSONObject).toString();
    }

    @PatchMapping(value = "/decrease-series", consumes = "application/json")
    public String decreaseSeries(@RequestBody String dataJson) throws Exception {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        LibrariesDto librariesDto = gson.fromJson(dataJson, LibrariesDto.class);
        String result = myAnimeListService.decreaseSeries(librariesDto);
        JsonObject JSONObject = JsonParser.parseString(result).getAsJsonObject();
        return new Message<>(true, "Success", JSONObject).toString();
    }

    @PatchMapping(value = "/rewatch", consumes = "application/json")
    public String rewatchAnime(@RequestBody String dataJson) throws Exception {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        LibrariesDto librariesDto = gson.fromJson(dataJson, LibrariesDto.class);
        String result = myAnimeListService.rewatchAnime(librariesDto);
        JsonObject JSONObject = JsonParser.parseString(result).getAsJsonObject();
        return new Message<>(true, "Success", JSONObject).toString();
    }

    @GetMapping(value = "/status")
    public String getUserStatusAboutAnime(@RequestParam long animeId, @RequestParam String token) throws Exception {
        String usersStatus = myAnimeListService.getUserStatusAboutAnime(animeId, token);
        return new Message<>(true, "Success", usersStatus).toString();
    }

    @PatchMapping(value = "/score", consumes = "application/json")
    public String setScore(@RequestBody String dataJson) throws Exception {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        LibrariesDto librariesDto = gson.fromJson(dataJson, LibrariesDto.class);
        String result = myAnimeListService.setScore(librariesDto);
        JsonObject JSONObject = JsonParser.parseString(result).getAsJsonObject();
        return new Message<>(true, "Success", JSONObject).toString();
    }

    @DeleteMapping(value = "/role")
    public String removeRole() throws Exception {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        myAnimeListService.removeRole(mail);
        return new Message<>(true, "Success", "MyAnimeList's role has been removed successfully").toString();
    }
}
