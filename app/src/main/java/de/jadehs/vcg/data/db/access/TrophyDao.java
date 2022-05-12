package de.jadehs.vcg.data.db.access;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import de.jadehs.vcg.data.db.models.Trophy;
import de.jadehs.vcg.data.db.pojo.TrophyWithWaypoint;

@Dao
public abstract class TrophyDao implements ParentDao<Trophy> {
    private RouteDatabase routeDatabase;

    public TrophyDao(RouteDatabase routeDatabase) {
        this.routeDatabase = routeDatabase;
    }


    @Transaction
    @Query("SELECT * FROM trophy JOIN poiwaypoint on trophy.waypoint_id = poiwaypoint.id WHERE route_id = :routeId")
    public abstract LiveData<List<TrophyWithWaypoint>> getTrophiesOfRoute(long routeId);

}
