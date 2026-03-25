package com.travelplanner.places.dto;


public class PlacesSearchRequest {
    private double latitude ;//  широта центра поиска
    private double longitude;      // долгота центра поиска
    private int radiusMeters;   // радиус поиска
    private double availableHours;  //  сколько часов есть

    public PlacesSearchRequest() {
        latitude = 0.0;
        longitude = 0.0;
        radiusMeters = 0;
        availableHours = 0.0;
    }
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getRadiusMeters() {
        return radiusMeters;
    }

    public double getAvailableHours() {
        return availableHours;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setRadiusMeters(int radiusMeters) {
        this.radiusMeters = radiusMeters;
    }

    public void setAvailableHours(double availableHours) {
        this.availableHours = availableHours;
    }


}
