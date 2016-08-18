package com.example.mahmoud.ambulance;

/**
 * Created by Mahmoud on 8/15/2016.
 */
public class Patient {
    String description;
    String accidentType;
    Double lat,lng;

    public Patient(String description, String accidentType, Double lat, Double lng) {
        this.description = description;
        this.accidentType = accidentType;
        this.lat = lat;
        this.lng = lng;
    }

    public Patient() {
    }

    public String getDescription() {
        return description;
    }

    public String getAccidentType() {
        return accidentType;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }
}
