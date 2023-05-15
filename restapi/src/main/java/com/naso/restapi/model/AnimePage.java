package com.naso.restapi.model;

import com.google.gson.JsonObject;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import org.hibernate.annotations.*;
import org.jetbrains.annotations.NotNull;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "anime_page")
public class AnimePage {
    @Id
    private long id;

    @NotNull
    @Column(name="original_name", length=150)
    private String originalName;

    @NotNull
    @Column(name="russian_name", length=150)
    private String russianName;

    @NotNull
    @Column(name="anime_image", length=500)
    private String animeImage;

    @NotNull
    @Column(name="description", length=5000)
    private String description;

    @NotNull
    @Column(name="numer_of_episodes")
    private double numberOfEpisodes;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToMany(mappedBy = "animePageList")
    private List<Profile> profiles;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "animePage", cascade=CascadeType.ALL)
    private List<Video> videoList;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "animePage", cascade=CascadeType.ALL)
    private List<NotificationsFromAnime> notifications;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "animePage", cascade=CascadeType.ALL)
    private List<Comments> comments;

    public AnimePage() {};

    public AnimePage(long id, @NotNull String originalName, @NotNull String russianName,
                     @NotNull String animeImage, @NotNull String description, @NotNull int numberOfEpisodes) {
        this.id = id;
        this.originalName = originalName;
        this.russianName = russianName;
        this.animeImage = animeImage;
        this.description = description;
        this.numberOfEpisodes = numberOfEpisodes;
        profiles = new ArrayList<>();
        videoList = new ArrayList<>();
        notifications = new ArrayList<>();
        comments = new ArrayList<>();
    }

    public AnimePage(@NotNull String originalName, @NotNull String russianName,
                     @NotNull String animeImage, @NotNull String description, @NotNull int numberOfEpisodes) {
        this.originalName = originalName;
        this.russianName = russianName;
        this.animeImage = animeImage;
        this.description = description;
        this.numberOfEpisodes = numberOfEpisodes;
        profiles = new ArrayList<>();
        videoList = new ArrayList<>();
        notifications = new ArrayList<>();
        comments = new ArrayList<>();
    }

    public void addProfile(Profile profile) {
        profiles.add(profile);
    }

    public void addVideo(Video video) {
        video.setAnimePage(this);
        videoList.add(video);
    }

    public void addNotification(NotificationsFromAnime notification) {
        notification.setAnimePage(this);
        notifications.add(notification);
    }

    public void addComment(Comments comment) {
        comment.setAnimePage(this);
        comments.add(comment);
    }

    public @NotNull String getDescription() {
        return description;
    }

    public void setDescription(@NotNull String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public @NotNull String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(@NotNull String originalName) {
        this.originalName = originalName;
    }

    public @NotNull String getRussianName() {
        return russianName;
    }

    public void setRussianName(@NotNull String russianName) {
        this.russianName = russianName;
    }

    public @NotNull String getAnimeImage() {
        return animeImage;
    }

    public void setAnimeImage(@NotNull String animeImage) {
        this.animeImage = animeImage;
    }

    public double getNumberOfEpisodes() {
        return numberOfEpisodes;
    }

    public void setNumberOfEpisodes(double numberOfEpisodes) {
        this.numberOfEpisodes = numberOfEpisodes;
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    public List<Video> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<Video> videoList) {
        this.videoList = videoList;
    }

    public List<NotificationsFromAnime> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationsFromAnime> notifications) {
        this.notifications = notifications;
    }

    public List<Comments> getComments() {
        return comments;
    }

    public void setComments(List<Comments> comments) {
        this.comments = comments;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("originalName", originalName);
        jsonObject.addProperty("russianName", russianName);
        jsonObject.addProperty("animeImage", animeImage);
        jsonObject.addProperty("description", description);
        jsonObject.addProperty("numberOfEpisodes", numberOfEpisodes);
        return jsonObject;
    }

    @Override
    public String toString() {
        return "{ " +
                "id: " + id +
                ", originalName: \"" + originalName + "\"" +
                ", russianName: \"" + russianName + "\"" +
                ", animeImage: \"" + animeImage + "\"" +
                ", description: \"" + description + "\"" +
                ", numberOfEpisodes: " + numberOfEpisodes +
                " }";
    }
}
