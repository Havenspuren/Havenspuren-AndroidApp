package de.jadehs.vcg.data.db.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = POIRoute.class,
                parentColumns = "id",
                childColumns = "route_id",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE)
})
public class RouteProperties {


    /**
     * Property type which displays how much time a user needs to
     */
    public static final String TOTAL_DURATION_TYPE = "total_duration";

    @PrimaryKey(autoGenerate = true)
    private long id;

    /**
     * id of associated route
     */
    @ColumnInfo(name = "route_id")
    private long routeId;

    /**
     * Type of property
     */
    @ColumnInfo(name = "propertyType")
    @NonNull
    private String propertyType;


    /**
     * Json String of the property value
     */
    @NonNull
    private String value;

    public RouteProperties(long id, long routeId, @NonNull String propertyType, @NonNull String value) {
        this.id = id;
        this.routeId = routeId;
        this.propertyType = propertyType;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    @NonNull
    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(@NonNull String propertyType) {
        this.propertyType = propertyType;
    }

    @NonNull
    public String getValue() {
        return value;
    }

    public void setValue(@NonNull String value) {
        this.value = value;
    }
}
