package de.jadehs.vcg.data.db.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Entity()
@Getter
@Setter
public class POIRoute implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;
    @NonNull
    private String name;
    @NonNull
    private String description;

    /**
     * themes file which defines the app's and map's theme
     */
    @ColumnInfo(name = "themes_file")
    private String themesFile;
    /**
     * the picture which is displayed as the route picture
     */
    @ColumnInfo(name = "path_to_route_image")
    @NonNull
    private String pathToRouteImage;

    /**
     * background picture of the trophy overview
     */
    @ColumnInfo(name = "path_to_map_image")
    @NonNull
    private String pathToMapImage;

    /**
     * picture of character
     */
    @ColumnInfo(name = "path_to_character_image")
    private String pathToCharacterImage;


    /**
     * expected time in minutes this route takes
     */
    @ColumnInfo(name = "expected_time")
    private int expectedTime;


    /**
     * path to map file which contains the map data
     */
    @ColumnInfo(name = "path_to_map")
    @NonNull
    private String pathToMap;


    public boolean hasPathToRouteImage() {
        return getPathToRouteImage() != null;
    }

    public boolean hasCharacterImage() {
        return getPathToCharacterImage() != null;
    }
    public boolean hasThemesFile(){
        return getThemesFile() != null;
    }
}

