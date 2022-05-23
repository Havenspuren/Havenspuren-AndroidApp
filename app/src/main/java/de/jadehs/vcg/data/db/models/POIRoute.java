package de.jadehs.vcg.data.db.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;


@Entity()
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
     * Color of the navigation path
     */
    @ColumnInfo(name = "navigation_path_color", defaultValue = "2570282751")
    @Nullable
    private Integer navigationPathColor;


    /**
     * path to map file which contains the map data
     */
    @ColumnInfo(name = "path_to_map")
    @NonNull
    private String pathToMap;

    public POIRoute(long id, @NonNull String name, @NonNull String description, String themesFile, @NonNull String pathToRouteImage, @NonNull String pathToMapImage, String pathToCharacterImage, int expectedTime, int navigationPathColor, @NonNull String pathToMap) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.themesFile = themesFile;
        this.pathToRouteImage = pathToRouteImage;
        this.pathToMapImage = pathToMapImage;
        this.pathToCharacterImage = pathToCharacterImage;
        this.expectedTime = expectedTime;
        this.navigationPathColor = navigationPathColor;
        this.pathToMap = pathToMap;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    public void setDescription(@NonNull String description) {
        this.description = description;
    }

    public String getThemesFile() {
        return themesFile;
    }

    public void setThemesFile(String themesFile) {
        this.themesFile = themesFile;
    }

    @NonNull
    public String getPathToRouteImage() {
        return pathToRouteImage;
    }

    public void setPathToRouteImage(@NonNull String pathToRouteImage) {
        this.pathToRouteImage = pathToRouteImage;
    }

    @NonNull
    public String getPathToMapImage() {
        return pathToMapImage;
    }

    public void setPathToMapImage(@NonNull String pathToMapImage) {
        this.pathToMapImage = pathToMapImage;
    }

    public String getPathToCharacterImage() {
        return pathToCharacterImage;
    }

    public void setPathToCharacterImage(String pathToCharacterImage) {
        this.pathToCharacterImage = pathToCharacterImage;
    }

    public int getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(int expectedTime) {
        this.expectedTime = expectedTime;
    }

    @NonNull
    public String getPathToMap() {
        return pathToMap;
    }

    public void setPathToMap(@NonNull String pathToMap) {
        this.pathToMap = pathToMap;
    }

    public boolean hasPathToRouteImage() {
        return getPathToRouteImage() != null;
    }

    public boolean hasCharacterImage() {
        return getPathToCharacterImage() != null;
    }

    public boolean hasThemesFile() {
        return getThemesFile() != null;
    }

    @Nullable
    public Integer getNavigationPathColor() {
        return navigationPathColor;
    }

    public void setNavigationPathColor(@Nullable Integer navigationPathColor) {
        this.navigationPathColor = navigationPathColor;
    }
}

