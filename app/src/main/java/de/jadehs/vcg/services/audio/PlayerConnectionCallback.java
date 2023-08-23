package de.jadehs.vcg.services.audio;

import android.support.v4.media.session.MediaControllerCompat;

import androidx.media3.session.MediaController;

public interface PlayerConnectionCallback {

    @Deprecated
    default void connectionEstablished(MediaController controller) {
    }

    void onSessionAvailable(MediaController controller);

    void connectionLost();
}
