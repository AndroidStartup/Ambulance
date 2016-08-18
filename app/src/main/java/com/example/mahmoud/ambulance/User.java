package com.example.mahmoud.ambulance;

/**
 * Created by Mahmoud on 6/27/2016.
 */
public class User {
    String email;
    String type;

    public User(String email, String type) {
        this.email = email;
        this.type = type;

    }

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public String getType() {
        return type;
    }

}
