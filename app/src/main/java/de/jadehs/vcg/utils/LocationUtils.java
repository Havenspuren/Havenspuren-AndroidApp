package de.jadehs.vcg.utils;

import android.location.Location;

import org.oscim.core.GeoPoint;


public class LocationUtils {

    public static float getDistanceTo(Location location , GeoPoint location2){
        float[] results = new float[1];
        Location.distanceBetween(location.getLatitude(),location.getLongitude(),location2.getLatitude(),location2.getLongitude(),results);
        return results[0];
    }
}
