package de.jadehs.vcg.layout.fragments.bottom_sheet;

import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.List;

import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia;
import de.jadehs.vcg.databinding.FragmentPoiInfoChatLongBinding;
import de.jadehs.vcg.services.audio.AudioPlayerManager;
import de.jadehs.vcg.services.audio.AudioPlayerService;
import de.jadehs.vcg.services.audio.PlayerConnectionCallback;
import de.jadehs.vcg.utils.MessagesParser;
import de.jadehs.vcg.utils.data.FileProvider;

public class LongChatPoiInfo extends Fragment {

    private static final String TAG = "LongChatPoiInfo";

    private static final String ARG_WAYPOINT = "de.jadehs.vcg.waypoint";
    private POIWaypointWithMedia waypoint;
    private AudioPlayerManager audioManager;
    private FragmentPoiInfoChatLongBinding binding;
    private MessagesListAdapter<IMessage> adapter;
    private AudioPlayerService.AudioServiceBinder audioBinder;
    private MediaControllerCompat audioController;
    private PlayerConnectionCallback playerConnectionCallback = new PlayerConnectionCallback() {
        @Override
        public void connectionEstablished(AudioPlayerService.AudioServiceBinder binder) {
            audioBinder = binder;
        }

        @Override
        public void onSessionAvailable(MediaControllerCompat controller) {
            audioController = controller;
        }

        @Override
        public void connectionLost() {
            audioBinder = null;
            audioController = null;
        }
    };
    private FileProvider fileProvider;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param waypoint the waypoint.
     * @return A new instance of fragment LongPoiInfo.
     */
    public static LongChatPoiInfo newInstance(POIWaypointWithMedia waypoint) {
        LongChatPoiInfo fragment = new LongChatPoiInfo();
        Bundle args = new Bundle();
        args.putSerializable(ARG_WAYPOINT, waypoint);
        fragment.setArguments(args);
        return fragment;
    }

    public LongChatPoiInfo() {
        // Empty constructor
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

        fileProvider = new FileProvider(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPoiInfoChatLongBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupMessages(view, savedInstanceState);
        binding.textTitle.setText(waypoint.getTitle());
    }


    private void setupMessages(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapter = new MessagesListAdapter<>(MessagesParser.SELF_USER.getId(), new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                imageView.setImageURI(fileProvider.getMediaUri(url));
            }
        });

        List<IMessage> messages = new MessagesParser(waypoint.getLongDescription())
                .parseMessages();


        for (IMessage message : messages) {
            adapter.addToStart(message, true);
        }
        binding.longPoiInfoChat.setAdapter(adapter);
    }

    private void unbindFromAudioService() {
        audioManager.removeConnectionListener(playerConnectionCallback);
        audioBinder = null;
        audioController = null;
    }


    private void startAudio() {
        if (audioManager == null) {
            return;
        }

        audioManager.startPlayback(waypoint);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindFromAudioService();
    }
}
