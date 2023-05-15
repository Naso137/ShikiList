package com.naso.restapi.dto;

public class VideoDto {
    private long animeId;
    private int type;
    private String name;
    private String link;
    private double number;

    public VideoDto(long animeId, int type, String name, String link, double number) {
        this.animeId = animeId;
        this.type = type;
        this.name = name;
        this.link = link;
        this.number = number;
    }

    public VideoDto(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public long getAnimeId() {
        return animeId;
    }

    public void setAnimeId(long animeId) {
        this.animeId = animeId;
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

    public double getNumber() {
        return number;
    }

    public void setNumber(double number) {
        this.number = number;
    }
}
