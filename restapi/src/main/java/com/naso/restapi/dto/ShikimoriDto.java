package com.naso.restapi.dto;

public class ShikimoriDto {
    private int user_id;
    private long target_id;
    private String target_type;
    private String status;
    private int episodes;
    private int rewatches;
    private int score;

    public ShikimoriDto(int user_id, long target_id, String target_type) {
        this.user_id = user_id;
        this.target_id = target_id;
        this.target_type = target_type;
    }

    public ShikimoriDto(int user_id, long target_id, String target_type, int episodes) {
        this.user_id = user_id;
        this.target_id = target_id;
        this.target_type = target_type;
        this.episodes = episodes;
    }

    public ShikimoriDto(int user_id, long target_id, String target_type, String status, int rewatches) {
        this.user_id = user_id;
        this.target_id = target_id;
        this.target_type = target_type;
        this.status = status;
        this.rewatches = rewatches;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public long getTarget_id() {
        return target_id;
    }

    public void setTarget_id(int target_id) {
        this.target_id = target_id;
    }

    public String getTarget_type() {
        return target_type;
    }

    public void setTarget_type(String target_type) {
        this.target_type = target_type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getEpisodes() {
        return episodes;
    }

    public void setEpisodes(int episodes) {
        this.episodes = episodes;
    }

    public int getRewatches() {
        return rewatches;
    }

    public void setRewatches(int rewatches) {
        this.rewatches = rewatches;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
