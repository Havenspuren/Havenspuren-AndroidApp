package de.jadehs.vcg.data.db.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;


@Entity(foreignKeys = {@ForeignKey(entity = POIWaypoint.class, parentColumns = "id", childColumns = "waypoint_id")},
        indices = {@Index("waypoint_id")})
public class Trophy implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "waypoint_id")
    private long waypointId;
    @ColumnInfo(name = "path_to_image")
    @NonNull
    private String pathToImage;
    @ColumnInfo(name = "path_to_icon")
    @NonNull
    private String pathToIcon;
    @ColumnInfo(name = "path_to_character_image")
    private String pathToCharacterImage;
    private float x;
    private float y;
    @NonNull
    private String description;
    @NonNull
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getWaypointId() {
        return waypointId;
    }

    public void setWaypointId(long waypointId) {
        this.waypointId = waypointId;
    }

    @NonNull
    public String getPathToImage() {
        return pathToImage;
    }

    public void setPathToImage(@NonNull String pathToImage) {
        this.pathToImage = pathToImage;
    }

    @NonNull
    public String getPathToIcon() {
        return pathToIcon;
    }

    public void setPathToIcon(@NonNull String pathToIcon) {
        this.pathToIcon = pathToIcon;
    }

    public String getPathToCharacterImage() {
        return pathToCharacterImage;
    }

    public void setPathToCharacterImage(String pathToCharacterImage) {
        this.pathToCharacterImage = pathToCharacterImage;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    public void setDescription(@NonNull String description) {
        this.description = description;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }
}
