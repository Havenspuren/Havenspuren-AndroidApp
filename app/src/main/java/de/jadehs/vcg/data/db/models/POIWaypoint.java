package de.jadehs.vcg.data.db.models;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;

import org.oscim.core.GeoPoint;

import java.io.Serializable;

import de.jadehs.vcg.data.model.Coordinate;


@Entity(foreignKeys = {@ForeignKey(
        entity = POIRoute.class,
        parentColumns = "id",
        childColumns = "route_id",
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
)})
public class POIWaypoint implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "route_id")
    private long routeId;
    private String title;
    @ColumnInfo(name = "long_description")
    private String longDescription;
    @ColumnInfo(name = "short_description")
    private String shortDescription;
    @ColumnInfo(name = "index_of_route")
    private int indexOfRoute;
    private boolean visited;

    @Embedded
    private Coordinate position;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Coordinate getPosition() {
        return position;
    }

    public void setPosition(Coordinate position) {
        this.position = position;
    }

    public GeoPoint getGeoPosition() {
        return position.toGeoPoint();
    }

    public void setGeoPosition(GeoPoint position) {
        this.position = Coordinate.fromGeoPoint(position);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public int getIndexOfRoute() {
        return indexOfRoute;
    }

    public void setIndexOfRoute(int indexOfRoute) {
        this.indexOfRoute = indexOfRoute;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }
}
