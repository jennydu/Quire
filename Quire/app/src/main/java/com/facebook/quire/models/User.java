package com.facebook.quire.models;

import com.google.firebase.database.DataSnapshot;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class User {
    String user_id;
    String profile_image_url;
    String screen_name;
    String full_name;
    String email;
    String password;
    String description;
    ArrayList<String> createdQuires; // arraylist of qids
    ArrayList<String> answeredQuires; // arraylist of qids
    String fb_id;

    public ArrayList<String> getCreatedQuires() {
        return createdQuires;
    }

    public void setCreatedQuires(ArrayList<String> createdQuires) {
        this.createdQuires = createdQuires;
    }

    public void setFb_id(String id) {
        this.fb_id = id;
    }

    public String getFb_id() {
        return fb_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setProfile_image_url(String profile_image_url) {
        this.profile_image_url = profile_image_url;
    }

    public void setScreen_name(String screen_name) {
        this.screen_name = screen_name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getProfile_image_url() {
        return profile_image_url;
    }

    public String getScreen_name() {
        return screen_name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getDescription() {
        return description;
    }

    public String getFull_name() {
        return full_name;
    }

    // Blank constructor for Parcelable
    public User() {

    }


    public static User fromSnapshot(DataSnapshot snapshot) {
        final User user = new User();

        user.setUser_id(snapshot.child("user_id").getValue().toString());
        user.setProfile_image_url(snapshot.child("profile_image_url").getValue().toString());
        //user.setDescription(snapshot.child("description").getValue().toString());
        user.setScreen_name(snapshot.child("screen_name").getValue().toString());
        user.setFull_name(snapshot.child("full_name").getValue().toString());
        user.setCreatedQuires(snapshot.child("quires").getValue(ArrayList.class));

        return user;
    }

    public User(String name, String email, String screen_name) {
        User u = new User();
        u.full_name = name;
        u.email = email;
        u.screen_name = screen_name;
    }

}
