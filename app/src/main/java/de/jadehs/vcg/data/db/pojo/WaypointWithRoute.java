package de.jadehs.vcg.data.db.pojo;

import androidx.room.Relation;

import de.jadehs.vcg.data.db.models.POIRoute;
import de.jadehs.vcg.data.db.models.POIWaypoint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WaypointWithRoute extends POIWaypoint {

    @Relation(entity = POIRoute.class, entityColumn = "id", parentColumn = "route_id")
    private POIRoute route;
}
