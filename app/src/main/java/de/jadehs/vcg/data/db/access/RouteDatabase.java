package de.jadehs.vcg.data.db.access;


import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.jadehs.vcg.data.db.association.WaypointMediaJunction;
import de.jadehs.vcg.data.db.converter.Converters;
import de.jadehs.vcg.data.db.models.Media;
import de.jadehs.vcg.data.db.models.POIRoute;
import de.jadehs.vcg.data.db.models.POIWaypoint;
import de.jadehs.vcg.data.db.models.RouteProperties;
import de.jadehs.vcg.data.db.models.Trophy;


@Database(
        entities = {POIWaypoint.class, POIRoute.class, Media.class, WaypointMediaJunction.class, Trophy.class, RouteProperties.class},
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
    public abstract WaypointDao wayPointDao();

    public abstract RouteDao routeDao();

    public abstract TrophyDao trophyDao();

    private static final String DATABASE_NAME = "VRCityGuide";
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static RouteDatabase database;

    public static synchronized RouteDatabase getInstance(Context context) {
        if (database == null) {
            database = create(context);
        }
        return database;
    }

    private static RouteDatabase create(Context context) {
        RouteDatabase database = Room.databaseBuilder(context, RouteDatabase.class, DATABASE_NAME).createFromAsset("TestData.db").build();
        try { // TODO Remove. Migrations are needed. Only work around
            database.getOpenHelper().getReadableDatabase();
        }catch (IllegalStateException e){
            context.deleteDatabase(DATABASE_NAME);
            database = Room.databaseBuilder(context, RouteDatabase.class, DATABASE_NAME).createFromAsset("TestData.db").build();
        }



        return database;


    }

}
