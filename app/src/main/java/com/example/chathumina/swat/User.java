package com.example.chathumina.swat;

/**
 * Created by chathumina on 4/3/18.
 */

public class User {
    public String password;

    public String email;

    public String homeId;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String homeId){
        this.homeId = homeId;
    }

    public User(String password, String email) {
        this.password = password;
        this.email = email;
    }

    public User(String password, String email,String homeId) {
        this.password = password;
        this.email = email;
        this.homeId = homeId;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getHomeId() {
        return homeId;
    }
}
