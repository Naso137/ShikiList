package com.naso.restapi.dto;

public class RegisterDto {
    private String login;
    private String mail;
    private String password;

    public RegisterDto() {
    }

    public RegisterDto(String login, String mail, String password) {
        this.login = login;
        this.mail = mail;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
