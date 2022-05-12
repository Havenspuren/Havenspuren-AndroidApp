package de.jadehs.vcg.layout.fragments.trophies;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.List;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.pojo.TrophyWithWaypoint;
import de.jadehs.vcg.utils.data.FileProvider;
import de.jadehs.vcg.views.TrophyMap;

public class TrophyMapFragment extends Fragment implements TrophyMap.FocusChangedListener {

    public static final String ROUTE_ID_ARGUMENT = "de.jadehs.vct.ROUTE_ID";
    public static final String ROUTE_MAP_ARGUMENT = "de.jadehs.vct.ROUTE_MAP";
    private static final String TAG = "TrophyMapFragment";
    NavController controller;
    private TrophyMapViewModel viewModel;
    private TrophyMap trophyMap;
    private TrophyAdapter adapter;
    private long routeId = 1;
    private TextView currentTrophyName;
    private ImageView previousButton;
    private ImageButton nextButton;
    private FileProvider fileProvider;
    private String routeMap;
    private String routeName;

    public static TrophyMapFragment newInstance(long routeId, String title, String mapPath) {
        TrophyMapFragment fragment = new TrophyMapFragment();
        fragment.setArguments(createArguments(routeId, title, mapPath));
        return fragment;
    }

    public static Bundle createArguments(long routeId, String title, String mapPath) {
        Bundle b = new Bundle();
        b.putLong(ROUTE_ID_ARGUMENT, routeId);
        b.putString("title", title);
        b.putString(ROUTE_MAP_ARGUMENT, mapPath);
        return b;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            routeId = getArguments().getLong(ROUTE_ID_ARGUMENT);
            routeMap = getArguments().getString(ROUTE_MAP_ARGUMENT);
            routeName = getArguments().getString("title");
        }
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(this.requireActivity().getApplication())).get(TrophyMapViewModel.class);
        viewModel.setRouteOfTrophies(routeId);
        fileProvider = new FileProvider(this.requireActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trophy_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = Navigation.findNavController(view);
        trophyMap = view.findViewById(R.id.trophy_map);

        bindRouteData(this.routeName, this.routeMap);


        currentTrophyName = view.findViewById(R.id.trophy_overview_current_name);


        if (adapter == null) {
            adapter = new TrophyAdapter();
        }
        trophyMap.setAdapter(adapter);

        previousButton = view.findViewById(R.id.previous_button);

        nextButton = view.findViewById(R.id.next_button);

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trophyMap.focusPreviousTrophy();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trophyMap.focusNextTrophy();
            }
        });

        setupObserver();
    }

    private void setupObserver() {
        if (adapter == null) {
            adapter = new TrophyAdapter();
        }
        viewModel.getTrophies().observe(this.getViewLifecycleOwner(), adapter);
        trophyMap.addFocusChangedListener(this);
    }

    private void bindRouteData(String routeName, String pathToImage) {
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(routeName);
        this.trophyMap.setTrophyBackground(Drawable.createFromPath(fileProvider.getMediaFile(pathToImage).getAbsolutePath()));
    }

    @Override
    public void onFocusChangedListener(int index) {
        if (viewModel.getTrophies().getValue() != null) {
            TrophyWithWaypoint trophy = viewModel.getTrophies().getValue().get(index);
            currentTrophyName.setText(trophy.getName());


            previousButton.setEnabled(index > 0);
            nextButton.setEnabled(index < viewModel.getTrophies().getValue().size() - 1);
        }
    }


    private class TrophyAdapter extends TrophyMap.Adapter implements Observer<List<TrophyWithWaypoint>> {
        TrophyWithWaypoint[] trophies;
        FileProvider fileProvider;

        public TrophyAdapter() {
            trophies = new TrophyWithWaypoint[0];
            fileProvider = new FileProvider(getContext());
        }

        @Override
        public int getCount() {
            return trophies.length;
        }

        @Override
        public Bitmap getBitmapOfPosition(int position) {
            TrophyWithWaypoint trophy = trophies[position];
            // TODO cache Bitmap
            return BitmapFactory.decodeFile(fileProvider.getMediaFile(trophy.getPathToIcon()).getAbsolutePath());
        }

        @Override
        public Point getPositionOfPosition(int position, float width, float height) {
            TrophyWithWaypoint trophy = trophies[position];
            int actualX = (int) (width * trophy.getX() / 100);
            int actualY = (int) (height * trophy.getY() / 100);
            return new Point(actualX, actualY);
        }

        @Override
        public boolean getGreyScaleOfPosition(int position) {
            return !trophies[position].getWaypoint().isVisited();
        }

        @Override
        public void onChanged(List<TrophyWithWaypoint> trophyWithWaypoints) {
            trophies = trophyWithWaypoints.toArray(new TrophyWithWaypoint[0]);
            notifyDataSetHasChanged();
        }

        private void loadBitmaps() {
        }

        @Override
        public void onClickAtPosition(int position) {
            //TODO add check if trophy already unlocked

            TrophyWithWaypoint trophy = trophies[position];
            if (trophy.getWaypoint().isVisited()) {
                String characterPath = trophy.getPathToCharacterImage();
                if (characterPath == null && trophy.getWaypoint().getRoute().hasCharacterImage()) {
                    characterPath = trophy.getWaypoint().getRoute().getPathToCharacterImage();
                }
                Bundle b = TrophyDetailFragment.createArguments(
                        trophy.getPathToImage(),
                        trophy.getDescription(),
                        characterPath
                );

                b.putString("title", trophy.getName());
                controller.navigate(R.id.action_trophy_map_to_trophy_detail, b);
            }
        }
    }

}