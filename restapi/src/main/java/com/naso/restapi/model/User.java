package com.naso.restapi.model;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Column(unique=true, length=50)
    private String mail;

    @NotNull
    @Column(length=200)
    private String password;

    private long invalidLogIn;

    public User() {
    }
    public User(@NotNull String mail, @NotNull String password, long invalidLogIn) {
        this.mail = mail;
        this.password = password;
        this.invalidLogIn = invalidLogIn;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public @NotNull String getMail() {
        return mail;
    }

    public void setMail(@NotNull String mail) {
        this.mail = mail;
    }

    public @NotNull String getPassword() {
        return password;
    }

    public void setPassword(@NotNull String password) {
        this.password = password;
    }

    public long getInvalidLogIn() {
        return invalidLogIn;
    }

    public void setInvalidLogIn(long invalidLogin) {
        this.invalidLogIn = invalidLogin;
    }

    @Override
    public String toString() {
        return "{ " +
                "mail: \"" + mail + "\"" +
                ", password: \"" + password + "\""  +
                " }";
    }
}
