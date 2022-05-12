package de.jadehs.vcg.data.db.access;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import de.jadehs.vcg.data.db.models.POIRoute;
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints;

@Dao
public abstract class RouteDao implements ParentDao<POIRoute> {

    private static final String TAG = "RouteDao";

//    @Query("SELECT POIROUTE.*,POIWaypoint.id as WAY_id,POIWaypoint.long_description as WAY_long_description ,POIWaypoint.short_description as WAY_short_description,POIWaypoint.title as WA_title" +
//            " FROM POIRoute,POIWaypoint,RouteWaypoint WHERE RouteWaypoint.routeId = POIRoute.id AND POIWaypoint.id = RouteWaypoint.waypointId")
//    @Query("SELECT r.* , w.id as WAY_id,w.short_description as WAY_short_description, w.title as WAY_title, w.long_description as WAY_long_description FROM POIRoute as r JOIN RouteWaypoint as rw on (r.id = rw.routeId) JOIN POIWaypoint as w on (rw.waypointId = w.id)")


    private RouteDatabase database;

    public RouteDao(RouteDatabase database) {
        this.database = database;
    }

    @Transaction
    @Query("Select * from POIRoute")
    public abstract LiveData<List<RouteWithWaypoints>> getRoutes();

    @Transaction
    @Query("SELECT * FROM POIRoute WHERE id = :id")
    public abstract LiveData<RouteWithWaypoints> getRoute(long id);

    @Transaction
    @Query("SELECT * FROM POIRoute WHERE id = :id")
    public abstract LiveData<POIRoute> getRawRoute(long id);

}
