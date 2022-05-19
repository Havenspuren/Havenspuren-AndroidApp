package de.jadehs.vcg.data.db.pojo;

import androidx.room.Relation;

import de.jadehs.vcg.data.db.models.POIWaypoint;
import de.jadehs.vcg.data.db.models.Trophy;

public class TrophyWithWaypoint extends Trophy {

    @Relation(
            entity = POIWaypoint.class,
            parentColumn = "waypoint_id",
            entityColumn = "id"
    )
    private WaypointWithRoute waypoint;

    public WaypointWithRoute getWaypoint() {
        return waypoint;
    }

    public void setWaypoint(WaypointWithRoute waypoint) {
        this.waypoint = waypoint;
    }
}
