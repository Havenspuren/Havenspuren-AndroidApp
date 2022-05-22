package de.jadehs.vcg.layout.fragments.poi_list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints;
import de.jadehs.vcg.databinding.FragmentPoiListOverviewBinding;
import de.jadehs.vcg.layout.fragments.route_view.RouteViewFragment;
import de.jadehs.vcg.utils.data.FileProvider;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PoiListOverview#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PoiListOverview extends RouteViewFragment {
    private static final String TAG = "PoiListOverview";

    private POIAdapter recyclerAdapter;
    private String progressText;
    private String trophyProgressText;
    private FragmentPoiListOverviewBinding binding;
    private FileProvider fileProvider;

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

    public PoiListOverview() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.fileProvider = new FileProvider(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPoiListOverviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecycler(view, savedInstanceState);
        setupObserver(view, savedInstanceState);
        setupRouteInfos(view, savedInstanceState);
    }

    private void setProgress(int percent) {
        this.binding.poiListProgress.setText(String.format(this.progressText, percent));
    }

    private void setTrophyProgress(int unlocked, int total) {
        this.binding.poiListTrophyProgress.setText(String.format(this.trophyProgressText, unlocked, total));
    }

    private void setRouteImage(String pathToRouteImage) {
        this.binding.poiListRoutePicture.setImageURI(fileProvider.getMediaUri(pathToRouteImage));
    }

    private void setupObserver(View view, Bundle savedInstanceState) {
        this.getCurrentRoute().observe(getViewLifecycleOwner(), new Observer<RouteWithWaypoints>() {
            @Override
            public void onChanged(RouteWithWaypoints routeWithWaypoints) {
                recyclerAdapter.setWaypoints(routeWithWaypoints.getWaypoints());
                setProgress((int) (routeWithWaypoints.getProgress() * 100));
                int[] trophyProgress = routeWithWaypoints.getTrophyProgress();
                setTrophyProgress(trophyProgress[0], trophyProgress[1]);
                setRouteImage(routeWithWaypoints.getPoiRoute().getPathToRouteImage());
            }
        });
    }

    private void setupRouteInfos(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.progressText = view.getContext().getString(R.string.route_progress_text);

        this.trophyProgressText = view.getContext().getString(R.string.trophy_progress);
    }

    private void setupRecycler(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.binding.poiRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerAdapter = new POIAdapter();
        this.binding.poiRecyclerView.setAdapter(recyclerAdapter);
    }
}