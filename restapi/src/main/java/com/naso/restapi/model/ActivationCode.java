package com.naso.restapi.model;

import jakarta.persistence.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Date;

@Entity
@Table(name = "activation_code")
public class ActivationCode {
    @Id
    private long userId;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @MapsId
    private User user;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Date expiryDate;

    public ActivationCode() {}

    public ActivationCode(User user, String code, Date expiryDate) {
        this.user = user;
        this.code = code;
        this.expiryDate = expiryDate;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}
