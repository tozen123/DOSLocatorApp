package com.christianserwedevs.doslocator.Model;

public class SOSAlertInfo {
    public String documentId;
    public String childName;
    public String timestamp;
    public String location;
    public Double latitude;
    public Double longitude;

    public SOSAlertInfo(String documentId, String childName, String timestamp, String location, Double latitude, Double longitude) {
        this.documentId = documentId;
        this.childName = childName;
        this.timestamp = timestamp;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
