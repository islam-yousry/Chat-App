package com.example.chatapp.Model;


import androidx.annotation.NonNull;

public class User {

    private String user_email, user_id, user_name, phone, profile_image, status,notification_key,search_name;
    private Object friends;

    public User(String user_email, String user_id, String user_name, String phone, String profile_image, String status, String search_name) {
        this.user_email = user_email;
        this.user_id = user_id;
        this.user_name = user_name;
        this.phone = phone;
        this.profile_image = profile_image;
        this.status = status;
        this.search_name = search_name;
    }

    public User() {

    }


    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getFriends() {
        return friends;
    }

    public void setFriends(Object friends) {
        this.friends = friends;
    }

    public String getNotification_key() {
        return notification_key;
    }

    public void setNotification_key(String notification_key) {
        this.notification_key = notification_key;
    }

    public String getSearch_name() {
        return search_name;
    }

    public void setSearch_name(String search_name) {
        this.search_name = search_name;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_email='" + user_email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", user_name='" + user_name + '\'' +
                ", phone='" + phone + '\'' +
                ", profile_image='" + profile_image + '\'' +
                ", status='" + status + '\'' +
                ", notification_key='" + notification_key + '\'' +
                ", search_name='" + search_name + '\'' +
                ", friends=" + friends +
                '}';
    }
}
