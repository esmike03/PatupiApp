package com.patupiapp;

public class Booking {
    private String date;
    private String userEmail;
    private String placeName;
    private String userName;

    public Booking() {
        // Default constructor for Firebase
    }

    public Booking(String date, String userEmail, String placeName, String userName) {
        this.date = date;
        this.userEmail = userEmail;
        this.placeName = placeName;
        this.userName = userName;
    }

    public String getDate() {
        return date;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return null;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

