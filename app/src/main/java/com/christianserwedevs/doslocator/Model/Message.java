package com.christianserwedevs.doslocator.Model;

public class Message {
    private String senderId;
    private String text;
    private String timestamp;

    public Message(String senderId, String text, String timestamp) {
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getText() {
        return text;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
