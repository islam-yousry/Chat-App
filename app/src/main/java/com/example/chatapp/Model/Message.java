package com.example.chatapp.Model;

import androidx.annotation.NonNull;

public class Message {
    private String text, photo_url, sender, receiver, message_id;
    private long time;
    private boolean seen;

    public Message(String text, String photo_url, String sender, String receiver, long time, String message_id) {
        this.text = text;
        this.photo_url = photo_url;
        this.sender = sender;
        this.receiver = receiver;
        this.time = time;
        this.message_id = message_id;
    }

    public Message() {

    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getMessage_id() {
        return message_id;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @NonNull
    @Override
    public String toString() {
        return "Message{" +
                "text='" + text + '\'' +
                ", photo_url='" + photo_url + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", message_id='" + message_id + '\'' +
                ", time=" + time +
                ", seen=" + seen +
                '}';
    }
}
