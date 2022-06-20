package de.jadehs.vcg.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavDeepLinkBuilder;

import de.jadehs.vcg.R;
import de.jadehs.vcg.layout.fragments.trophies.TrophyMapFragment;

public class TrophyBroadcastReceiver extends BroadcastReceiver {

    public static final String TROPHY_UNLOCKED_ACTION = "de.jadehs.vcg.TROPHY_UNLOCKED";

    public static final String EXTRA_ROUTE_ID = TrophyMapFragment.ROUTE_ID_ARGUMENT;

    public static final String NOTIFICATION_CHANNEL = "de.jadehs.vcg.TROPHY_UNLOCKED_CHANNEL";


    public static Intent createIntent(Context context, long routeId, String routeName, String mapPath) {
        Intent i = new Intent(context, TrophyBroadcastReceiver.class);
        i.setAction(TROPHY_UNLOCKED_ACTION);
        Bundle trophyMapBundle = TrophyMapFragment.createArguments(routeId,routeName, mapPath);
        i.putExtras(trophyMapBundle);

        return i;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(TROPHY_UNLOCKED_ACTION)
                && intent.hasExtra(EXTRA_ROUTE_ID)) {
            Bundle b = intent.getExtras();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
                    .setSmallIcon(R.drawable.ic_trophy)
                    .setContentTitle(context.getResources().getString(R.string.trophy_unlocked_notification_title))
                    .setContentText(context.getResources().getString(R.string.trophy_unlocked_notification_desc))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(
                            new NavDeepLinkBuilder(context)
                                    .setGraph(R.navigation.main_navigation_graph)
                                    .setDestination(R.id.destination_trophy_map)
                                    .setArguments(b)
                                    .createPendingIntent()
                    )
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

// notificationId is a unique int for each notification that you must define
            notificationManager.notify(0, builder.build());

        }

    }
}
