package com.travelplanner.places.client;

public record OpenTripMapPlace(
    String xid,
    String name,
    double latitude,
    double longitude,
    double rating,
    String address,
    String description) {}
