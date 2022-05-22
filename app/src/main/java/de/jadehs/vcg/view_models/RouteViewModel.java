package de.jadehs.vcg.view_models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.ListIterator;

import de.jadehs.vcg.data.db.access.RouteDatabase;
import de.jadehs.vcg.data.db.models.POIRoute;
import de.jadehs.vcg.data.db.models.POIWaypoint;
import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia;
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints;
import de.jadehs.vcg.utils.ReplaceableLiveData;

public class RouteViewModel extends DatabaseViewModel {

    /**
     * Mutable live data instance which holds the Route with all waypoints
     */
    ReplaceableLiveData<RouteWithWaypoints> currentRoute;

    /**
     * retrieves the database instance
     *
     * @param application the application who holds the database
     */
    public RouteViewModel(@NonNull Application application, long routeId) {
        super(application);
        currentRoute = new ReplaceableLiveData<>();
        this.setCurrentRoute(routeId);
    }

    /**
     * returns the LiveData instance
     *
     * @return live data of the current route
     */
    public LiveData<RouteWithWaypoints> getCurrentRoute() {
        return currentRoute;
    }

    /**
     * updates the live data with the given value
     *
     * @param currentRoute route with waypoints
     */
    private void setCurrentRoute(LiveData<RouteWithWaypoints> currentRoute) {
        this.currentRoute.setCurrentSource(currentRoute);
    }

    /**
     * retrieves the route with waypoints from the database asynchronously and does publish the retrieved data on the ui thread.
     *
     * @param idOfRoute id of the route which is retrieved from the database
     */
    private void setCurrentRoute(long idOfRoute) {
        setCurrentRoute(getDatabase().routeDao().getRoute(idOfRoute));
    }

    /**
     * update the given waypoint and all previous to have the visited state
     *
     * @param waypoint
     */
    public void unlockWaypointsUntil(final POIWaypoint waypoint) {
        RouteWithWaypoints route = getCurrentRoute().getValue();
        if (route != null) {

            ListIterator<POIWaypointWithMedia> iterator = route.getIteratorAt(waypoint.getId());
            if (iterator != null) {
                POIWaypointWithMedia previous;
                while (iterator.hasPrevious()) {
                    previous = iterator.previous();
                    if (previous.isVisited())
                        continue;

                    previous.setVisited(true);
                    this.updateWaypoint(previous);
                }
                return;
            }
        }
        waypoint.setVisited(true);
        this.updateWaypoint(waypoint);
    }

}
