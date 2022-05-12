package de.jadehs.vcg.layout.fragments.map;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import org.jetbrains.annotations.NotNull;

public abstract class LocationObserver extends LocationCallback implements LocationListener {

    private Location currentLocation;

    public Location getCurrentLocation() {
        return currentLocation;
    }

    private void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
        locationUpdated(currentLocation);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
        super.onLocationResult(locationResult);
        locationChanged(locationResult.getLastLocation());
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null)
            locationChanged(location);
    }

    private void locationChanged(Location location) {
        if (this.getCurrentLocation() == null || this.getCurrentLocation().distanceTo(location) > 1) {
            this.setCurrentLocation(location);
        }
    }

    protected abstract void locationUpdated(@NonNull Location loc);
}
