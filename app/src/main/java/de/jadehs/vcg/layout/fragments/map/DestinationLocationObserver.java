package de.jadehs.vcg.layout.fragments.map;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia;
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints;

public class DestinationLocationObserver extends LocationObserver implements Observer<RouteWithWaypoints> {

    private static final float VISITED_TRESHOLD = 10;

    public interface NextStepReachedListener {
        void onNextStepReachedListener(POIWaypointWithMedia waypoint);
    }

    private RouteWithWaypoints currentRoute;
    private List<NextStepReachedListener> nextStepReachedListeners;

    public DestinationLocationObserver() {
        nextStepReachedListeners = new LinkedList<>();
    }

    public void addNextStepReachedListener(NextStepReachedListener listener) {
        if (!nextStepReachedListeners.contains(listener)) {
            nextStepReachedListeners.add(listener);
        }
    }

    public void removeNextStepReachedListener(NextStepReachedListener listener) {
        nextStepReachedListeners.remove(listener);
    }

    protected void notifyNextStepReachedListeners(POIWaypointWithMedia waypoint) {
        for (NextStepReachedListener l : nextStepReachedListeners) {
            l.onNextStepReachedListener(waypoint);
        }
    }

    @Override
    protected void locationUpdated(@NonNull @NotNull Location loc) {
        if (currentRoute != null) {
            POIWaypointWithMedia nextStep = currentRoute.getNextWaypoint();
            if (nextStep != null) {
                float[] length = new float[1];
                Location.distanceBetween(
                        loc.getLatitude(),
                        loc.getLongitude(),
                        nextStep.getGeoPosition().getLatitude(),
                        nextStep.getGeoPosition().getLongitude(), length);
                if (length[0] <= VISITED_TRESHOLD) {
                    notifyNextStepReachedListeners(nextStep);
                }
            }
        }
    }

    @Override
    public void onChanged(RouteWithWaypoints routeWithWaypoints) {
        this.currentRoute = routeWithWaypoints;
    }
}
