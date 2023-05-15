package com.naso.restapi.service;

import com.naso.restapi.dto.CommentDto;
import com.naso.restapi.model.*;
import com.naso.restapi.repository.CommentsRepository;
import com.naso.restapi.repository.NotificationsFromUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@Service
public class CommentsService {
    private final CommentsRepository commentsRepository;
    private final ProfileService profileService;
    private final AnimePageService animePageService;
    private final NotificationsFromUserRepository notificationsFromUserRepository;
    private final RoleService roleService;

    @Autowired
    public CommentsService(CommentsRepository commentsRepository, ProfileService profileService,
                            AnimePageService animePageService, NotificationsFromUserRepository notificationsFromUserRepository,
                            RoleService roleService) {
        this.commentsRepository = commentsRepository;
        this.profileService = profileService;
        this.animePageService = animePageService;
        this.notificationsFromUserRepository = notificationsFromUserRepository;
        this.roleService = roleService;
    }

    @Transactional
    public Comments findById(long id) throws IOException {
        return commentsRepository.findById(id).orElseThrow(()->
                new IOException("Not found comment with id : " + id));
    }

    @Transactional
    public List<Comments> findComments(long idAnimePage, int prevCommentId) throws IOException {
        AnimePage animePage = animePageService.findById(idAnimePage);
        Timestamp time = new Timestamp(0);
        if (prevCommentId != 0) {
            Comments comments = findById(prevCommentId);
            time = comments.getDate();
        }
        return commentsRepository.findTop5ByAnimePageAndDateAfterOrderByDateAsc(animePage, time);
    }

    @Transactional
    public long addComment(String mail, CommentDto commentDto, String idParent) throws IOException {
        User user = profileService.findUserByMail(mail);
        Profile profile = profileService.findProfileById(user.getId());
        AnimePage animePage = animePageService.findById(commentDto.idAnimePage());
        Comments comment = new Comments(profile, animePage, commentDto.episode(), commentDto.text());
        comment.setDate(new Timestamp(System.currentTimeMillis()));
        Comments newComment = commentsRepository.save(comment);

        if (idParent != null) {
            Comments parent = findById(Long.parseLong(idParent));
            commentsRepository.save(parent);

            Profile parentProfile = parent.getProfile();

            if (parentProfile != profile) {
                NotificationsFromUser notification = new NotificationsFromUser(parentProfile, new Timestamp(System.currentTimeMillis()));
                notification.setProfileAnswered(profile);
                notification.setComment(newComment);

                newComment.addNotification(notification);
                parentProfile.addNotificationFromUser(notification);
                profile.addNotificatonFromUserParent(notification);

                notificationsFromUserRepository.save(notification);
            }
            profileService.updateProfile(parentProfile);
        }

        profile.addComment(newComment);
        animePage.addComment(newComment);
        animePageService.updateAnimePage(animePage);
        profileService.updateProfile(profile);
        return newComment.getId();
    }

    @Transactional
    public void updateComment(Comments comment) {
        commentsRepository.save(comment);
    }

    @Transactional
    public Comments changeComment(String mail, long idComment, String text) throws IOException {
        Comments comments = findById(idComment);

        Profile profile = comments.getProfile();
        User user = profileService.findUserById(profile.getId());

        if (!user.getMail().equals(mail)) {
            throw new RuntimeException("Forbidden");
        }

        comments.setText(text);
        return commentsRepository.save(comments);
    }

    @Transactional
    public void deleteComment(String mail, long idComment) throws IOException {
        Comments comment = findById(idComment);
        AnimePage animePage = comment.getAnimePage();
        Profile profile = comment.getProfile();
        User user = profileService.findUserById(profile.getId());

        Roles roleAdmin = roleService.findByName("ADMIN");
        Roles roleSupervisor = roleService.findByName("SUPERVISOR");

        if ((!profile.getRoles().contains(roleAdmin)) || (!profile.getRoles().contains(roleSupervisor))) {
            if (!user.getMail().equals(mail)) {
                throw new RuntimeException("Forbidden");
            }
        }

        List<Comments> animePageComments = animePage.getComments();
        List<Comments> profileComments = profile.getComments();

        animePageComments.remove(comment);
        profileComments.remove(comment);

        animePage.setComments(animePageComments);
        profile.setComments(profileComments);

        Comments parent = comment.getParent();

        if (parent != null) {
            Profile profileParent = parent.getProfile();

            List<NotificationsFromUser> commentsNotifications = notificationsFromUserRepository.findAllByComment(comment);
            List<NotificationsFromUser> profileParentNotifications = profileParent.getNotificationsFromUserList();
            List<NotificationsFromUser> profileAnsweredNotifications = profile.getNotificationsFromUserAnsweredList();

            profileParentNotifications.removeAll(commentsNotifications);
            profileAnsweredNotifications.removeAll(commentsNotifications);

            profile.setNotificationsFromUserAnsweredList(profileAnsweredNotifications);
            profileParent.setNotificationsFromUserList(profileAnsweredNotifications);
            comment.setNotifications(null);
            notificationsFromUserRepository.deleteAllByComment(comment);
            if (notificationsFromUserRepository.findAllByComment(comment).size() > 0) {
                throw new IOException("Notifications hasn't been deleted");
            }

            profileService.updateProfile(profileParent);

            List<Comments> parentsChildren = parent.getChildren();
            parentsChildren.remove(comment);
            commentsRepository.save(parent);
            comment.setParent(null);
        }

        List<Comments> children = comment.getChildren();

        if (children.size() > 0) {
            comment.setChildren(null);
            for (Comments child: children) {
                child.setParent(null);
                commentsRepository.save(child);
            }
        }

        profileService.updateProfile(profile);
        animePageService.updateAnimePage(animePage);

        commentsRepository.save(comment);
        commentsRepository.delete(comment);
        if (commentsRepository.findById(idComment).isPresent()) {
            throw new IOException("Comment hasn't been deleted");
        }

    }
}
