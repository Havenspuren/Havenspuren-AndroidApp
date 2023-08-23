package de.jadehs.vcg.services.audio;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

public class AudioPlayerService extends MediaSessionService implements MediaSession.Callback {

    /**
     * Used as an string extra field in AudioPlayerService intents to tell the application being invoked which title the current track has.
     */
    public static final String EXTRA_CONTENT_TITLE = "de.jadehs.vcg.extra_title";
    /**
     * Used as an string extra field in AudioPlayerService intents to tell the application being invoked which description the current track has.
     */
    public static final String EXTRA_DESCRIPTION = "de.jadehs.vcg.extra_description";


    // INTENT EXTRAS
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
    /**
     * Broadcast which is send, when playback started.
     */
    public static final String PLAYBACK_STARTED_ACTION = "de.jadehs.vcg.playback_started_action";
    /**
     * Broadcast which is send, when the playback location of the playback changed
     */
    public static final String PLAYBACK_LOCATION_CHANGED_ACTION = "de.jadehs.vcg.playback_location_changed_action";


    // Broadcasts
    public static final String EXTRA_PLAYBACK_POSITION = "de.jadehs.vcg.playback_location";
    public static final String EXTRA_PLAYBACK_LENGTH = "de.jadehs.vcg.playback_length";
    /**
     * the interval which is used to send the PLAYBACK_LOCATION_CHANGED_ACTION broadcast
     */
    public static final long PLAYBACK_LOCATION_UPDATE_INTERVAL = 300;
    private static final String TAG = "AudioPlayerService";
    // NOTIFICATION ID
    private static final String PLAYER_CHANNEL = "de.jadehs.vcg.audio_player";
    private MediaSession mediaSession;

    @Override
    public void onCreate() {
        super.onCreate();
        ExoPlayer player = new ExoPlayer.Builder(this).build();
        mediaSession = new MediaSession.Builder(this, player)
                .setCallback(this)
                .build();
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        int r = super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand: ");
        return r;
    }

    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        Log.d(TAG, "onGetSession: " + controllerInfo.getPackageName());

        return mediaSession;
    }

    @Override
    public ListenableFuture<MediaSession.MediaItemsWithStartPosition> onPlaybackResumption(MediaSession mediaSession, MediaSession.ControllerInfo controller) {
        return Futures.immediateFuture(new MediaSession.MediaItemsWithStartPosition(ImmutableList.of(), C.INDEX_UNSET, C.TIME_UNSET));
    }

    @Override
    public MediaSession.ConnectionResult onConnect(MediaSession session, MediaSession.ControllerInfo controller) {
        Log.d(TAG, "onConnect: ");
        return MediaSession.Callback.super.onConnect(session, controller);
    }

    @Override
    public void onDestroy() {
        mediaSession.getPlayer().release();
        mediaSession.release();
        mediaSession = null;
        super.onDestroy();

    }
}
