package de.jadehs.vcg.data.db.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;


@Entity(foreignKeys = {@ForeignKey(entity = POIWaypoint.class,parentColumns = "id",childColumns = "waypoint_id")})
@Getter
@Setter
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
}
