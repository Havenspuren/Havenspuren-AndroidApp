package de.jadehs.vcg.view_models;

import android.app.Application;

import androidx.annotation.NonNull;

import java.io.IOException;

import de.jadehs.vcg.data.db.access.RouteDatabase;
import de.jadehs.vcg.data.db.models.POIWaypoint;
import de.jadehs.vcg.utils.RouteMemoryCache;

public class MapViewModel extends DatabaseViewModel {
    private static final String TAG = "MainActivityViewModel";


    private RouteMemoryCache routeCache;


    /**
     * ATTENTION if the viewmodel is every changed to accept the graph folder in the constructor,
     * the viewmodelsprovider needs to have a custom key to for the map path
     * @param application
     */
    public MapViewModel(@NonNull Application application) {
        super(application);
    }

    public void setGraphFolder(String mapName){
        if(this.routeCache != null){
            try {
                this.routeCache.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        this.routeCache = new RouteMemoryCache(getApplication(),mapName);
    }

    public RouteMemoryCache getRouteCache() {
        return routeCache;
    }
}
