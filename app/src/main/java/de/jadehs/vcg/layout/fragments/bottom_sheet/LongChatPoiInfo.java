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
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.List;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia;
import de.jadehs.vcg.data.model.TextMessage;
import de.jadehs.vcg.databinding.FragmentPoiInfoChatLongBinding;
import de.jadehs.vcg.services.audio.AudioPlayerManager;
import de.jadehs.vcg.services.audio.AudioPlayerService;
import de.jadehs.vcg.services.audio.PlayerConnectionCallback;

public class LongChatPoiInfo extends Fragment {

    private static final String TAG = "LongChatPoiInfo";

    private static final String ARG_WAYPOINT = "de.jadehs.vcg.waypoint";
    private POIWaypointWithMedia waypoint;
    private AudioPlayerManager audioManager;
    private FragmentPoiInfoChatLongBinding binding;
    private MessagesListAdapter<IMessage> adapter;
    private PlayerConnectionCallback playerConnectionCallback = new PlayerConnectionCallback() {
        @Override
        public void connectionEstablished(AudioPlayerService.AudioServiceBinder binder) {

        }

        @Override
        public void onSessionAvailable(MediaControllerCompat controller) {

        }

        @Override
        public void connectionLost() {

        }
    };

    public LongChatPoiInfo() {
        // Empty constructor
    }

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPoiInfoChatLongBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new MessagesListAdapter<>("ai", new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {

            }
        });

        List<IMessage> messages = new ArrayList<>();
        messages.add(new TextMessage("m1", "*test*", new IUser() {
            @Override
            public String getId() {
                return "ai";
            }

            @Override
            public String getName() {
                return "Dr. Toll";
            }

            @Override
            public String getAvatar() {
                return null;
            }
        }));
        messages.add(new TextMessage("m2", "test", new IUser() {
            @Override
            public String getId() {
                return "ai2";
            }

            @Override
            public String getName() {
                return "Dr. Toll";
            }

            @Override
            public String getAvatar() {
                return null;
            }
        }));
        for (IMessage message : messages) {
            adapter.addToStart(message, true);
        }
        binding.longPoiInfoChat.setAdapter(adapter);
    }

    private void unbindFromAudioService() {
        audioManager.removeConnectionListener(playerConnectionCallback);
//        binder = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindFromAudioService();
    }
}
