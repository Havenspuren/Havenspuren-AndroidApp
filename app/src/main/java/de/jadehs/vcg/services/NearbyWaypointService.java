package de.jadehs.vcg.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import de.jadehs.vcg.R;
import de.jadehs.vcg.layout.activities.MainActivity;

public class NearbyWaypointService extends Service {

    private static final String TAG = "NearbyWaypointService";


    public final static String NOTIF_ID = "de.voltfinder.nearby_notif";
    private final static int notif_id = 897;
    private final static int notif_id_detail = 900;


    public final static String EXTRA_WPID = "de.jadehs.vcg.services.NearbyWaypointService.ID";
    public final static String EXTRA_MAPID = "de.jadehs.vcg.services.NearbyWaypointService.MAP_ID";
    public final static String EXTRA_WPLNG = "de.jadehs.vcg.services.NearbyWaypointService.LNG";
    public final static String EXTRA_WPLAT = "de.jadehs.vcg.services.NearbyWaypointService.LAT";
    public final static String EXTRA_NEXT = "de.jadehs.vcg.services.NearbyWaypointService.NEXT_ID";


    /**
     * distance within the waypoint is reached
     */
    private final static int threshhold = 10;
    private final CharSequence textTitle = "Virtual City Guide";
    private final CharSequence textContent = "Die Stadtführung läuft";
    private final CharSequence detailContent = "Du bist nah an einem Punkt";


    private NotificationCompat.Builder builderForeground = new NotificationCompat.Builder(this, NOTIF_ID)
            .setSmallIcon(R.drawable.ic_directions)
            .setContentTitle(textTitle)
            .setContentText(textContent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);

    private NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIF_ID)
            .setSmallIcon(R.drawable.ic_directions)
            .setContentTitle(textTitle)
            .setContentText(detailContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true);

    private long wpId;
    private long mapId;
    private double lng;
    private double lat;
    private long next;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null){
            if(intent.hasExtra(EXTRA_WPID)) wpId = intent.getLongExtra(EXTRA_WPID,0);
            if(intent.hasExtra(EXTRA_MAPID)) mapId = intent.getLongExtra(EXTRA_MAPID,0);
            if(intent.hasExtra(EXTRA_WPLAT)) lat = intent.getDoubleExtra(EXTRA_WPLAT,0);
            if(intent.hasExtra(EXTRA_WPLNG)) lng = intent.getDoubleExtra(EXTRA_WPLNG,0);
            next = intent.getLongExtra(EXTRA_NEXT,-1);
            startForeground();
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {

    }

    private void startForeground(){
        builderForeground.setContentIntent(getMapPendingIntent());
        this.startForeground(notif_id, builderForeground.build());
    }


    private PendingIntent getMapPendingIntent(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_ROUTE , mapId);
        return PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /*private PendingIntent getDetailPendingIntent(){
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.ID_EXTRA , wpId);
        TaskStackBuilder taskBuilder = TaskStackBuilder.create(this.getApplicationContext());
        taskBuilder.addNextIntentWithParentStack(intent);
        return taskBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
    }*/

    /*@Override
    public void onLocationChanged(Location location, IMyLocationProvider source) {
        float[] result = new float[1];
        Location.distanceBetween(lat,lng,location.getLatitude(),location.getLongitude(),result);
        float distance = result[0];
        if(distance<threshhold){
            sendDetailNotification();

//            SharedPreferences preferences = getSharedPreferences(MapActivity.PREF_ROUTES, MODE_PRIVATE);
//            preferences.edit().putLong(MapActivity.KEY_NEXTSTEP,next).apply();

            stopSelf();
        }
    }*/




    private void sendDetailNotification(){
        builder.setContentIntent(getMapPendingIntent());
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(notif_id_detail,builder.build());
    }
}
