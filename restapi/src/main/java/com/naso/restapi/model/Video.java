package com.naso.restapi.model;

import com.google.gson.JsonObject;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "video")
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToOne()
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_anime_page")
    AnimePage animePage;

    private int type;

    @NotNull
    @Column(length=150)
    private String name;

    @NotNull
    @Column(length=150)
    private String link;

    private double episode;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "video", cascade=CascadeType.ALL)
    private List<NotificationsFromAnime> notifications;

    public Video() {};

    public Video(AnimePage animePage, int type, @NotNull String name, @NotNull String link, double episode) {
        this.animePage = animePage;
        this.type = type;
        this.name = name;
        this.link = link;
        this.episode = episode;
        notifications = new ArrayList<>();
    }

    public void addNotification(NotificationsFromAnime notification) {
        notifications.add(notification);
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AnimePage getAnimePage() {
        return animePage;
    }

    public void setAnimePage(AnimePage animePage) {
        this.animePage = animePage;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public double getEpisode() {
        return episode;
    }

    public void setEpisode(double number) {
        this.episode = number;
    }

    public List<NotificationsFromAnime> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationsFromAnime> notifications) {
        this.notifications = notifications;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("animeId", animePage.getId());
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("link", link);
        jsonObject.addProperty("number", episode);
        return jsonObject;
    }

    @Override
    public String toString() {
        return "{ " +
                "id: " + id +
                ", animeId: " + animePage.getId() +
                ", type: " + type +
                ", name: \"" + name + "\"" +
                ", link: \"" + link + "\"" +
                ", number: " + episode +
                " }";
    }
}
