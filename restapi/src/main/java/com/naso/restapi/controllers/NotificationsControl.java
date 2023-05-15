package com.naso.restapi.controllers;


import com.naso.restapi.exception.Message;
import com.naso.restapi.security.jwt.JwtUtils;
import com.naso.restapi.service.NotificationsService;
import com.naso.restapi.model.NotificationsFromAnime;
import com.naso.restapi.model.NotificationsFromUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
public class NotificationsControl {
    private final NotificationsService notificationsService;
    private final JwtUtils jwtUtils;

    @Autowired
    public NotificationsControl(NotificationsService notificationsService, JwtUtils jwtUtils) {
        this.notificationsService = notificationsService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping(value = {"/anime-notifications" ,"/anime-notifications/{previous-notification}"})
    public String getAnimeNotifications(@PathVariable(name = "previous-notification", required = false) String prevNotificationId) throws IOException {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        List<NotificationsFromAnime> notifications = notificationsService.getAnimeNotifications(mail, prevNotificationId);
        return new Message<>(true, "Success", notifications).toString();
    }

    @GetMapping(value = {"/user-notifications","/user-notifications/{previous-notification}"})
    public String getUserNotifications(@PathVariable(name = "previous-notification", required = false) String prevNotificationId) throws IOException {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        List<NotificationsFromUser> notifications = notificationsService.getUserNotifications(mail, prevNotificationId);
        return new Message<>(true, "Success", notifications).toString();
    }

    @GetMapping(value = "count-anime-notifications")
    public String getCountAnimeNotifications() throws IOException {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        int count = notificationsService.getCountAnimeNotifications(mail);
        return new Message<>(true, "Success", count).toString();
    }

    @GetMapping(value = "count-user-notifications")
    public String getCountUserNotifications() throws IOException {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        int count = notificationsService.getCountProfileNotifications(mail);
        return new Message<>(true, "Success", count).toString();
    }

    @DeleteMapping(value = {"/anime-notification", "/anime-notification/{id}"} )
    public String deleteAnimeNotification(@PathVariable long id) throws IOException {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        notificationsService.deleteAnimeNotification(id, mail);
        return new Message<>(true, "Success", "Anime notification has been deleted").toString();
    }

    @DeleteMapping(value = {"/user-notification","/user-notification/{id}"} )
    public String deleteUserNotification(@PathVariable long id) throws IOException {
        Authentication authentication = jwtUtils.getJwt();
        String mail = authentication.getName();
        notificationsService.deleteProfileNotification(id, mail);
        return new Message<>(true, "Success", "Profile notification has been deleted").toString();
    }
}
