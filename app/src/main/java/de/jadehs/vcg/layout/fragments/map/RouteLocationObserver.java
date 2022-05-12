package de.jadehs.vcg.layout.fragments.map;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.graphhopper.ResponsePath;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * does observe the current Path and the current Location and notifies all listeners if the path needs to get recalculated
 */
public class RouteLocationObserver extends LocationObserver implements Observer<ResponsePath> {

    public interface RecalculateListener {
        void onRecalculateNeeded(Location newStartLocation);
    }

    public interface NavigationInstructionListener {
        void onNewInstruction(Location currentLocation, Instruction instruction);
    }

    private static final double RECALCULATE_THRESHOLD = 50;


    private static final String TAG = "RouteLocationObserver";

    private ResponsePath currentPath;
    private List<RecalculateListener> recalculateListeners;
    private List<NavigationInstructionListener> instructionListeners;
    private Instruction currentInstruction;
    private boolean pause;



    public RouteLocationObserver() {
        recalculateListeners = new LinkedList<>();
        instructionListeners = new LinkedList<>();
    }

    public void addRecalculateListener(RecalculateListener recalculateListener) {
        if (!this.recalculateListeners.contains(recalculateListener)) {
            this.recalculateListeners.add(recalculateListener);
        }
    }

    public void removeRecalculateListener(RecalculateListener recalculateListener) {
        this.recalculateListeners.remove(recalculateListener);
    }

    protected void notifyRecalculateListener(Location newLocation) {
        Log.d(TAG, "notifyRecalculateListener: Recalculation called");
        if(!pause){
            for (RecalculateListener l : this.recalculateListeners) {
                l.onRecalculateNeeded(newLocation);
            }
        }
    }

    public void addInstructionListener(NavigationInstructionListener navigationInstructionListener) {
        if (!this.instructionListeners.contains(navigationInstructionListener)) {
            this.instructionListeners.add(navigationInstructionListener);
            navigationInstructionListener.onNewInstruction(getCurrentLocation(),currentInstruction);
        }
    }

    public void removeInstructionListener(NavigationInstructionListener navigationInstructionListener) {
        this.instructionListeners.remove(navigationInstructionListener);
    }

    protected void notifyInstructionListeners(Location loc, Instruction instruction) {
        Log.d(TAG, "notifyInstructionListeners: New Instruction available");
        if(!pause){
            for (NavigationInstructionListener l : this.instructionListeners) {
                l.onNewInstruction(loc, instruction);
            }
        }

    }

    @Override
    public void onChanged(ResponsePath responsePath) {
        if (responsePath != null) {
            if (this.currentPath == null || !responsePath.getPoints().equals(this.currentPath.getPoints())) {
                this.currentPath = responsePath;
                checkRecalculationAndCallIfNeeded();
                checkInstructionAndUpdateIfNeeded();
            } else {
                this.currentPath = responsePath;
            }
        }
    }

    @Override
    protected void locationUpdated(@NonNull @NotNull Location loc) {
        checkRecalculationAndCallIfNeeded();
        checkInstructionAndUpdateIfNeeded();
    }

    private void checkRecalculationAndCallIfNeeded() {
        if (currentPath == null && getCurrentLocation() != null) {
            notifyRecalculateListener(getCurrentLocation());
        }
        double distance = getDistanceFromPath();

        if (distance > RECALCULATE_THRESHOLD) {
            notifyRecalculateListener(getCurrentLocation());
        }
    }

    private void checkInstructionAndUpdateIfNeeded() {
        if (currentPath != null && getCurrentLocation() != null) {
            Instruction instruction = currentPath.getInstructions().find(getCurrentLocation().getLatitude(), getCurrentLocation().getLongitude(), RECALCULATE_THRESHOLD);

            if (instruction != null && currentInstruction != instruction) {
                currentInstruction = instruction;
                notifyInstructionListeners(getCurrentLocation(), currentInstruction);
            }
        } else {
            if (currentInstruction != null) {
                currentInstruction = null;
                notifyInstructionListeners(null, null);
            }
        }
    }

    /**
     * does calculate the distance to the path
     *
     * @return distance in meters. -1 if no distance could get calculated
     */
    private double getDistanceFromPath() {
        if (currentPath != null && getCurrentLocation() != null) {
            PointList points = currentPath.getPoints();
            Double minLength = null;
            if (points.size() > 1) {
                Iterator<GHPoint3D> iterator = points.iterator();
                GHPoint lastPoint = iterator.next();
                while (iterator.hasNext()) {
                    GHPoint currentPoint = iterator.next();
                    double distance = getDistanceToLine(lastPoint, currentPoint);
                    if (distance > 0) {
                        if (minLength == null) {
                            minLength = distance;
                        } else if (distance < minLength) {
                            minLength = distance;
                        }
                    }
                    lastPoint = currentPoint;
                }
            }
            return minLength == null ? -1 : minLength;

        } else {
            Log.d(TAG, "getDistanceFromPath: Nothing to analyse");
            return -1;
        }
    }

    /**
     * does calculate the smallest length th the line
     *
     * @param start
     * @param end
     * @return
     */
    private double getDistanceToLine(GHPoint start, GHPoint end) {
        if (getCurrentLocation() != null) {
            //direction
            GHPoint diff = new GHPoint(end.getLat() - start.getLat(), end.getLon() - start.getLon());

            //location
            GHPoint loc = new GHPoint(getCurrentLocation().getLatitude(), getCurrentLocation().getLongitude());
            //direction to line
            GHPoint ortho = new GHPoint(-diff.getLon(), diff.getLat());
            // TODO change could use Helper.DIST_EARTH.calcNormalizedEdgeDistance for distance calculation
            double steigung = ortho.getLat() / ortho.getLon();
            double achsenAbschnitt = loc.getLat() - loc.getLon() * steigung;

            //
            double steigungLine = diff.getLat() / diff.getLon();
            double achsenAbschnittLine = start.getLat() - start.getLon() * steigungLine;

            // equation to calculate interception
            // x*m1 + b1 = x*m2 + b2 | -x*m1
            // b1 = x*m2 +b2 -x*m1 | -b2
            // b1 -b2 = x*m2 - x*m1
            // b1 -b2 = x*(m2-m1) | / (m2-m1)
            // (b1-b2)/(m2-m1) = x

            double pointLong = (achsenAbschnitt - achsenAbschnittLine) / (steigungLine - steigung);

            double pointLat = steigung * pointLong + achsenAbschnitt;

            // (x1,y1) + x * (x2,y2) = (x3,y3)
            //  x1 + x * x2 = x3 | -x1
            //  x*x2 = x3 - x1 | / x2
            //  x = (x3 - x1)/x2

            double multiplierLat = (pointLat - start.getLat()) / diff.getLat();


            if (multiplierLat > 1) {
                pointLat = end.getLat();
                pointLong = end.getLon();
            } else if (multiplierLat < 0) {
                pointLat = start.getLat();
                pointLong = start.getLon();
            }

            return Helper.DIST_EARTH.calcDist(pointLat, pointLong, loc.getLat(), loc.getLon());
        } else {
            return -1D;
        }
    }

    public void invalidate(){
        if(getCurrentLocation() != null){
            notifyRecalculateListener(getCurrentLocation());
        }

    }


    public void pause(){
        pause = true;
    }
    public void resume(){
        pause = false;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

