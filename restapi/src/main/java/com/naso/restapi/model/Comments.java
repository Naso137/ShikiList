package com.naso.restapi.model;

import com.google.gson.JsonObject;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToOne()
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_profile")
    Profile profile;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToOne()
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_anime_page")
    AnimePage animePage;

    private double episode;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToOne()
    @JoinColumn(name = "id_parent")
    private Comments parent;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "parent")
    private List<Comments> children;

    @NotNull
    @Column(length=500)
    private String text;

    private Timestamp date;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "comment", cascade=CascadeType.ALL)
    private List<NotificationsFromUser> notifications;

    public Comments() {};

    public Comments(Profile profile, AnimePage animePage, double episode,
                    @NotNull String text) {
        this.profile = profile;
        this.animePage = animePage;
        this.episode = episode;
        this.text = text;
        children = new ArrayList<>();
        notifications = new ArrayList<>();
    }

    public Comments(Profile profile, AnimePage animePage, double episode, Comments parent,
                    @NotNull String text) {
        this.profile = profile;
        this.animePage = animePage;
        this.episode = episode;
        this.parent = parent;
        this.text = text;
        children = new ArrayList<>();
        notifications = new ArrayList<>();
    }

    public void addNotification(NotificationsFromUser notification) {
        notifications.add(notification);
    }
    public void setDate(Timestamp date) {
        this.date = date;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void addAnswer(Comments comments) {
        children.add(comments);
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

    public Comments getParent() {
        return parent;
    }

    public void setParent(Comments parent) {
        this.parent = parent;
    }

    public List<Comments> getChildren() {
        return children;
    }

    public void setChildren(List<Comments> children) {
        this.children = children;
    }

    public double getEpisode() {
        return episode;
    }

    public void setEpisode(double number) {
        this.episode = number;
    }

    public String getText() {
        return text;
    }

    public List<NotificationsFromUser> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationsFromUser> notifications) {
        this.notifications = notifications;
    }

    public Timestamp getDate() {
        return date;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("profileLogin", profile.getLogin());
        jsonObject.addProperty("animePageId", animePage.getId());
        jsonObject.addProperty("number", episode);
        jsonObject.addProperty("parent", parent.id);
        jsonObject.addProperty("text", text);
        jsonObject.addProperty("date", String.valueOf(date));
        return jsonObject;
    }

    @Override
    public String toString() {
        return "{ " +
                "id: " + id +
                ", profileLogin: " + profile.getLogin() +
                ", animePageId: " + animePage.getId() +
                ", number: " + episode +
                ", parent: " + parent +
                ", text: \"" + text + "\"" +
                ", date: " + date +
                " }";
    }
}
