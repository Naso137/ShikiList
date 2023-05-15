package com.naso.restapi.exception;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Message<T> {
    private final Boolean isSucceeded;
    private final String message;
    private final T data;

    public Message(Boolean isSucceeded, String message, T data) {
        this.isSucceeded = isSucceeded;
        this.message = message;
        this.data = data;
    }

    @Override
    public String toString() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        return gson.toJson(this);
    }
}
