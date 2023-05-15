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
@Table(name = "profile")
public class Profile {
    @Id
    private long userId;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @MapsId
    private User user;

    @NotNull
    @Column(unique=true, length=20)
    private String login;
    @Column(length=150)
    private String description;

    @Column(length=100)
    private String avatar;

    @Column(name = "shikimori_link", length=50)
    private String shikimoriLink;

    @Column(name = "myanimelist_link",length=50)
    private String myAnimeListLink;

    @Column(name = "id_shikimori")
    private int idShikimori;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Roles> roles;


    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToMany()
    @JoinTable(name = "subscriptions",
            joinColumns = { @JoinColumn(name = "profile_id") },
            inverseJoinColumns = { @JoinColumn(name = "anime_page_id") }
    )
    private List<AnimePage> animePageList;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "profile", cascade=CascadeType.ALL)
    private List<NotificationsFromUser> notificationsFromUserList;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "profileAnswered", cascade=CascadeType.ALL)
    private List<NotificationsFromUser> notificationsFromUserAnsweredList;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "profile", cascade=CascadeType.ALL)
    private List<NotificationsFromAnime> notificationsFromAnimeList;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "profile", cascade=CascadeType.ALL)
    private List<Comments> comments;


    public Profile() {};

    public Profile(@NotNull String login, User user, String avatar) {
        this.login = login;
        this.user = user;
        this.avatar = avatar;
        roles = new ArrayList<>();
        animePageList = new ArrayList<>();
        notificationsFromUserList = new ArrayList<>();
        notificationsFromAnimeList = new ArrayList<>();
        notificationsFromUserAnsweredList = new ArrayList<>();
        comments = new ArrayList<>();
    }

    public void addRole(Roles role) {
        roles.add(role);
    }

    public void addAnimePage(AnimePage animePage) {
        animePageList.add(animePage);
    }

    public void addNotificationFromAnime(NotificationsFromAnime notificationsFromAnime) {
        notificationsFromAnimeList.add(notificationsFromAnime);
    }

    public void addNotificationFromUser(NotificationsFromUser notificationsFromUser) {
        notificationsFromUserList.add(notificationsFromUser);
    }

    public void addNotificatonFromUserParent(NotificationsFromUser notificationsFromUser) {
        notificationsFromUserAnsweredList.add(notificationsFromUser);
    }

    public void addComment(Comments comment) {
        comments.add(comment);
    }

    public long getId() {
        return userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public int getIdShikimori() {
        return idShikimori;
    }

    public void setIdShikimori(int idShikimori) {
        this.idShikimori = idShikimori;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getShikimoriLink() {
        return shikimoriLink;
    }

    public void setShikimoriLink(String shikimoriLink) {
        this.shikimoriLink = shikimoriLink;
    }

    public String getMyAnimeListLink() {
        return myAnimeListLink;
    }

    public void setMyAnimeListLink(String myAnimeListLink) {
        this.myAnimeListLink = myAnimeListLink;
    }

    public List<Roles> getRoles() {
        return roles;
    }

    public void setRoles(List<Roles> roles) {
        this.roles = roles;
    }

    public List<JsonObject> getAnimePageListJson() {
        List<JsonObject> jsonObjects = new ArrayList<>();

        for (AnimePage animePage: animePageList) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", animePage.getId());
            jsonObject.addProperty("originalName", animePage.getOriginalName());
            jsonObject.addProperty("russianName", animePage.getRussianName());
            jsonObject.addProperty("animeImage", animePage.getAnimeImage());
            jsonObject.addProperty("description", animePage.getDescription());
            jsonObject.addProperty("numberOfEpisodes", animePage.getNumberOfEpisodes());
            jsonObjects.add(jsonObject);
        }
        return jsonObjects;
    }

    public List<AnimePage> getAnimePageList() {

        return animePageList;
    }

    public void setAnimePageList(List<AnimePage> animePageList) {
        this.animePageList = animePageList;
    }

    public List<Comments> getComments() {
        return comments;
    }

    public void setComments(List<Comments> comments) {
        this.comments = comments;
    }

    public List<NotificationsFromUser> getNotificationsFromUserList() {
        return notificationsFromUserList;
    }

    public void setNotificationsFromUserList(List<NotificationsFromUser> notificationsFromUserList) {
        this.notificationsFromUserList = notificationsFromUserList;
    }

    public List<NotificationsFromUser> getNotificationsFromUserAnsweredList() {
        return notificationsFromUserAnsweredList;
    }

    public void setNotificationsFromUserAnsweredList(List<NotificationsFromUser> notificationsFromUserParentList) {
        this.notificationsFromUserAnsweredList = notificationsFromUserParentList;
    }

    public List<NotificationsFromAnime> getNotificationsFromAnimeList() {
        return notificationsFromAnimeList;
    }

    public void setNotificationsFromAnimeList(List<NotificationsFromAnime> notificationsFromAnimeList) {
        this.notificationsFromAnimeList = notificationsFromAnimeList;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("login", login);
        jsonObject.addProperty("description", description);
        jsonObject.addProperty("shikimoriLink", shikimoriLink);
        jsonObject.addProperty("myAnimeListLink", myAnimeListLink);
        jsonObject.addProperty("idShikimori", idShikimori);
        return jsonObject;
    }

    @Override
    public String toString() {
        return "{ " +
                "userId: " + userId +
                ", login: \"" + login + "\"" +
                " }";
    }
}
