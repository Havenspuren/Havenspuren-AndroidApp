package de.jadehs.vcg.data.db.association;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.io.Serializable;

import de.jadehs.vcg.data.db.models.Media;
import de.jadehs.vcg.data.db.models.POIWaypoint;

@Entity(primaryKeys = {"waypoint_id","media_id"},
foreignKeys = {
        @ForeignKey(
                entity = POIWaypoint.class,
                parentColumns = "id",
                childColumns = "waypoint_id"
        ),
        @ForeignKey(
                entity = Media.class,
                parentColumns = "id",
                childColumns = "media_id"
        )
},
indices = {@Index("waypoint_id"),@Index("media_id")})
public class WaypointMediaJunction implements Serializable {
    @ColumnInfo(name = "waypoint_id")
    private long waypointId;
    @ColumnInfo(name = "media_id")
    private long mediaId;

    public WaypointMediaJunction(long waypointId, long mediaId) {
        this.waypointId = waypointId;
        this.mediaId = mediaId;
    }

    public long getWaypointId() {
        return waypointId;
    }

    public void setWaypointId(long waypointId) {
        this.waypointId = waypointId;
    }

    public long getMediaId() {
        return mediaId;
    }

    public void setMediaId(long mediaId) {
        this.mediaId = mediaId;
    }
}
