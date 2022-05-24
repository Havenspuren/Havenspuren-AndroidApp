package de.jadehs.vcg.data.db.models;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
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
)},
        indices = {@Index("route_id")}
)
public class POIWaypoint implements Serializable {


    public enum UnlockAction {
        GPS, PASSWORD;
    }

    public enum ContentType {
        TEXT, CHAT;
    }

    public POIWaypoint(long id, long routeId, String title, String longDescription, String shortDescription, @Nullable String password, int indexOfRoute, boolean visited, UnlockAction unlockAction, Coordinate position) {
        this.id = id;
        this.routeId = routeId;
        this.title = title;
        this.longDescription = longDescription;
        this.shortDescription = shortDescription;
        this.password = password;
        this.indexOfRoute = indexOfRoute;
        this.visited = visited;
        this.unlockAction = unlockAction;
        this.position = position;
    }

    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "route_id")
    private long routeId;
    private String title;
    @ColumnInfo(name = "long_description")
    private String longDescription;
    @ColumnInfo(name = "short_description")
    private String shortDescription;
    /**
     * password which unlocks this waypoint.
     */
    @Nullable
    private String password;
    @ColumnInfo(name = "index_of_route")
    private int indexOfRoute;
    private boolean visited;
    @ColumnInfo(name = "unlock_action", defaultValue = "GPS")
    private UnlockAction unlockAction;
    /**
     * Defines how to display and interpret the content of the long_description attribute
     */
    @ColumnInfo(name = "long_content_type", defaultValue = "TEXT")
    private ContentType longContentType;
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

    @Nullable
    public String getPassword() {
        return password;
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    public UnlockAction getUnlockAction() {
        return unlockAction;
    }

    public void setUnlockAction(UnlockAction unlockAction) {
        this.unlockAction = unlockAction;
    }

    public ContentType getLongContentType() {
        return longContentType;
    }

    public void setLongContentType(ContentType longContentType) {
        this.longContentType = longContentType;
    }
}
