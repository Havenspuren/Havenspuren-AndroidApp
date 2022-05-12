package de.jadehs.vcg.view_models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import de.jadehs.vcg.data.db.access.RouteDatabase;

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
}
