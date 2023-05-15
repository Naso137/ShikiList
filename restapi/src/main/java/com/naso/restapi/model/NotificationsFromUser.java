package com.naso.restapi.model;

import jakarta.persistence.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;

@Entity
@Table(name = "notificationsfromuser")
public class NotificationsFromUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToOne()
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_profile")
    private Profile profile;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToOne()
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_answered")
    private Profile profileAnswered;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToOne()
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_comment")
    private Comments comment;

    @NotNull
    private Timestamp date;

    public NotificationsFromUser() {};

    public NotificationsFromUser(Profile profile, @NotNull Timestamp date) {
        this.profile = profile;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Profile getProfileAnswered() {
        return profileAnswered;
    }

    public void setProfileAnswered(Profile profileAnswered) {
        this.profileAnswered = profileAnswered;
    }

    public Comments getComment() {
        return comment;
    }

    public void setComment(Comments comment) {
        this.comment = comment;
    }

    public @NotNull Timestamp getDate() {
        return date;
    }


    @Override
    public String toString() {
        return "{ " +
                "id: " + id +
                ", profileLogin: " + profile.getLogin() +
                ", profileAnsweredLogin: " + profile.getLogin() +
                ", commentId: " + comment.getId() +
                ", date: " + date +
                " }";
    }
}
