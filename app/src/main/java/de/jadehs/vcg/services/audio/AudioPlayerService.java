package de.jadehs.vcg.services.audio;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import de.jadehs.vcg.R;
import de.jadehs.vcg.layout.activities.MainActivity;
import de.jadehs.vcg.utils.AudioUtils;
import de.jadehs.vcg.utils.ImageUtils;

/**
 * Service which will play the file which is provided as extra. EXTRA_AUDIO_FILE is needed!
 * <p>
 * Behaviour:
 * <p>
 * - Notification only disposable while the player isn't playing
 * <p>
 * Disposal:
 * - Stays until the user dismisses the notification OR
 * - the player is paused and the corresponding Task is dismissed OR
 * - the track finished playing
 */
public class AudioPlayerService extends Service {

    private static final String TAG = "AudioPlayerService";


    // NOTIFICATION ID
    private static final String PLAYER_CHANNEL = "de.jadehs.vcg.audio_player";


    // INTENT EXTRAS
    /**
     * Used as an string extra field in AudioPlayerService intents to tell the application being invoked which title the current track has.
     */
    public static final String EXTRA_CONTENT_TITLE = "de.jadehs.vcg.extra_title";
    /**
     * Used as an string extra field in AudioPlayerService intents to tell the application being invoked which description the current track has.
     */
    public static final String EXTRA_DESCRIPTION = "de.jadehs.vcg.extra_description";
    /**
     * Used as an string extra field in AudioPlayerService intents to tell the application being invoked where the audio file is located. This extra is needed to start the service
     */
    public static final String EXTRA_AUDIO_FILE = "de.jadehs.vcg.extra_uri_to_file";

    /**
     * Used as an string extra field in AudioPlayerService intents to tell the application being invoked which picture to display in the notification.
     */
    public static final String EXTRA_AUDIO_PICTURE = "de.jadehs.vcg.extra_uri_to_picture";
    /**
     * Used as an string extra field in AudioPlayerService intents to tell the application being invoked which picture to display in the notification.
     */
    public static final String EXTRA_WAYPOINT_ID = "de.jadehs.vcg.extra_waypoint_id";


    // Broadcasts

    /**
     * Broadcast which is send, when playback started.
     */
    public static final String PLAYBACK_STARTED_ACTION = "de.jadehs.vcg.playback_started_action";

    /**
     * Broadcast which is send, when the playback location of the playback changed
     */
    public static final String PLAYBACK_LOCATION_CHANGED_ACTION = "de.jadehs.vcg.playback_location_changed_action";

    public static final String EXTRA_PLAYBACK_POSITION = "de.jadehs.vcg.playback_location";

    public static final String EXTRA_PLAYBACK_LENGTH = "de.jadehs.vcg.playback_length";

    /**
     * the interval which is used to send the PLAYBACK_LOCATION_CHANGED_ACTION broadcast
     */
    public static final long PLAYBACK_LOCATION_UPDATE_INTERVAL = 300;


    private SimpleExoPlayer player;
    private PlayerNotificationManager manager;
    private MediaSessionCompat mediaSessionCompat;
    private MediaSessionConnector connector;
    private MediaDescriptor descriptor;


    private final Binder binder = new AudioServiceBinder();
    private DefaultDataSourceFactory dataSourceFactory;
    /**
     * id of the waypoint which was last passed with the onStartCommand intent, if no id was passed, the value is -1
     */
    private long waypointId = -1;
    private boolean isLocationUpdatesRunning = false;
    private Runnable locationUpdateCallback;
    private Handler handler;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setupNotificationManager();

