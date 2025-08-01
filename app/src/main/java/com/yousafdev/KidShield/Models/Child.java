package com.yousafdev.KidShield.Models;

public class Child {
    private String uid;
    private String email;

    public Child() {
        // Default constructor required for Firebase
    }

    public Child(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }
}