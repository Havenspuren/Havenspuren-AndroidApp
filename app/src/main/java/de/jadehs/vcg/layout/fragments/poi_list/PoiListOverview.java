package de.jadehs.vcg.layout.fragments.poi_list;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints;
import de.jadehs.vcg.layout.behaviours.BottomSheetController;
import de.jadehs.vcg.layout.fragments.route_view.RouteViewFragment;
import de.jadehs.vcg.utils.BottomSheetControllerProvider;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PoiListOverview#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PoiListOverview extends RouteViewFragment implements BottomSheetControllerProvider {
    private static final String TAG = "PoiListOverview";

    private RecyclerView recyclerView;
    private POIAdapter recyclerAdapter;
    private BottomSheetControllerProvider controllerProvider;
    private FrameLayout bottomSheetView;
    private BottomSheetController bottomSheetController;
    private TextView progressView;
    private TextView trophyProgressView;
    private String progressText;
    private String trophyProgressText;
    private ImageView routeImageView;

    public PoiListOverview() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param routeId id of the route
     * @return A new instance of fragment PoiListOverview.
     */
    public static PoiListOverview newInstance(long routeId) {
        PoiListOverview fragment = new PoiListOverview();
        Bundle args = new Bundle();
        args.putLong(ARG_ROUTE_ID, routeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_poi_list_overview, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecycler(view, savedInstanceState);
        setupObserver(view, savedInstanceState);
        setupRouteInfos(view, savedInstanceState);
        controllerProvider = this;
    }

    private void setProgress(int percent) {
        this.progressView.setText(String.format(this.progressText, percent));
    }

    private void setTrophyProgress(int unlocked, int total) {
        this.trophyProgressView.setText(String.format(this.trophyProgressText, unlocked, total));
    }

    private void setupObserver(View view, Bundle savedInstanceState) {
        this.getCurrentRoute().observe(getViewLifecycleOwner(), new Observer<RouteWithWaypoints>() {
            @Override
            public void onChanged(RouteWithWaypoints routeWithWaypoints) {
                recyclerAdapter.setWaypoints(routeWithWaypoints.getWaypoints());
                setProgress((int) (routeWithWaypoints.getProgress() * 100));
                int[] trophyProgress = routeWithWaypoints.getTrophyProgress();
                setTrophyProgress(trophyProgress[0], trophyProgress[1]);
            }
        });
    }

    private void setupRouteInfos(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.progressView = view.findViewById(R.id.poi_list_progress);
        this.progressText = view.getContext().getString(R.string.route_progress_text);

        this.trophyProgressView = view.findViewById(R.id.poi_list_trophy_progress);
        this.trophyProgressText = view.getContext().getString(R.string.trophy_progress);
        this.routeImageView = view.findViewById(R.id.poi_list_route_picture);
    }

    private void setupRecycler(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.poi_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerAdapter = new POIAdapter();
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public BottomSheetController getController() {
        return bottomSheetController;
    }
}