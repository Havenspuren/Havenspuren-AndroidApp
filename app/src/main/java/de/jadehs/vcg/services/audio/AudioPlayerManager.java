package de.jadehs.vcg.services.audio;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.media.session.MediaControllerCompat;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.LinkedList;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia;
import de.jadehs.vcg.utils.data.FileProvider;
import kotlin.NotImplementedError;

public class AudioPlayerManager {
    private static final String TAG = "AudioPlayerManager";
    private final AudioPlayerActivityLifecycleObserver lifecycleCallback = new AudioPlayerActivityLifecycleObserver();
    private FragmentActivity activtiy;
    private AudioPlayerService.AudioServiceBinder binder;
    private ServiceConnection connection;
    private MediaControllerCompat controller;
    private LinkedList<PlayerConnectionCallback> playerConnectionCallbacks = new LinkedList<>();
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver playerStartedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isBound()) {
                controller = binder.getSessionController();
                callSessionAvailableCallback();
            }
        }
    };


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
        broadcastManager.registerReceiver(playerStartedReceiver, getPlayerStartedIntentFilter());
    }

    public void unattachActivity() {
        if (this.activtiy != null) {
            this.activtiy.getLifecycle().removeObserver(lifecycleCallback);
            unBindFromService();
            this.broadcastManager.unregisterReceiver(playerStartedReceiver);
            this.broadcastManager = null;
            this.activtiy = null;
        }
    }


    public void startPlayback(POIWaypointWithMedia waypoint) {
        if (isAttached()) {
            bindToService();
            startService(waypoint);
        } else {
            throw new IllegalStateException("No activity is attached to this Manager");
        }
    }

    public void stopPlayback() {
        if (isAttached()) {
            if (isBound()) {
                controller = this.binder.getSessionController();
                if (controller != null) {
                    controller.getTransportControls().stop();
                }
            }
            this.activtiy.stopService(getServiceIntent());

        }
    }

    /**
     * does start the service the the given metadata of the given waypoint
     *
     * @param waypoint waypoint, which is used retrieve the audio file
     */
    private void startService(POIWaypointWithMedia waypoint) {
        if (waypoint != null) {
            FileProvider provider = new FileProvider(this.activtiy.getApplicationContext());


            Intent intent = getServiceIntent();
            intent.putExtra(AudioPlayerService.EXTRA_CONTENT_TITLE, waypoint.getTitle());
            intent.putExtra(AudioPlayerService.EXTRA_DESCRIPTION, this.activtiy.getString(R.string.audio_player_waypoint_description));
            if (waypoint.getPictures().size() > 0) {
                intent.putExtra(AudioPlayerService.EXTRA_AUDIO_PICTURE, provider.getMediaFile(waypoint.getPictures().get(0).getPathToFile()).getAbsolutePath());
            }
            if (waypoint.hasAudio()) {
                intent.putExtra(AudioPlayerService.EXTRA_AUDIO_FILE, provider.getMediaFile(waypoint.getAudio().getPathToFile()).getAbsolutePath());
            }

            intent.putExtra(AudioPlayerService.EXTRA_WAYPOINT_ID, waypoint.getId());

            // start service to start playback
            this.activtiy.startService(intent);
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
            if (isBound()) {
                callback.connectionEstablished(binder);
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
            callback.connectionEstablished(binder);
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
            if (isAttached() && !isBound()) {
                connection = new AudioPlayerConnection();
                this.activtiy.bindService(getServiceIntent(), connection, Activity.BIND_AUTO_CREATE);

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
        if (!isBound() && playerConnectionCallbacks.size() > 0) {
            bindToService();
        }
    }

    private void unBindFromService() {
        callDisconnectedCallback();
        if (connection != null) {
            if (isAttached()) {
                this.activtiy.unbindService(connection);
            }
            connection = null;
            binder = null;
        }
    }

    private boolean isBound() {
        return binder != null && binder.isBinderAlive();
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


    private class AudioPlayerActivityLifecycleObserver implements LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onActivityDestroyed() {
            AudioPlayerManager.this.unattachActivity();
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onActivityStopped() {
            unBindFromService();
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        public void OnActivityResumed() {
            bindToServiceIfNeeded();
        }
    }

    private class AudioPlayerConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (componentName.compareTo(new ComponentName(activtiy.getBaseContext(), AudioPlayerService.class)) == 0) {
                binder = (AudioPlayerService.AudioServiceBinder) iBinder;
            }
            callConnectedCallback();
            controller = binder.getSessionController();
            if (controller != null) {
                callSessionAvailableCallback();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            unBindFromService();
        }

        @Override
        public void onNullBinding(ComponentName name) {
            throw new NotImplementedError("Service needs to return a binder");
        }

        @Override
        public void onBindingDied(ComponentName name) {
            connection = null;
        }
    }
}
