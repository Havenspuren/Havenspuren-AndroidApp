package de.jadehs.vcg.services.audio;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia;
import de.jadehs.vcg.utils.data.FileProvider;

public class AudioPlayerManager {
    private static final String TAG = "AudioPlayerManager";
    private final AudioPlayerActivityLifecycleObserver lifecycleCallback = new AudioPlayerActivityLifecycleObserver();
    private FragmentActivity activtiy;
    private MediaController controller;
    private LinkedList<PlayerConnectionCallback> playerConnectionCallbacks = new LinkedList<>();
    private LocalBroadcastManager broadcastManager;
    private ListenableFuture<MediaController> controllerFuture;


    public void attachActivity(FragmentActivity activity) {
        if (this.activtiy != null) {
            throw new IllegalStateException("An activity is already bound to this AudioPlayerManager");
        }
        if (activity == null) {
            throw new IllegalArgumentException("Can not attach to null activity");
        }

        this.activtiy = activity;
        this.activtiy.getLifecycle().addObserver(lifecycleCallback);
        broadcastManager = LocalBroadcastManager.getInstance(activtiy.getApplicationContext());
        // broadcastManager.registerReceiver(playerStartedReceiver, getPlayerStartedIntentFilter());
    }

    public void unattachActivity() {
        unBindFromService();
        if (broadcastManager != null) {
            // this.broadcastManager.unregisterReceiver(playerStartedReceiver);
            this.broadcastManager = null;
        }
        if (this.activtiy != null) {
            this.activtiy.getLifecycle().removeObserver(lifecycleCallback);
            this.activtiy = null;
        }
    }


    public void startPlayback(POIWaypointWithMedia waypoint) {
        Log.d(TAG, "startPlayback: with " + waypoint.getTitle());
        if (isAttached()) {
            bindToService();
            this.listenForConnection(new PlayerConnectionCallback() {

                @Override
                public void onSessionAvailable(MediaController controller) {
                    startService(waypoint);
                    removeConnectionListener(this);
                }

                @Override
                public void connectionLost() {

                }
            });
        } else {
            throw new IllegalStateException("No activity is attached to this Manager");
        }
    }

    public void stopPlayback() {
        if (isAttached()) {
            if (isConnected()) {
                controller.stop();
            }
        }
    }

    /**
     * does start the service the the given metadata of the given waypoint
     *
     * @param waypoint waypoint, which is used retrieve the audio file
     */
    private void startService(POIWaypointWithMedia waypoint) {
        if (!isConnected()) {
            Log.w(TAG, "Tried starting a playback when manager wasn't connected");
            return;
        }
        if (waypoint != null) {
            FileProvider provider = new FileProvider(this.activtiy.getApplicationContext());


            MediaItem.Builder mediaItem = new MediaItem.Builder();
            MediaMetadata.Builder mediaMetadata = new MediaMetadata.Builder();
            mediaMetadata.setTitle(waypoint.getTitle());
            mediaMetadata.setDescription(this.activtiy.getString(R.string.audio_player_waypoint_description));
            if (waypoint.getPictures().size() > 0) {
                mediaMetadata.setArtworkUri(provider.getMediaUri(waypoint.getPictures().get(0).getPathToFile()));
            }
            if (waypoint.hasAudio()) {
                mediaItem.setUri(provider.getMediaUri(waypoint.getAudio().getPathToFile()));
            }

            mediaItem.setMediaId(Long.toString(waypoint.getId()));

            // start service to start playback
            mediaItem.setMediaMetadata(mediaMetadata.build());
            Log.d(TAG, "startService: started play media");
            controller.addMediaItem(mediaItem.build());
            controller.prepare();
            controller.play();
        }
    }

    /**
     * Does register the given callback
     *
     * @param callback
     */
    public void listenForConnection(PlayerConnectionCallback callback) {
        if (callback != null && !playerConnectionCallbacks.contains(callback)) {
            playerConnectionCallbacks.add(callback);
            if (isConnected()) {
                callback.connectionEstablished(controller);
                if (controller != null) {
                    callback.onSessionAvailable(controller);
                }
            } else {
                bindToService();
            }
        }
    }

    /**
     * removes the given callback
     *
     * @param callback callback which will get removed
     */
    public void removeConnectionListener(PlayerConnectionCallback callback) {
        if (callback != null) {
            playerConnectionCallbacks.remove(callback);
        }
    }

    /**
     * calls the connectionEstablished function of every registered listener
     */
    private void callConnectedCallback() {
        for (PlayerConnectionCallback callback : playerConnectionCallbacks) {
            callback.connectionEstablished(controller);
        }
    }

    /**
     * calls the connectionLost function of every registered listener
     */
    private void callDisconnectedCallback() {
        for (PlayerConnectionCallback callback : playerConnectionCallbacks) {
            callback.connectionLost();
        }
    }

    private void callSessionAvailableCallback() {
        for (PlayerConnectionCallback callback : playerConnectionCallbacks) {
            callback.onSessionAvailable(controller);
        }
    }

    private void bindToService() {
        if (this.activtiy.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            if (isAttached() && !isConnected()) {

                this.controllerFuture = new MediaController.Builder(activtiy,
                        new SessionToken(activtiy,
                                new ComponentName(activtiy.getBaseContext(),
                                        AudioPlayerService.class)))
                        .buildAsync();
                this.controllerFuture.addListener(() -> {
                    try {
                        this.controller = this.controllerFuture.get();
                        callConnectedCallback();
                        callSessionAvailableCallback();
                    } catch (ExecutionException | InterruptedException e) {
                        callDisconnectedCallback();

                    }
                }, ContextCompat.getMainExecutor(activtiy));

            }/* Don't know if this is even needed
            else {
                throw new IllegalStateException("Is already bound to service or not already attached to activity");
            }*/
        }

    }

    /**
     * binds to service if at least one ConnectionCallback is registered
     */
    private void bindToServiceIfNeeded() {
        if (!isConnected() && playerConnectionCallbacks.size() > 0) {
            bindToService();
        }
    }

    private void unBindFromService() {
        callDisconnectedCallback();
        if (controllerFuture != null) {
            MediaController.releaseFuture(controllerFuture);
        }
        if (controller != null) {
            controller.release();
        }
    }

    private boolean isConnected() {
        return controller != null && controller.isConnected();
    }

    private boolean isAttached() {
        return activtiy != null && activtiy.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
    }

    private Intent getServiceIntent() {
        if (isAttached()) {
            return new Intent(this.activtiy, AudioPlayerService.class);
        } else {
            throw new IllegalStateException("Activity needs to be attached");
        }
    }

    private IntentFilter getPlayerStartedIntentFilter() {
        return new IntentFilter(AudioPlayerService.PLAYBACK_STARTED_ACTION);
    }


    private class AudioPlayerActivityLifecycleObserver implements DefaultLifecycleObserver {

        @Override
        public void onDestroy(@NonNull LifecycleOwner owner) {
            AudioPlayerManager.this.unattachActivity();
        }

        @Override
        public void onStart(@NonNull LifecycleOwner owner) {
            bindToServiceIfNeeded();
        }

        @Override
        public void onStop(@NonNull LifecycleOwner owner) {
            unBindFromService();
        }
    }
}
