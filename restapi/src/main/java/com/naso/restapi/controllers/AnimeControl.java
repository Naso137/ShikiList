package com.naso.restapi.controllers;

import com.naso.restapi.exception.Message;
import com.naso.restapi.service.AnimePageService;
import com.naso.restapi.service.VideoService;
import com.naso.restapi.model.AnimePage;
import com.naso.restapi.model.Video;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/anime", produces = MediaType.APPLICATION_JSON_VALUE)
public class AnimeControl {
    private final AnimePageService animePageService;
    private final VideoService videoService;

    @Autowired
    public AnimeControl(AnimePageService animePageService, VideoService videoService) {
        this.animePageService = animePageService;
        this.videoService = videoService;
    }

    @GetMapping(value = "/anime-page/{id}")
    public String getAnimePage(@PathVariable long id) {
        AnimePage animePage = animePageService.findById(id);
        return new Message<>(true, "Success", animePage.toJson()).toString();
    }

    @GetMapping(value = "/full-anime-page/{id}")
    public String getFullAnimePage(@PathVariable long id) {
        JsonObject animePage = animePageService.getFullAnimePage(id);
        return new Message<>(true, "Success", animePage).toString();
    }

    @GetMapping(value = "/anime-page/{word}/{previous-name}")
    public String getAnimePagesByLetter(@PathVariable String word,
                                        @PathVariable(name = "previous-name", required = false) String prevRussianAnimeName) throws IOException {
        List<AnimePage> animePages = animePageService.findByNameOrRussianName(word, prevRussianAnimeName);
        return new Message<>(true, "Success", animePages).toString();
    }

    @GetMapping(value = "/video/{id}")
    public String getVideo(@PathVariable long id) throws IOException {
        Video video = videoService.findById(id);
        return new Message<>(true, "Success", video.toJson()).toString();
    }
}
