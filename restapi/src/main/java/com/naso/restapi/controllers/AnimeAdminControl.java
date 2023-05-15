package com.naso.restapi.controllers;

import com.naso.restapi.dto.VideoDto;
import com.naso.restapi.exception.Message;
import com.naso.restapi.service.AnimePageService;
import com.naso.restapi.service.VideoService;
import com.naso.restapi.model.AnimePage;
import com.naso.restapi.model.Video;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "/api/v1/anime-admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AnimeAdminControl {
    private final AnimePageService animePageService;
    private final VideoService videoService;

    @Autowired
    public AnimeAdminControl(AnimePageService animePageService, VideoService videoService) {
        this.animePageService = animePageService;
        this.videoService = videoService;
    }

    @PostMapping(value ="/anime-page/{id}")
    public String addAnimePage(@PathVariable long id) throws IOException {
        JsonObject jsonObject = animePageService.addAnimePage(id);
        return new Message<>(true, "Success", jsonObject).toString();
    }

    @PatchMapping(value = "/anime-page/{id}", consumes = "application/json")
    public String changeAnimePage(@PathVariable long id, @RequestBody String dataJson) throws IOException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        AnimePage animePage = gson.fromJson(dataJson, AnimePage.class);
        AnimePage newAnimePage = animePageService.changeAnimePage(id, animePage);
        return new Message<>(true, "Success", newAnimePage.toJson()).toString();
    }

    @DeleteMapping(value = "/anime-page/{id}")
    public String deleteAnimePage(@PathVariable long id) throws IOException {
        animePageService.deleteAnimePage(id);
        return new Message<>(true, "Success", "Anime page has been deleted").toString();
    }

    @PostMapping(value ="/video", consumes = "application/json")
    public String addVideo(@RequestBody String dataJson) throws IOException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        VideoDto videoDto = gson.fromJson(dataJson, VideoDto.class);
        Video video = videoService.addVideo(videoDto);
        return new Message<>(true, "Success", video.toJson()).toString();
    }

    @PatchMapping(value ="/video/{id}", consumes = "application/json")
    public String changeVideo(@PathVariable long id, @RequestBody String dataJson) throws IOException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        VideoDto videoDto = gson.fromJson(dataJson, VideoDto.class);
        Video video = videoService.changeVideo(id, videoDto);
        return new Message<>(true, "Success", video.toJson()).toString();
    }

    @DeleteMapping(value ="/video/{id}")
    public String deleteVideo(@PathVariable long id) throws IOException {
        videoService.deleteVideo(id);
        return new Message<>(true, "Success", "Video has been deleted").toString();
    }

}
