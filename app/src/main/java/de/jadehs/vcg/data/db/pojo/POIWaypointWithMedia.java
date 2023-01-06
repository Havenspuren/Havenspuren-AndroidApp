package de.jadehs.vcg.data.db.pojo;


import androidx.annotation.Nullable;
import androidx.room.Ignore;
import androidx.room.Junction;
import androidx.room.Relation;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import de.jadehs.vcg.data.db.association.WaypointMediaJunction;
import de.jadehs.vcg.data.db.models.Media;
import de.jadehs.vcg.data.db.models.POIRoute;
import de.jadehs.vcg.data.db.models.POIWaypoint;
import de.jadehs.vcg.data.db.models.Trophy;
import de.jadehs.vcg.data.model.Coordinate;

public class POIWaypointWithMedia extends POIWaypoint implements Serializable {

    public POIWaypointWithMedia(long id, long routeId, String title, String longDescription, String shortDescription, @Nullable String password, int indexOfRoute, boolean visited, UnlockAction unlockAction, Coordinate position, boolean addToProgress) {
        super(id, routeId, title, longDescription, shortDescription, password, indexOfRoute, visited, unlockAction, position, addToProgress);
    }

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = WaypointMediaJunction.class,
                    parentColumn = "waypoint_id",
                    entityColumn = "media_id"
            )
    )
    private List<Media> media;


    @Relation(entity = POIRoute.class, parentColumn = "route_id", entityColumn = "id")
    private POIRoute route;


    @Relation(
            entity = Trophy.class,
            parentColumn = "id",
            entityColumn = "waypoint_id"
    )
    private Trophy trophy;

    @Ignore
    private List<Media> pictures;
    @Ignore
    private Media audio;
    @Ignore
    private Media arFile;

    public List<Media> getMedia() {
        return media;
    }

    /**
     * does set the all media fields.
     *
     * @param media
     */
    public void setMedia(List<Media> media) {
        this.media = media;
        this.pictures = new LinkedList<>();
        this.audio = null;
        this.arFile = null;
        for (Media m : media) {
            if (m.getType() == Media.MediaType.PICTURES) {
                this.pictures.add(m);
            }

            if (m.getType() == Media.MediaType.AUDIO) {
                if (this.audio != null) {
                    throw new IllegalStateException("A waypoint is only allowed to have one audio file");
                } else {
                    this.audio = m;
                }
            }

            if (m.getType() == Media.MediaType.AR) {
                if (this.arFile != null) {
                    throw new IllegalStateException("A waypoint is only allowed to have one ar file");
                } else {
                    this.arFile = m;
                }
            }
        }
    }

    public List<Media> getPictures() {
        return this.pictures;
    }

    public boolean hasAudio() {
        return this.audio != null;
    }

    public Media getAudio() {
        return this.audio;
    }

    public boolean hasAR() {
        return this.arFile != null;
    }

    public Media getAr() {
        return this.arFile;
    }

    public Trophy getTrophy() {
        return trophy;
    }

    public void setTrophy(Trophy trophy) {
        this.trophy = trophy;
    }

    public boolean hasTrophy() {
        return getTrophy() != null;
    }

    public POIRoute getRoute() {
        return route;
    }

    public void setRoute(POIRoute route) {
        this.route = route;
    }
}
