package com.naso.restapi.dto;

public class ProfileDto {
    private String login;
    private String password;
    private String description;
    private String avatar;
    private String shikimoriLink;
    private String myAnimeListLink;

    public ProfileDto(String login, String password, String description, String avatar, String shikimoriLink, String myAnimeListLink) {
        this.login = login;
        this.password = password;
        this.description = description;
        this.avatar = avatar;
        this.shikimoriLink = shikimoriLink;
        this.myAnimeListLink = myAnimeListLink;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
