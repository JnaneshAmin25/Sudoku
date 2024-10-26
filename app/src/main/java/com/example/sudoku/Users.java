package com.example.sudoku;

public class Users {
    public String uid;
    public String email;
    public String username;
    public String def;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public Users() {}

    public Users(String uid, String email, String username) {
        this.uid = uid;
        this.email = email;
        this.username = username;
    }

    public Users(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }
}