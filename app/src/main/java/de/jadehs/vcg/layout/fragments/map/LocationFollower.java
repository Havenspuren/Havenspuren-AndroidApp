package de.jadehs.vcg.layout.fragments.map;

import android.location.Location;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.oscim.core.MapPosition;
import org.oscim.map.Map;

public class LocationFollower extends LocationObserver {

    private Map map;
    private boolean enabled;
    private boolean animate = true;

    public boolean shouldAnimate() {
        return animate;
    }

    public void setAnimate(boolean animate) {
        this.animate = animate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if(this.enabled){
            centerOnLocation();
        }
    }

    public LocationFollower(Map map) {
        this.map = map;
    }


    @Override
    protected void locationUpdated(@NonNull @NotNull Location loc) {
        centerOnLocation(loc);
    }

    private void centerOnLocation(){
        if(getCurrentLocation() != null){
            centerOnLocation(getCurrentLocation());
        }
    }

    private void centerOnLocation(Location loc){
        if(isEnabled()){
            MapPosition point = new MapPosition(loc.getLatitude(),loc.getLongitude(),map.getMapPosition().scale);
            if(shouldAnimate()){
                map.animator().animateTo(point);
            }else{
                map.setMapPosition(point);
            }
        }
    }
}
