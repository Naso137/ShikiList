package com.naso.restapi.service;

import com.naso.restapi.repository.NotificationsFromAnimeRepository;
import com.naso.restapi.repository.NotificationsFromUserRepository;
import com.naso.restapi.model.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@Service
public class NotificationsService {
    private final NotificationsFromAnimeRepository notificationsFromAnimeRepository;
    private final NotificationsFromUserRepository notificationsFromUserRepository;
    private final ProfileService profileService;
    private final AnimePageService animePageService;
    private final VideoService videoService;
    private final CommentsService commentsService;

    @Autowired
    public NotificationsService(NotificationsFromAnimeRepository notificationsFromAnimeRepository, NotificationsFromUserRepository notificationsFromUserRepository,
                                 ProfileService profileService, AnimePageService animePageService, VideoService videoService,
                                 CommentsService commentsService) {
        this.notificationsFromAnimeRepository = notificationsFromAnimeRepository;
        this.notificationsFromUserRepository = notificationsFromUserRepository;
        this.profileService = profileService;
        this.animePageService = animePageService;
        this.videoService = videoService;
        this.commentsService = commentsService;
    }

    @Transactional
    public List<NotificationsFromAnime> getAnimeNotifications(String mail, String prevNotificationId) throws IOException {
        User user = profileService.findUserByMail(mail);
        Profile profile = profileService.findProfileById(user.getId());
        Timestamp time = new Timestamp(0);
        if (prevNotificationId != null) {
            NotificationsFromAnime notifications = notificationsFromAnimeRepository.findById(Long.parseLong(prevNotificationId)).
                    orElseThrow(()-> new IOException("Such notification doesn't exists"));
            time = notifications.getDate();
            return notificationsFromAnimeRepository.findTop5ByProfileAndDateBeforeOrderByDateDesc(profile, time);
        }
        return notificationsFromAnimeRepository.findTop5ByProfileAndDateAfterOrderByDateDesc(profile, time);
    }

    @Transactional
    public List<NotificationsFromUser> getUserNotifications(String mail, String prevNotificationId) throws IOException {
        User user = profileService.findUserByMail(mail);
        Profile profile = profileService.findProfileById(user.getId());
        Timestamp time = new Timestamp(0);
        if (prevNotificationId != null) {
            NotificationsFromUser notifications = notificationsFromUserRepository.findById(Long.parseLong(prevNotificationId)).
                    orElseThrow(()-> new IOException("Such notification doesn't exists"));
            time = notifications.getDate();
            return notificationsFromUserRepository.findTop5ByProfileAndDateBeforeOrderByDateDesc(profile, time);
        }
        return notificationsFromUserRepository.findTop5ByProfileAndDateAfterOrderByDateDesc(profile, time);
    }

    @Transactional
    public int getCountAnimeNotifications(String mail) throws IOException {
        User user = profileService.findUserByMail(mail);
        Profile profile = profileService.findProfileById(user.getId());
        return notificationsFromAnimeRepository.countAllByProfile(profile);
    }

    @Transactional
    public int getCountProfileNotifications(String mail) throws IOException {
        User user = profileService.findUserByMail(mail);
        Profile profile = profileService.findProfileById(user.getId());
        return notificationsFromUserRepository.countAllByProfile(profile);
    }

    @Transactional
    public void deleteAnimeNotification(long notificationId, String mail) throws IOException {
        NotificationsFromAnime notificationFromAnime = notificationsFromAnimeRepository.findById(notificationId).orElseThrow(() ->
                new IOException("Anime notification with this id doesn't exist"));

        Profile profile = notificationFromAnime.getProfile();
        User user = profileService.findUserById(profile.getId());
        if (!user.getMail().equals(mail)) {
            throw new IOException("Forbidden");
        }
        Video video = notificationFromAnime.getVideo();
        AnimePage animePage = notificationFromAnime.getAnimePage();

        List<NotificationsFromAnime> profilesNotifications = profile.getNotificationsFromAnimeList();
        List<NotificationsFromAnime> videosNotifications = video.getNotifications();
        List<NotificationsFromAnime> animePagesNotifications = animePage.getNotifications();

        profilesNotifications.remove(notificationFromAnime);
        videosNotifications.remove(notificationFromAnime);
        animePagesNotifications.remove(notificationFromAnime);

        profile.setNotificationsFromAnimeList(profilesNotifications);
        video.setNotifications(videosNotifications);
        animePage.setNotifications(animePagesNotifications);

        profileService.updateProfile(profile);
        animePageService.updateAnimePage(animePage);
        videoService.updateVideo(video);

        notificationsFromAnimeRepository.delete(notificationFromAnime);
        if (notificationsFromAnimeRepository.findById(notificationId).isPresent()) {
            throw new IOException("Anime notification hasn't been deleted");
        }
    }

    @Transactional
    public void deleteProfileNotification(long notificationId, String mail) throws IOException {
        NotificationsFromUser notificationFromUser = notificationsFromUserRepository.findById(notificationId).orElseThrow(() ->
                new IOException("User notification with this id doesn't exist"));

        Profile profile = notificationFromUser.getProfile();
        User user = profileService.findUserById(profile.getId());
        if (!user.getMail().equals(mail)) {
            throw new IOException("Forbidden");
        }
        Profile profileAnswered = notificationFromUser.getProfileAnswered();
        Comments comment = notificationFromUser.getComment();

        List<NotificationsFromUser> profilesNotifications = profile.getNotificationsFromUserList();
        List<NotificationsFromUser> profileAnsweredNotifications = profileAnswered.getNotificationsFromUserAnsweredList();
        List<NotificationsFromUser> commentNotifications = comment.getNotifications();

        profilesNotifications.remove(notificationFromUser);
        profileAnsweredNotifications.remove(notificationFromUser);
        commentNotifications.remove(notificationFromUser);

        profile.setNotificationsFromUserList(profilesNotifications);
        profileAnswered.setNotificationsFromUserAnsweredList(profileAnsweredNotifications);
        comment.setNotifications(commentNotifications);

        profileService.updateProfile(profile);
        profileService.updateProfile(profileAnswered);
        commentsService.updateComment(comment);

        notificationsFromUserRepository.delete(notificationFromUser);
        if (notificationsFromUserRepository.findById(notificationId).isPresent()) {
            throw new IOException("User notification hasn't been deleted");
        }
    }
}
