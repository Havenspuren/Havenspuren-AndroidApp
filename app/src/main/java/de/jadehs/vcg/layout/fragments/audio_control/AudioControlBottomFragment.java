package de.jadehs.vcg.layout.fragments.audio_control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.slider.Slider;

import java.util.ListIterator;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia;
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints;
import de.jadehs.vcg.services.audio.AudioPlayerManager;
import de.jadehs.vcg.services.audio.AudioPlayerService;
import de.jadehs.vcg.services.audio.PlayerConnectionCallback;
import de.jadehs.vcg.utils.AudioUtils;
import de.jadehs.vcg.view_models.RouteViewModel;
import de.jadehs.vcg.view_models.factories.RouteViewModelFactory;

/**
 * Binds to the AudioPlayerService and controls it
 */
public class AudioControlBottomFragment extends Fragment {
    public static final String ROUTE_ID_ARGUMENT = "de.jadehs.audio_controller.session_token";
    private static final String TAG = "AudioControlBottomFragment";

    private final AudioPlayerManager audioPlayerManager = new AudioPlayerManager();
    private final PlayerConnectionCallback playerCallback;
    private final BroadcastReceiver progressReceiver;
    private final MediaControllerCompat.Callback audioCallback;
    private float maxProgress = 100;
    private TextView titleNameView;
    private LocalBroadcastManager broadcastManager;
    private ImageButton nextButton;
    private ImageButton previousButton;
    private ImageButton playButton;
    private MediaControllerCompat controller;
    private AudioPlayerService.AudioServiceBinder audioServiceBinder;
    private Slider progressBar;
    private RouteViewModel viewModel;
    private long routeId;
    /**
     * id of waypoint which is displayed by this bottom sheet
     */
    private long waypointId = -1;
    private TextView trackNumberProgressView;
    private String progressText;

    public static AudioControlBottomFragment getInstance(long routeId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ROUTE_ID_ARGUMENT, routeId);

