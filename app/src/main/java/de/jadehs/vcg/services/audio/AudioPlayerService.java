package de.jadehs.vcg.services.audio;

import android.content.Intent;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import de.jadehs.vcg.utils.AudioUtils;

public class AudioPlayerService extends MediaSessionService implements MediaSession.Callback, Player.Listener {
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

    private static final int[] DISABLED_COMMANDS = {Player.COMMAND_SET_SHUFFLE_MODE, Player.COMMAND_SET_REPEAT_MODE};
    private static final int[] EXTERNAL_DISABLED_COMMANDS = {Player.COMMAND_SET_MEDIA_ITEM, Player.COMMAND_CHANGE_MEDIA_ITEMS};
    private MediaSession mediaSession;
    private Handler handler;
    private boolean isLocationUpdatesRunning;
    private Runnable locationUpdateCallback;
    private LocalBroadcastManager broadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        ExoPlayer player = new ExoPlayer.Builder(this).build();
        mediaSession = new MediaSession.Builder(this, player)
                .setCallback(this)
                .build();
        handler = new Handler(player.getApplicationLooper());
        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        player.addListener(this);
    }

    @Nullable
    @Override
    public MediaSession onGetSession(@NonNull final MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @NonNull
    @Override
    public MediaSession.ConnectionResult onConnect(@NonNull final MediaSession session,
                                                   @NonNull final MediaSession.ControllerInfo controller) {

        MediaSession.ConnectionResult result = MediaSession.Callback.super.onConnect(session, controller);
        Player.Commands.Builder commandBuilder = result.availablePlayerCommands.buildUpon()
                .removeAll(DISABLED_COMMANDS);
        if (!controller.getPackageName().startsWith(getBaseContext().getPackageName())) {
            commandBuilder.removeAll(EXTERNAL_DISABLED_COMMANDS);
        }

        return MediaSession.ConnectionResult.accept(result.availableSessionCommands, commandBuilder.build());
    }


    private void startLocationUpdates() {
        if (!isLocationUpdatesRunning) {
            isLocationUpdatesRunning = true;
            if (locationUpdateCallback == null) {
                locationUpdateCallback = new Runnable() {
                    @Override
                    public void run() {
                        if (mediaSession.getPlayer().isPlaying()) {
                            Intent intent = new Intent(AudioPlayerService.PLAYBACK_LOCATION_CHANGED_ACTION);
                            intent.putExtra(AudioPlayerService.EXTRA_PLAYBACK_POSITION, mediaSession.getPlayer().getCurrentPosition());
                            intent.putExtra(AudioPlayerService.EXTRA_PLAYBACK_LENGTH, mediaSession.getPlayer().getDuration());

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

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        mediaSession.getPlayer().release();
        mediaSession.release();
        mediaSession = null;
        super.onDestroy();

    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
            if (isLocationUpdatesRunning) {
                stopLocationUpdates();
            }
        } else {
            if (!isLocationUpdatesRunning) {
                startLocationUpdates();
            }
        }
    }
}
