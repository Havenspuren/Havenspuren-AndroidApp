package de.jadehs.vcg.data.model;


import org.oscim.core.GeoPoint;

import java.io.Serializable;

public class Coordinate implements Serializable {


    public static Coordinate fromGeoPoint(GeoPoint point){
        return new Coordinate(point.getLatitude(), point.getLongitude());
    }

    private Double latitude;
    private Double longitude;

    public Coordinate(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public GeoPoint toGeoPoint(){
        return new GeoPoint(getLatitude(), getLongitude());
    }
}
