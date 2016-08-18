package com.example.mahmoud.ambulance;

/**
 * Created by Mahmoud on 8/15/2016.
 */
public class Ambulance {
    Double lat,lng;

    public Ambulance(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Ambulance() {
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }
}
