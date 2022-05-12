package de.jadehs.vcg.services.audio;

import android.support.v4.media.session.MediaControllerCompat;

public interface PlayerConnectionCallback {

    void connectionEstablished(AudioPlayerService.AudioServiceBinder binder);

    void onSessionAvailable(MediaControllerCompat controller);

    void connectionLost();
}
