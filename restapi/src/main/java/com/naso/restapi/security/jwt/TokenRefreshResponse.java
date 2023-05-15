package com.naso.restapi.security.jwt;

import com.google.gson.JsonObject;

public class TokenRefreshResponse {
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";

    public TokenRefreshResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("accessToken", accessToken);
        jsonObject.addProperty("refreshToken", refreshToken);
        jsonObject.addProperty("type", type);
        return jsonObject;
    }

    @Override
    public String toString() {
        return "{ " +
                "accessToken: \"" + accessToken + "\"" +
                ", refreshToken: \"" + refreshToken + "\"" +
                ", type: \"" + type + "\"" +
                " }";
    }
}