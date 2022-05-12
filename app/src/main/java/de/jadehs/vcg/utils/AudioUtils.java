package de.jadehs.vcg.utils;

import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.exoplayer2.Player;

public class AudioUtils {


    public static boolean isPlaying(@PlaybackStateCompat.State int state) {
        return state == PlaybackStateCompat.STATE_PLAYING;
    }

    public static boolean isPlaying(MediaControllerCompat controller) {
        if(controller != null){
            int state = controller.getPlaybackState().getState();
            return isPlaying(state);
        }
        return false;

    }

    public static boolean isPlayingExo(@Player.State int state) {
        return state != Player.STATE_IDLE && state != Player.STATE_ENDED;
    }
}
