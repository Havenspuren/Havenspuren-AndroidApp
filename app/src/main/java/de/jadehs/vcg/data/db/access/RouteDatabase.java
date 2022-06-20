package de.jadehs.vcg.data.db.access;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.jadehs.vcg.BuildConfig;
import de.jadehs.vcg.data.db.association.WaypointMediaJunction;
import de.jadehs.vcg.data.db.converter.Converters;
import de.jadehs.vcg.data.db.models.Media;
import de.jadehs.vcg.data.db.models.POIRoute;
import de.jadehs.vcg.data.db.models.POIWaypoint;
import de.jadehs.vcg.data.db.models.RouteProperty;
import de.jadehs.vcg.data.db.models.Trophy;
import kotlin.io.FilesKt;


@Database(
        entities = {POIWaypoint.class, POIRoute.class, Media.class, WaypointMediaJunction.class, Trophy.class, RouteProperty.class},
        version = 17,
        autoMigrations = {
                @AutoMigration(from = 13, to = 14),
                @AutoMigration(from = 14, to = 15),
                @AutoMigration(from = 15, to = 16),
                @AutoMigration(from = 16, to = 17)
        }
)
@TypeConverters({Converters.class})
public abstract class RouteDatabase extends RoomDatabase {
    private static final String TAG = "RouteDatabase";
    private static final String DATABASE_NAME = "VRCityGuide";
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static RouteDatabase database;

    public static synchronized RouteDatabase getInstance(Context context) {
        deleteDatabaseIfOldVersion(context); // TODO remove if if not in alpha and beta
        if (database == null) {
            database = create(context);
        }
        return database;
    }

    private static RouteDatabase create(Context context) {
        Builder<RouteDatabase> builder = getDatabaseBuilder(context);
        RouteDatabase database = builder.build();
        try { // TODO Remove. Migrations are needed. Only work around
            database.getOpenHelper().getReadableDatabase();
        } catch (IllegalStateException e) {
            context.deleteDatabase(DATABASE_NAME);
            database = builder.build();
        }

        context.getSharedPreferences("GENERAL", Context.MODE_PRIVATE)
                .edit().putInt("DATABASE_VERSION", BuildConfig.DATABASE_DATA_VERSION).commit();
        return database;
    }

    @NonNull
    private static Builder<RouteDatabase> getDatabaseBuilder(Context context) {
        return Room.databaseBuilder(context, RouteDatabase.class, DATABASE_NAME).createFromAsset("TestData.db");
    }

    private static void copyVisitedInto(RouteDatabase database, List<POIWaypoint> visitedStatus) {

        for (POIWaypoint waypoint : visitedStatus) {
            try {
                database.wayPointDao().updateVisited(waypoint.getId(), waypoint.isVisited());
            } catch (Exception e) {
                Log.w(TAG, "Tried update visited status, while upgrading database, but an error occurred", e);
            }
        }

    }

    private static void deleteDatabaseIfOldVersion(Context context) {
        int currentDatabaseVersion = context
                .getSharedPreferences("GENERAL", Context.MODE_PRIVATE)
                .getInt("DATABASE_VERSION", -1);
        if (currentDatabaseVersion < BuildConfig.DATABASE_DATA_VERSION) {
            context.deleteDatabase(DATABASE_NAME);
        }
    }

    public abstract WaypointDao wayPointDao();

    public abstract RouteDao routeDao();

    public abstract TrophyDao trophyDao();

}
