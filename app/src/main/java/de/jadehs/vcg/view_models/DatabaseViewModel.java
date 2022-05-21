package de.jadehs.vcg.view_models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import de.jadehs.vcg.data.db.access.RouteDatabase;
import de.jadehs.vcg.data.db.models.POIWaypoint;

public class DatabaseViewModel extends AndroidViewModel {

    /**
     * database instance
     */
    private RouteDatabase database;

    public DatabaseViewModel(@NonNull Application application) {
        super(application);
        database = RouteDatabase.getInstance(application.getApplicationContext());
    }

    /**
     * returns the stored database instance
     * @return database instance
     */
    public synchronized RouteDatabase getDatabase() {
        return database;
    }

    /**
     * Update the specific waypoint
     *
     * @param waypoint
     */
    public void updateWaypoint(final POIWaypoint waypoint) {
        RouteDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getDatabase().wayPointDao().update(waypoint);
            }
        });
    }
}
