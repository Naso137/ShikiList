package com.naso.restapi.security.jwt;

import com.google.gson.JsonObject;

public class JwtResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private long profileId;

    public JwtResponse(String accessToken, String refreshToken, long profileId) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.profileId = profileId;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("accessToken", token);
        jsonObject.addProperty("refreshToken", refreshToken);
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("profileId", profileId);
        return jsonObject;
    }

    @Override
    public String toString() {
        return "{ " +
                "accessToken: \"" + token + "\"" +
                ", refreshToken: \"" + refreshToken + "\"" +
                ", type: \"" + type + "\"" +
                ", profileId: " + profileId +
                " }";
    }
}