        dataSourceFactory = new DefaultDataSourceFactory(
                getApplicationContext(),
                Util.getUserAgent(
                        getApplicationContext(),
                        getString(R.string.app_name)));
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {


        if (intent == null || !intent.hasExtra(EXTRA_AUDIO_FILE)) {
            // Log.e(TAG, "onStartCommand: test error", new IllegalArgumentException("Service, cannot be started, the uri to the audio file is missing"));

            Log.e(TAG, "AudioPlayerService onStart: no audio path was given", new IllegalArgumentException("Service, cannot be started, the absolute path of the audio file is missing"));

            stopAudioService();
            return START_NOT_STICKY;
        }


        setupPlayer();


        setupMediaSession();
        setupSessionConnector();

        connectManagerToPlayerAndSession();

        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        broadcastManager.sendBroadcast(getStartedPlaybackIntent());


        waypointId = intent.getLongExtra(EXTRA_WAYPOINT_ID, -1);

        //Debug audioSource
        //MediaSource audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(RawResourceDataSource.buildRawResourceUri(R.raw.sound));

        String filePath = intent.getStringExtra(EXTRA_AUDIO_FILE);
        Runnable throwMissingAudio = new Runnable() {
            @Override
            public void run() {
                stopAudioService();
                Log.e(TAG, "onStartCommand: Audio file is missing or doesn't exist");
            }
        };
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                MediaSource audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.fromFile(file));
                player.prepare(audioSource);
                mediaSessionCompat.setActive(true);
            } else {
                throwMissingAudio.run();
            }
        } else {
            throwMissingAudio.run();
        }

        descriptor.setExtras(intent.getExtras());


        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (!player.getPlayWhenReady()) {
            stopAudioService();
        }
    }

    private void setupPlayer() {

        if (player != null) {
            destroyPlayer();
        }
        player = new SimpleExoPlayer.Builder(getBaseContext()).build();
        handler = new Handler(player.getApplicationLooper());


        player.addListener(new Player.EventListener() {

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    if (playWhenReady && !isLocationUpdatesRunning) {
                        startLocationUpdates();
                    }

                    if (!playWhenReady && isLocationUpdatesRunning) {
                        stopLocationUpdates();
                    }

                }

                if (Player.STATE_ENDED == playbackState) {
                    stopAudioService();
                }

            }
        });


        player.setPlayWhenReady(true);
        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(C.CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                true);
    }

    /**
     * does stop the player and the media session
     */
    private void destroyPlayer() {
        if (player != null) {
            if (handler != null)
                handler.removeCallbacksAndMessages(null);
            Player p = player;
            player = null;
            p.stop();
            destroyMediaSession();
            p.release();
        }
    }

    private void startLocationUpdates() {
        if (!isLocationUpdatesRunning) {
            isLocationUpdatesRunning = true;
            if(locationUpdateCallback == null){
                locationUpdateCallback = new Runnable() {
                    private final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

                    @Override
                    public void run() {
                        if (player != null && AudioUtils.isPlayingExo(player.getPlaybackState())) {
                            Intent intent = new Intent(AudioPlayerService.PLAYBACK_LOCATION_CHANGED_ACTION);
                            intent.putExtra(AudioPlayerService.EXTRA_PLAYBACK_POSITION, player.getCurrentPosition());
                            intent.putExtra(AudioPlayerService.EXTRA_PLAYBACK_LENGTH, player.getDuration());

                            broadcastManager.sendBroadcast(intent);

                            handler.postDelayed(this, PLAYBACK_LOCATION_UPDATE_INTERVAL);
                        } else {
                            isLocationUpdatesRunning = false;
                        }
                    }
                };
            }

            handler.postDelayed(locationUpdateCallback, PLAYBACK_LOCATION_UPDATE_INTERVAL);
        }

    }

    private void stopLocationUpdates() {
        handler.removeCallbacks(locationUpdateCallback);
        isLocationUpdatesRunning = false;
    }


    private void setupNotificationManager() {
        descriptor = new MediaDescriptor(new Bundle());
        manager = PlayerNotificationManager.createWithNotificationChannel(
                getBaseContext(),
                PLAYER_CHANNEL,
                R.string.audio_player_channel,
                R.string.audio_player_channel_description,
                100,
                descriptor,
                new PlayerNotificationManager.NotificationListener() {

                    @Override
                    public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                        stopForeground(true);
                        stopAudioService();
                    }

                    @Override
                    public void onNotificationPosted(int notificationId, @NotNull Notification notification, boolean ongoing) {
                        Log.d(TAG, "onNotificationPosted: " + ongoing);
                        if (ongoing) {
                            startForeground(notificationId, notification);
                        } else {
                            stopForeground(false);
                        }
                    }
                });

        manager.setUseChronometer(true);

        manager.setUseNavigationActions(false);
        manager.setUseNavigationActionsInCompactView(false);

        // 10s
        manager.setRewindIncrementMs(10000);
        // 10s
        manager.setFastForwardIncrementMs(10000);
    }

    private void connectManagerToPlayerAndSession() {
        if (player != null && mediaSessionCompat != null) {
            manager.setPlayer(player);
            manager.setMediaSessionToken(mediaSessionCompat.getSessionToken());
        }
    }

    private void disconnectManagerFromPlayerAndSession() {
        if (manager != null) {
            manager.setPlayer(null);
            manager.setMediaSessionToken(null);
        }

    }

    private void setupMediaSession() {
        mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), TAG);
        mediaSessionCompat.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );
    }

    private void destroyMediaSession() {
        destroySessionConnector();
        disconnectManagerFromPlayerAndSession();
        if (mediaSessionCompat != null) {
            mediaSessionCompat.release();
            mediaSessionCompat = null;
        }

    }

    private void setupSessionConnector() {
        connector = new MediaSessionConnector(mediaSessionCompat);
        connector.setPlayer(player);
        connector.setMediaMetadataProvider(descriptor);
    }

    private void destroySessionConnector() {
        if (connector != null) {
            connector.setPlayer(null);
            connector.setMediaMetadataProvider(null);
            connector = null;
        }

    }

    private void stopAudioService() {
        stopLocationUpdates();
        destroyPlayer();
        stopSelf();
    }

    private Intent getStartedPlaybackIntent() {
        return new Intent(PLAYBACK_STARTED_ACTION);
    }


    @Override
    public void onDestroy() {
        destroyPlayer();
        super.onDestroy();
    }

    private class MediaDescriptor implements PlayerNotificationManager.MediaDescriptionAdapter, MediaSessionConnector.MediaMetadataProvider {

        private final Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
        private String title;
        private String description;
        private Bitmap picture;
        private MediaMetadataCompat.Builder metadata;

        public MediaDescriptor(Bundle extras) {
            setExtras(extras);
        }


        public void setExtras(Bundle extras) {
            if (extras == null)
                extras = new Bundle();


            title = extras.getString(EXTRA_CONTENT_TITLE, "MISSING TITLE");

            description = extras.getString(EXTRA_DESCRIPTION, "");

            String path = extras.getString(EXTRA_AUDIO_PICTURE, null);
            if (path != null) picture = ImageUtils.getImageOfBounds(new File(path), 100, 100);


            metadata = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.getTitle())
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, this.getDescription())
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, this.getPicture());

            if (waypointId != -1) {
                metadata.putLong(EXTRA_WAYPOINT_ID, waypointId);
            }


        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public Bitmap getPicture() {
            return picture;
        }

        public Intent getMainIntent() {
            return mainIntent;
        }

        @NonNull
        @Override
        public CharSequence getCurrentContentTitle(@NotNull Player player) {
            return getTitle();

        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(@NonNull Player player) {
            return PendingIntent.getActivity(getBaseContext(), 0, getMainIntent(), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        @Nullable
        @Override
        public CharSequence getCurrentContentText(@NonNull Player player) {
            return getDescription();
        }


        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(@NonNull Player player, PlayerNotificationManager.BitmapCallback callback) {
            return getPicture();
        }

        @NonNull
        @Override
        public MediaMetadataCompat getMetadata(@NonNull Player player) {
            metadata.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player.getDuration());
            return metadata.build();
        }
    }


    public class AudioServiceBinder extends Binder {

        public MediaSessionCompat.Token getSessionToken() {
            if (isMediaSessionRunning()) {
                return mediaSessionCompat.getSessionToken();
            } else {
                return null;
            }
        }

        public MediaControllerCompat getSessionController() {
            if (isMediaSessionRunning()) {
                return mediaSessionCompat.getController();
            } else {
                return null;
            }
        }

        public long getWaypointId() {
            return waypointId;
        }

        public boolean isMediaSessionRunning() {
            return mediaSessionCompat != null;
        }

    }
}
