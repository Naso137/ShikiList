package com.naso.restapi.controllers;

import com.naso.restapi.dto.CommentDto;
import com.naso.restapi.exception.Message;
import com.naso.restapi.security.jwt.JwtUtils;
import com.naso.restapi.service.CommentsService;
import com.naso.restapi.model.Comments;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/comments", produces = MediaType.APPLICATION_JSON_VALUE)
public class CommentsControl {
    private final CommentsService commentsService;
    private final JwtUtils jwtUtils;

    @Autowired
    public CommentsControl(CommentsService commentsService, JwtUtils jwtUtils) {
        this.commentsService = commentsService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping(value = "/comments/{id-anime}")
    public String getCommentsByAnimeId(@PathVariable("id-anime") long idAnime,
                                       @RequestParam(required = false, defaultValue = "0") int prevCommentId) throws IOException {
        List<Comments> comments = commentsService.findComments(idAnime, prevCommentId);
        return new Message<>(true, "Success", comments).toString();
    }

    @PostMapping(value = { "/comment", "/comment/{previous-id}"}, consumes = "application/json")
    public String addComment(@RequestBody String dataJson,
                             @PathVariable(name = "previous-id", required = false) String idParent) throws IOException {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        CommentDto commentDto = gson.fromJson(dataJson, CommentDto.class);
        long id = commentsService.addComment(mail, commentDto,idParent);
        return new Message<>(true, "Success", id).toString();
    }

    @PatchMapping(value = "/comment/{id}", consumes = "application/json")
    public String changeComment(@PathVariable long id, @RequestBody String dataJson) throws IOException {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String text = gson.fromJson(dataJson, String.class);
        Comments comment = commentsService.changeComment(mail, id, text);
        return new Message<>(true, "Success", comment).toString();
    }

    @DeleteMapping(value = "/comment/{id}")
    public String deleteComment(@PathVariable long id) throws Exception {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        commentsService.deleteComment(mail, id);
        return new Message<>(true, "Success", "Comment has been deleted").toString();
    }

}
