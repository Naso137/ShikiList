package com.naso.restapi.model;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "notificationsfromanime")
public class NotificationsFromAnime {
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
    @JoinColumn(name = "id_anime_page")
    private AnimePage animePage;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToOne()
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_video")
    private Video video;

    @NotNull
    private Timestamp date;

    public NotificationsFromAnime() {};

    public NotificationsFromAnime(Profile profile, @NotNull Timestamp date) {
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

    public AnimePage getAnimePage() {
        return animePage;
    }

    public void setAnimePage(AnimePage animePage) {
        this.animePage = animePage;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public @NotNull Timestamp getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "{ " +
                "id: " + id +
                ", profileLogin: " + profile.getLogin() +
                ", animePageName: " + animePage.getOriginalName() +
                ", videoId: " + video.getId() +
                ", episode: " + video.getEpisode() +
                ", date: " + date +
                " }";
    }
}
