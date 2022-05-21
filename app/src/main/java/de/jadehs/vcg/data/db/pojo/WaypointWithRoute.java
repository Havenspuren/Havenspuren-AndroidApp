package de.jadehs.vcg.data.db.pojo;

import androidx.annotation.Nullable;
import androidx.room.Relation;

import de.jadehs.vcg.data.db.models.POIRoute;
import de.jadehs.vcg.data.db.models.POIWaypoint;
import de.jadehs.vcg.data.model.Coordinate;


public class WaypointWithRoute extends POIWaypoint {

    public WaypointWithRoute(long id, long routeId, String title, String longDescription, String shortDescription, @Nullable String password, int indexOfRoute, boolean visited, UnlockAction unlockAction, Coordinate position) {
        super(id, routeId, title, longDescription, shortDescription, password, indexOfRoute, visited, unlockAction, position);
    }

    @Relation(entity = POIRoute.class, entityColumn = "id", parentColumn = "route_id")
    private POIRoute route;

    public POIRoute getRoute() {
        return route;
    }

    public void setRoute(POIRoute route) {
        this.route = route;
    }
}
