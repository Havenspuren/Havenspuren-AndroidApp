package de.jadehs.vcg.data.db.access;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import de.jadehs.vcg.data.db.models.POIWaypoint;
import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia;

@Dao
public abstract class WaypointDao implements ParentDao<POIWaypoint> {

    private RouteDatabase database;

    public WaypointDao(RouteDatabase database) {
        this.database = database;
    }

    @Transaction
    @Query("SELECT * FROM POIWaypoint WHERE id = :id")
    public abstract LiveData<POIWaypointWithMedia> getWaypoint(long id);

    @Transaction
    @Query("SELECT * FROM POIWaypoint WHERE id = :id ORDER BY index_of_route ASC")
    public abstract LiveData<List<POIWaypointWithMedia>> getWaypointsFromRoute(long id);

    @Query("SELECT * FROM POIWaypoint")
    public abstract List<POIWaypoint> getAllWaypoints();


    @Query("UPDATE POIWaypoint SET visited=:visited WHERE id=:id")
    protected abstract void updateVisited(long id, int visited);

    /**
     * updates the visited status of the waypoint specified by the id
     *
     * @param id      id of the waypoint
     * @param visited whether the given waypoint was visited
     */
    public void updateVisited(long id, boolean visited) {
        updateVisited(id, visited ? 1 : 0);
    }
}
