package com.patupiapp;

public class User {

    public String name, email, profile, id, business;

    public User() {
        // Default constructor required for Firebase deserialization
    }
    public User(String email, String id, String fullName, String profile, String business){
        this.email = email;
        this.id = id;
        this.name = fullName;
        this.profile = profile;
        this.business = business;
    }

    public String getUserName() {
        return name;
    }
}
