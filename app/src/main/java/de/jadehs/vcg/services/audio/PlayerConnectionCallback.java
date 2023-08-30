package de.jadehs.vcg.services.audio;

import androidx.media3.session.MediaController;

public interface PlayerConnectionCallback {

    /**
     * is called when a connection to the audio player service is established.
     *
     * @param controller the controller which controls the audio service
     * @deprecated Does behave the same as {@link PlayerConnectionCallback#onSessionAvailable(MediaController)}
     */
    @Deprecated()
    default void connectionEstablished(MediaController controller) {
    }

    /**
     * is called when a connection to the audio player service is established.
     *
     * @param controller the controller which controls the audio service
     */
    void onSessionAvailable(MediaController controller);

    /**
     * is called when a is lost to the service
     */
    void connectionLost();
}
