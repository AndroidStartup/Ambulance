package com.example.mahmoud.ambulance;

/**
 * Created by Mahmoud on 8/16/2016.
 */
public class Accident {
    String description;
    String accidentType;
    Double lat,lng;
    String accidentUser;

    public Accident(String description, String accidentType, Double lat, Double lng, String accidentUser) {
        this.description = description;
        this.accidentType = accidentType;
        this.lat = lat;
        this.lng = lng;
        this.accidentUser = accidentUser;
    }

    public Accident() {
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
    public String getAccidentUser() {
        return accidentUser;
    }
}
