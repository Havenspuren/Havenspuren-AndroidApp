package de.jadehs.vcg.data.db.pojo;


import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;

import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import de.jadehs.vcg.data.db.models.POIRoute;
import de.jadehs.vcg.data.db.models.POIWaypoint;
import de.jadehs.vcg.data.db.models.RouteProperty;
import de.jadehs.vcg.utils.CollectionUtils;
import kotlin.jvm.functions.Function1;


public class RouteWithWaypoints implements Serializable {


    @Ignore
    private BoundingBox bounds;
    @NonNull
    @Embedded
    private POIRoute poiRoute;
    @NonNull
    @Relation(entity = POIWaypoint.class, entityColumn = "route_id", parentColumn = "id")
    private List<POIWaypointWithMedia> waypoints;
    @NonNull
    @Relation(entity = RouteProperty.class, entityColumn = "route_id", parentColumn = "id")
    private List<RouteProperty> properties;

    @Ignore
    public RouteWithWaypoints(@NonNull POIRoute poiRoute) {
        this.poiRoute = poiRoute;
        waypoints = Collections.emptyList();
        properties = Collections.emptyList();
    }

    public RouteWithWaypoints(@NonNull POIRoute poiRoute, @NonNull List<POIWaypointWithMedia> waypoints, @NonNull List<RouteProperty> properties) {

        this.poiRoute = poiRoute;
        this.properties = properties;
        setWaypoints(waypoints);
    }

    @NonNull
    public List<RouteProperty> getProperties() {
        return properties;
    }

    public void setProperties(@NonNull List<RouteProperty> properties) {
        this.properties = properties;
    }

    @NonNull
    public List<POIWaypointWithMedia> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<POIWaypointWithMedia> waypoints) {
        this.waypoints = waypoints;
        this.bounds = null;
        Collections.sort(this.waypoints, new Comparator<POIWaypointWithMedia>() {
            @Override
            public int compare(POIWaypointWithMedia o1, POIWaypointWithMedia o2) {
                return Integer.compare(o1.getIndexOfRoute(), o2.getIndexOfRoute());
            }
        });
    }

    @NonNull
    public POIRoute getPoiRoute() {
        return poiRoute;
    }

    public void setPoiRoute(@NonNull POIRoute poiRoute) {
        this.poiRoute = poiRoute;
    }

    /**
     * Finds the waypoint which the user is supposed to visit next, null if all waypoints were visited
     *
     * @return a waypoint or null if all waypoints were visited
     */
    public POIWaypointWithMedia getNextWaypoint() {
        return searchWaypoint(new Function1<POIWaypointWithMedia, Boolean>() {
            @Override
            public Boolean invoke(POIWaypointWithMedia poiWaypointWithMedia) {
                return !poiWaypointWithMedia.isVisited();
            }
        });
    }

    public POIWaypointWithMedia getLastUnlocked() {
        return searchWaypoint(new Function1<POIWaypointWithMedia, Boolean>() {
            @Override
            public Boolean invoke(POIWaypointWithMedia poiWaypointWithMedia) {
                return poiWaypointWithMedia.isVisited();
            }
        }, true);
    }

    public ListIterator<POIWaypointWithMedia> getIteratorAt(long waypointId) {
        ListIterator<POIWaypointWithMedia> iterator = this.getWaypoints().listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId() == waypointId) {
                return iterator;
            }
        }
        return null;
    }

    public BoundingBox getBounds() {
        if (this.bounds == null) {
            this.bounds = calcBounds();
        }

        return bounds;
    }

    private BoundingBox calcBounds() {
        return new BoundingBox(CollectionUtils.map(getWaypoints(), new CollectionUtils.CollectionMapper<POIWaypointWithMedia, GeoPoint>() {
            @Override
            public GeoPoint map(POIWaypointWithMedia value) {
                return value.getGeoPosition();
            }
        }));
    }

    /**
     * does return number between zero and one indicating how many waypoints have been visited in percent
     *
     * @return the percentage of the visited waypoints
     */
    public float getProgress() {
        int count = 0;
        int ignoreCount = 0;
        for (POIWaypoint w : getWaypoints()) {
            if (w.isVisited() && w.isAddToProgress())
                count++;
            if (!w.isAddToProgress()) {
                ignoreCount++;
            }
        }
        return count / (float) (getWaypoints().size() - ignoreCount);
    }


    public int getVisitedCount() {
        int count = 0;
        for (POIWaypoint w : getWaypoints()) {
            if (w.isVisited())
                count++;
        }

        return count;
    }

    public int getTrophyCount() {
        int count = 0;
        for (POIWaypointWithMedia w : getWaypoints()) {
            if (w.hasTrophy())
                count++;
        }
        return count;
    }

    public int getUnlockedTrophyCount() {
        int count = 0;
        for (POIWaypointWithMedia w : getWaypoints()) {
            if (w.isVisited() && w.hasTrophy())
                count++;
        }
        return count;
    }

    /**
     * counts how many trophies this route has and how many of them are unlocked
     *
     * @return array with the length of 2, first number represents the unlocked trophies and the second one the amount of trophies
     */
    public int[] getTrophyProgress() {
        int[] progress = new int[2];
        for (POIWaypointWithMedia w : getWaypoints()) {
            if (w.hasTrophy()) {
                progress[1]++;
                if (w.isVisited())
                    progress[0]++;
            }

        }
        return progress;
    }

    public POIWaypointWithMedia getWaypointById(final long id) {
        return searchWaypoint(new Function1<POIWaypointWithMedia, Boolean>() {
            @Override
            public Boolean invoke(POIWaypointWithMedia poiWaypointWithMedia) {
                return poiWaypointWithMedia.getId() == id;
            }
        });
    }

    /**
     * Iterates over the waypoints collection from first index to last and calls the predicate for each element.
     * First time the predicate returns true, the current waypoint is returned and the loop is interrupted
     *
     * @param predicate
     * @return
     */
    private POIWaypointWithMedia searchWaypoint(Function1<POIWaypointWithMedia, Boolean> predicate) {
        return this.searchWaypoint(predicate, false);
    }

    /**
     * Iterates over the waypoints collection and calls the predicate for each element.
     * First time the predicate returns true, the current waypoint is returned and the loop is interrupted
     *
     * @param predicate
     * @param reverse   if true the collection is iterated from last to first
     * @return
     */
    private POIWaypointWithMedia searchWaypoint(Function1<POIWaypointWithMedia, Boolean> predicate, boolean reverse) {
        ListIterator<POIWaypointWithMedia> iterator = getWaypoints().listIterator(reverse ? getWaypoints().size() : 0);
        POIWaypointWithMedia found = null;
        while (reverse ? iterator.hasPrevious() : iterator.hasNext() && found == null) {
            POIWaypointWithMedia waypoint = reverse ? iterator.previous() : iterator.next();
            if (predicate.invoke(waypoint)) {
                found = waypoint;
            }
        }
        return found;
    }
}
