package com.example.user.firebase_wifidir;

public class UserInformation {

    private double Elevation;
    private double Latitude;
    private double Longitude;
    private double Location;

    public UserInformation(){

    }

    public double getElevation() {
        return Elevation;
    }

    public void setElevation(double elevation) {
        Elevation = elevation;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public double getLocation() {
        return Location;
    }

    public void setLocation(double location) {
        Location = location;
    }
}
