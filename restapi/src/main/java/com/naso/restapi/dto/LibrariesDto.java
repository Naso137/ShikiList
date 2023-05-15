package com.naso.restapi.dto;

public class LibrariesDto {
    private String token;
    private long animeId;
    private int episodesOrScore;
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getAnimeId() {
        return animeId;
    }

    public void setAnimeId(long animeId) {
        this.animeId = animeId;
    }

    public int getEpisodesOrScore() {
        return episodesOrScore;
    }

    public void setEpisodesOrScore(int episodesOrScore) {
        this.episodesOrScore = episodesOrScore;
    }
}
