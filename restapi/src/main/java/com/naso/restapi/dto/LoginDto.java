package com.naso.restapi.dto;

public class LoginDto {
    private String mail;
    private String password;

    public String getMail() {
        return mail;
    }

    public void setLoginOrMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
