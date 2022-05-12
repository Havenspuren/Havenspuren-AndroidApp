package de.jadehs.vcg.view_models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

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
    public RouteViewModel(@NonNull Application application) {
        super(application);
        currentRoute = new ReplaceableLiveData<>();
    }

    /**
     * returns the LiveData instance
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
    public void setCurrentRoute(long idOfRoute) {
        setCurrentRoute(getDatabase().routeDao().getRoute(idOfRoute));
    }
}
