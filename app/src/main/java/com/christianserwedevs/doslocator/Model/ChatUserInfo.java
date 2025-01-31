package com.christianserwedevs.doslocator.Model;

public class ChatUserInfo {
    private String fullName;
    private String email;
    private String userId;
    private String userType;

    public ChatUserInfo(String fullName, String email, String userId, String userType) {
        this.fullName = fullName;
        this.email = email;
        this.userId = userId;
        this.userType = userType;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserType() {
        return userType;
    }
}
