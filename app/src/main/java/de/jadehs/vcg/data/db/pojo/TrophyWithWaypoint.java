package de.jadehs.vcg.data.db.pojo;

import androidx.room.Relation;

import de.jadehs.vcg.data.db.models.POIWaypoint;
import de.jadehs.vcg.data.db.models.Trophy;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrophyWithWaypoint extends Trophy {

    @Relation(
            entity = POIWaypoint.class,
            parentColumn = "waypoint_id",
            entityColumn = "id"
    )
    private WaypointWithRoute waypoint;

}
