package de.jadehs.vcg.services.audio;

import androidx.annotation.Nullable;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

public class AudioPlayerService extends MediaSessionService implements MediaSession.Callback {
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

    private static int[] DISABLED_COMMANDS = {Player.COMMAND_SET_SHUFFLE_MODE, Player.COMMAND_SET_REPEAT_MODE};
    private static int[] EXTERNAL_DISABLED_COMMANDS = {Player.COMMAND_SET_MEDIA_ITEM, Player.COMMAND_CHANGE_MEDIA_ITEMS};
    private MediaSession mediaSession;

    @Override
    public void onCreate() {
        super.onCreate();
        ExoPlayer player = new ExoPlayer.Builder(this).build();
        mediaSession = new MediaSession.Builder(this, player)
                .setCallback(this)
                .build();
    }

    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public MediaSession.ConnectionResult onConnect(MediaSession session, MediaSession.ControllerInfo controller) {

        MediaSession.ConnectionResult result = MediaSession.Callback.super.onConnect(session, controller);
        Player.Commands.Builder commandBuilder = result.availablePlayerCommands.buildUpon()
                .removeAll(DISABLED_COMMANDS);
        if (!controller.getPackageName().startsWith(getBaseContext().getPackageName())) {
            commandBuilder.removeAll(EXTERNAL_DISABLED_COMMANDS);
        }

        return MediaSession.ConnectionResult.accept(result.availableSessionCommands, commandBuilder.build());
    }

    @Override
    public void onDestroy() {
        mediaSession.getPlayer().release();
        mediaSession.release();
        mediaSession = null;
        super.onDestroy();

    }
}
