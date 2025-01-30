package com.christianserwedevs.doslocator;

public class ChildInfo {
    private String userId;
    private String fullName;
    private Double latitude;
    private Double longitude;

    public ChildInfo(String userId, String fullName, Double latitude, Double longitude) {
        this.userId = userId;
        this.fullName = fullName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
