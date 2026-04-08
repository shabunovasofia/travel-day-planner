package com.travelplanner.places.dto;

public class PlaceDto {

    private String placeId;
    private String name ;
    private String category;
    private double latitude;
    private double longitude;
    private double estimatedHours;
    private String description;
    private Double rating ;

    public PlaceDto(String placeId, String name, String category, double latitude, double longitude, double estimatedHours, String description, Double rating) {
        this.placeId = placeId;
        this.name = name;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.estimatedHours = estimatedHours;
        this.description = description;
        this.rating = rating;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getEstimatedHours() {
        return estimatedHours;
    }

    public String getDescription() {
        return description;
    }

    public Double getRating() {
        return rating;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}