        AudioControlBottomFragment fragment = new AudioControlBottomFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    public AudioControlBottomFragment() {
        progressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                long playbackPosition = extras.getLong(AudioPlayerService.EXTRA_PLAYBACK_POSITION);
                long playbackLength = extras.getLong(AudioPlayerService.EXTRA_PLAYBACK_LENGTH);


                AudioControlBottomFragment.this.updateProgressBar(playbackPosition, playbackLength);
            }
        };
        playerCallback = new PlayerConnectionCallback() {
            @Override
            public void connectionEstablished(AudioPlayerService.AudioServiceBinder binder) {
                AudioControlBottomFragment.this.audioServiceBinder = binder;
            }

            @Override
            public void onSessionAvailable(MediaControllerCompat controller) {
                AudioControlBottomFragment.this.setController(controller);
                setPlayButtonState(AudioUtils.isPlaying(controller));
            }

            @Override
            public void connectionLost() {
                AudioControlBottomFragment.this.destroyAudioManager();
            }
        };
        audioCallback = new MediaControllerCompat.Callback() {

            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                setPlayButtonState(AudioUtils.isPlaying(state.getState()));
            }

            @Override
            public void onSessionDestroyed() {
                unsubscribeFromController();
            }

            @Override
            public void onSessionReady() {

                MediaMetadataCompat metadata = controller.getMetadata();
                if (metadata.containsKey(AudioPlayerService.EXTRA_WAYPOINT_ID)) {
                    updateTrackInfos(metadata);
                }
            }

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                if (metadata != null && AudioUtils.isPlaying(controller)) {
                    if (metadata.containsKey(AudioPlayerService.EXTRA_WAYPOINT_ID)) {
                        updateTrackInfos(metadata);
                    }
                }
            }

            @Override
            public void binderDied() {
                super.binderDied();
                Log.d(TAG, "binderDied: ");
                audioServiceBinder = null;
            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadcastManager = LocalBroadcastManager.getInstance(requireContext());
        this.progressText = this.requireContext().getString(R.string.progress);

        if (getArguments() != null) {
            if (getArguments().containsKey(ROUTE_ID_ARGUMENT)) {
                this.routeId = getArguments().getLong(ROUTE_ID_ARGUMENT);
            }
        }
        this.viewModel = new ViewModelProvider(
                requireActivity().getViewModelStore(),
                new RouteViewModelFactory(requireActivity().getApplication(), this.routeId)
        ).get(RouteViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audio_control_bottom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupButtons(view, savedInstanceState);
        setupUI(view, savedInstanceState);

        this.registerObserver(view, savedInstanceState);
    }

    private void setupUI(@NonNull View view, @Nullable Bundle savedInstanceState) {
        progressBar = view.findViewById(R.id.audio_control_progress_bar);
        maxProgress = progressBar.getValueTo();
        progressBar.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if (!fromUser || controller == null) {
                    return;
                }
                long duration = controller.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                long newProgress = (long) (value * duration / maxProgress);
                seekTo(newProgress);
            }
        });


        titleNameView = view.findViewById(R.id.audio_control_title_name);
        trackNumberProgressView = view.findViewById(R.id.audio_control_track_number_progress);
    }

    private void setupButtons(@NonNull View view, @Nullable Bundle savedInstanceState) {
        playButton = view.findViewById(R.id.audio_control_play_pause_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlayClick();
            }
        });

        nextButton = view.findViewById(R.id.audio_control_next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextClick();
            }
        });

        previousButton = view.findViewById(R.id.audio_control_previous_button);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPreviousClick();
            }
        });
    }

    private void registerObserver(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.viewModel.getCurrentRoute().observe(this.getViewLifecycleOwner(), new Observer<RouteWithWaypoints>() {
            @Override
            public void onChanged(RouteWithWaypoints routeWithWaypoints) {
                if (routeWithWaypoints == null)
                    return;

                POIWaypointWithMedia toDisplay;
                if (AudioControlBottomFragment.this.waypointId != -1) {
                    toDisplay = routeWithWaypoints.getWaypointById(AudioControlBottomFragment.this.waypointId);
                } else {
                    toDisplay = routeWithWaypoints.getLastUnlocked();
                }
                bindToWaypoint(toDisplay, routeWithWaypoints.getVisitedCount());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        startAudioManager();
        registerTrackProgressReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        destroyAudioManager();
        unregisterTrackProgressReceiver();
    }


    private MediaControllerCompat.Callback getCallback() {
        return audioCallback;
    }

    // set session token
    private void setController(MediaControllerCompat controllerCompat) {
        if (controller != null) {
            destroyAudioManager();
        }
        controller = controllerCompat;
        controller.registerCallback(getCallback());
    }

    private void startAudioManager() {
        this.audioPlayerManager.attachActivity(getActivity());
        this.audioPlayerManager.listenForConnection(this.playerCallback);
    }


    /**
     * does unsubscribe from the controller and hide the card view
     */
    private void destroyAudioManager() {
        Log.d(TAG, "destroyAudioManager: ");
        unsubscribeFromController();
        unsubscribeFromService();
    }


    private void unsubscribeFromController() {
        if (controller != null) {
            controller.unregisterCallback(getCallback());
            controller = null;
        }
    }

    private void unsubscribeFromService() {
        if (audioServiceBinder != null) {
            audioServiceBinder = null;
        }
        if (audioPlayerManager != null) {
            audioPlayerManager.removeConnectionListener(this.playerCallback);
            audioPlayerManager.unattachActivity();
        }
    }


    private void registerTrackProgressReceiver() {
        broadcastManager.registerReceiver(this.progressReceiver, new IntentFilter(AudioPlayerService.PLAYBACK_LOCATION_CHANGED_ACTION));
    }

    private void unregisterTrackProgressReceiver() {
        broadcastManager.unregisterReceiver(this.progressReceiver);
    }


    private void updateTrackInfos(MediaMetadataCompat metadata) {

        if (metadata.containsKey(AudioPlayerService.EXTRA_WAYPOINT_ID)) {
            this.waypointId = metadata.getLong(AudioPlayerService.EXTRA_WAYPOINT_ID);

            RouteWithWaypoints route = this.viewModel.getCurrentRoute().getValue();
            if (route == null) {
                return;
            }

            POIWaypointWithMedia waypoint = route.getWaypointById(this.waypointId);
            int waypointCount = route.getVisitedCount();
            if (waypoint == null) {
                bindToWaypoint(route.getLastUnlocked(), waypointCount);
            } else {
                bindToWaypoint(waypoint, waypointCount);
            }
        }
    }

    /**
     * does change the view to display the information provided by the given waypoint
     *
     * @param waypoint
     */
    private void bindToWaypoint(POIWaypointWithMedia waypoint, int waypointCount) {
        if (waypoint != null) {
            this.titleNameView.setText(waypoint.getTitle());
            this.waypointId = waypoint.getId();
            this.updateTrackNumberProgress(waypoint.getIndexOfRoute(), waypointCount);
        }
    }


    private void toggleVisibility(boolean visible) {
        if (!this.isDetached()) { // otherwise may crashed the UI thread because it performs actions on a already destroyed UI
            Transition s = new Slide(Gravity.BOTTOM);
            s.setDuration(150);

            TransitionManager.beginDelayedTransition((ViewGroup) this.getView(), s);
        }
    }


    private void setPlayButtonState(boolean playing) {
        if (playing)
            playButton.setImageResource(R.drawable.exo_controls_pause);
        else
            playButton.setImageResource(R.drawable.exo_controls_play);
    }

    private void updateProgressBar(long current, long max) {
        final float safeProgress = current * maxProgress / max;
        this.progressBar.setValue(safeProgress);
    }

    /**
     * Does change the track number progress
     *
     * @param current the current track number (starting from 1)
     * @param total   the total amount of tracks
     */
    private void updateTrackNumberProgress(int current, int total) {
        this.trackNumberProgressView.setText(String.format(this.progressText, current, total));
    }

    private void onPlayClick() {
        if (this.audioServiceBinder.isMediaSessionRunning()) {
            this.playPause();
        } else {
            RouteWithWaypoints route = this.viewModel.getCurrentRoute().getValue();
            if (route != null) {
                POIWaypointWithMedia waypoint = route.getWaypointById(this.waypointId);
                if (waypoint != null && waypoint.isVisited()) {
                    this.audioPlayerManager.startPlayback(waypoint);
                }
            }
        }
    }

    private void onNextClick() {
        RouteWithWaypoints route = this.viewModel.getCurrentRoute().getValue();
        if (route == null) {
            return;
        }
        ListIterator<POIWaypointWithMedia> iterator = route.getIteratorAt(this.waypointId);
        if (iterator == null) {
            return;
        }

        POIWaypointWithMedia next = null;
        while (next == null && iterator.hasNext()) {
            POIWaypointWithMedia nextLocal = iterator.next();
            if (nextLocal.isVisited() && nextLocal.hasAudio()) {
                next = nextLocal;
            }
        }

        if (next != null) {
            this.audioPlayerManager.stopPlayback();
            this.bindToWaypoint(next, route.getVisitedCount());
        }
    }

    private void onPreviousClick() {
        RouteWithWaypoints route = this.viewModel.getCurrentRoute().getValue();
        if (route == null) {
            return;
        }
        ListIterator<POIWaypointWithMedia> iterator = route.getIteratorAt(this.waypointId);
        if (iterator == null) {
            return;
        }


        POIWaypointWithMedia previous = null;
        while (previous == null && iterator.hasPrevious()) {
            POIWaypointWithMedia previousLocal = iterator.previous();
            if (previousLocal.isVisited() && previousLocal.hasAudio() && previousLocal.getId() != this.waypointId) {
                previous = previousLocal;
            }
        }

        if (previous != null) {
            this.audioPlayerManager.stopPlayback();
            this.bindToWaypoint(previous, route.getVisitedCount());
        }
    }


    /**
     * Does toggle the playstate of the current MediaSession
     */
    private void playPause() {
        if (controller == null) {
            return;
        }
        MediaControllerCompat.TransportControls controls = controller.getTransportControls();
        if (AudioUtils.isPlaying(controller)) {
            controls.pause();
        } else {
            controls.play();
        }
    }

    /**
     * jumps to the currently playing audio
     *
     * @param milliseconds
     */
    private void seekTo(long milliseconds) {
        if (controller == null) {
            return;
        }
        controller.getTransportControls().seekTo(milliseconds);
    }
}