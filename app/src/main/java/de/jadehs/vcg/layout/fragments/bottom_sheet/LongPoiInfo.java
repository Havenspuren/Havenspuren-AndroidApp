package de.jadehs.vcg.layout.fragments.bottom_sheet;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.models.Media;
import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia;
import de.jadehs.vcg.layout.dialogs.ARInfoDialog;
import de.jadehs.vcg.layout.dialogs.ImageDialog;
import de.jadehs.vcg.layout.fragments.galery.PictureGallery;
import de.jadehs.vcg.services.audio.AudioPlayerManager;
import de.jadehs.vcg.services.audio.AudioPlayerService;
import de.jadehs.vcg.services.audio.PlayerConnectionCallback;
import de.jadehs.vcg.utils.AudioUtils;
import de.jadehs.vcg.utils.data.FileProvider;
import uk.co.deanwild.flowtextview.FlowTextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LongPoiInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LongPoiInfo extends Fragment {

    private static final String TAG = "LongPoiInfo";

    private static final String ARG_WAYPOINT = "de.jadehs.vcg.waypoint";

    private POIWaypointWithMedia waypoint;

    private MediaController controller;
    private AudioPlayerManager audioManager;

    private FlowTextView desc;
    private TextView title;
    private FrameLayout pictureContainer;
    private FloatingActionButton arButton;
    private ImageView characterImage;

    private ViewPager2 pager;

    private PlayerConnectionCallback playerConnectionCallback = new PlayerConnectionCallback() {

        @Override
        public void connectionLost() {
            LongPoiInfo.this.controller = null;
        }

        @Override
        public void onSessionAvailable(MediaController controller) {
            LongPoiInfo.this.controller = controller;
            controller.addListener(new Player.Listener() {

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    if (getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
                        updateButtonImages(isPlaying);
                    }
                }
            });
            updateButtonImages(controller.isPlaying());
        }
    };


    public LongPoiInfo() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param waypoint the waypoint.
     * @return A new instance of fragment LongPoiInfo.
     */
    public static LongPoiInfo newInstance(POIWaypointWithMedia waypoint) {
        LongPoiInfo fragment = new LongPoiInfo();
        Bundle args = new Bundle();
        args.putSerializable(ARG_WAYPOINT, waypoint);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            waypoint = (POIWaypointWithMedia) getArguments().getSerializable(ARG_WAYPOINT);
        }
        audioManager = new AudioPlayerManager();
        audioManager.attachActivity(requireActivity());
        audioManager.listenForConnection(playerConnectionCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_poi_info_long, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = view.findViewById(R.id.text_title);
        desc = view.findViewById(R.id.text_desc);
        characterImage = view.findViewById(R.id.poi_info_character);
        pictureContainer = view.findViewById(R.id.gallery_container);
        arButton = view.findViewById(R.id.waypoint_ar_button);
        fillWaypointInfo(waypoint);
    }


    private long getControllerWaypointId() {
        MediaItem mediaItem = controller.getCurrentMediaItem();
        if (mediaItem == null) {
            return -1;
        }
        return Long.parseLong(mediaItem.mediaId);
    }

    public void updateButtonImages(boolean isPlaying) {
        /*if (isPlaying)
            playButton.setImageResource(R.drawable.ic_pause);
        else
            playButton.setImageResource(R.drawable.ic_play_arrow);*/
    }

    /**
     * does start the {@link AudioPlayerService} with the data of the given waypoint as intent values
     *
     * @param waypoint audio file of this waypoint is played
     */
    public void startAudioService(POIWaypointWithMedia waypoint) {
        audioManager.startPlayback(waypoint);
    }

    private void unbindFromAudioService() {
        audioManager.removeConnectionListener(playerConnectionCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindFromAudioService();
    }


    /**
     * does insert all needed data into the fitting views from the given waypoint
     *
     * @param waypoint source of the data which is displayed
     */
    private void fillWaypointInfo(POIWaypointWithMedia waypoint) {
        title.setText(waypoint.getTitle());
        // toolBar.setTitle(waypoint.getTitle());
        desc.setText(Html.fromHtml(waypoint.getLongDescription() + "\n  "));

        FileProvider fileProvider = new FileProvider(requireActivity().getApplicationContext());

        if (waypoint.getRoute().hasCharacterImage()) {
            this.characterImage.setImageURI(fileProvider.getMediaUri(waypoint.getRoute().getPathToCharacterImage()));
        }

        if (waypoint.getPictures().size() > 0) {
            ArrayList<Uri> mediaUris = new ArrayList<>();
            for (Media p : waypoint.getPictures()) {
                mediaUris.add(fileProvider.getMediaUri(p));
            }

            // TODO change to replace displayed mediaUris, not completely replacing the fragment

            FragmentTransaction transaction = LongPoiInfo.this.getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.gallery_container, PictureGallery.newInstance(mediaUris));
            transaction.commit();
        } else {
            pictureContainer.setVisibility(View.GONE);
        }

        /*if (waypoint.hasAudio()) {
            playButton.setVisibility(View.VISIBLE);
        }else{
            playButton.setVisibility(View.GONE);
        }*/

//        playButton.setOnClickListener(new OnPlayClickHandler(waypoint));
        arButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ARInfoDialog().show(getChildFragmentManager(), null);
            }
        });
        if (controller != null)
            updateButtonImages(controller.isPlaying());
    }


    private class OnPlayClickHandler implements View.OnClickListener {


        private final POIWaypointWithMedia waypoint;

        public OnPlayClickHandler(POIWaypointWithMedia waypoint) {
            this.waypoint = waypoint;
        }

        @Override
        public void onClick(View v) {
            if (isConnected()) {

                if (getControllerWaypointId() == waypoint.getId()) {
                    Log.d(TAG, "onClick: " + controller.getPlaybackState());
                    if (controller.isPlaying()) {
                        controller.pause();
                    } else {
                        controller.play();
                    }
                } else {
                    startAudioService(waypoint);
                }

            } else {
                startAudioService(waypoint);
            }
        }
    }

    private boolean isConnected() {
        return controller != null && controller.isConnected();
    }
}