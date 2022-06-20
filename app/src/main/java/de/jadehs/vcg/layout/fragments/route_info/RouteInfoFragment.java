package de.jadehs.vcg.layout.fragments.route_info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.models.POIRoute;
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints;
import de.jadehs.vcg.databinding.FragmentRouteInfoBinding;
import de.jadehs.vcg.layout.fragments.route_view.RouteViewFragment;
import de.jadehs.vcg.utils.data.FileProvider;
import io.noties.markwon.Markwon;

public class RouteInfoFragment extends RouteViewFragment {


    private String progressText;
    private String trophyProgressText;
    private FileProvider fileProvider;
    private FragmentRouteInfoBinding binding;
    private Markwon markwon;

    public RouteInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        markwon = Markwon.create(requireContext());

        fileProvider = new FileProvider(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRouteInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupInfoViews(view, savedInstanceState);
        setupObserver();
    }

    private void setupInfoViews(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.progressText = view.getContext().getString(R.string.route_progress_text);
        this.trophyProgressText = view.getContext().getString(R.string.trophy_progress);
    }

    private void setupObserver() {
        getCurrentRoute().observe(getViewLifecycleOwner(), new Observer<RouteWithWaypoints>() {
            @Override
            public void onChanged(RouteWithWaypoints routeWithWaypoints) {
                bindData(routeWithWaypoints);
            }
        });
    }

    private void bindData(RouteWithWaypoints route) {
        POIRoute poiRoute = route.getPoiRoute();
        this.binding.routeInfoTitle.setText(poiRoute.getName());
        this.binding.routeInfoDesc.setText(poiRoute.getDescription());
        this.binding.routeInfoImage.setImageURI(fileProvider.getMediaUri(poiRoute.getPathToRouteImage()));
        markwon.setMarkdown(this.binding.routeInfoContributor,route.getPoiRoute().getContributors());


        setProgress((int) (route.getProgress() * 100));
        int[] trophyProgress = route.getTrophyProgress();
        setTrophyProgress(trophyProgress[0], trophyProgress[1]);
    }

    private void setProgress(int percent) {
        this.binding.routeInfoProgress.setText(String.format(this.progressText, percent));
    }

    private void setTrophyProgress(int unlocked, int total) {
        this.binding.routeInfoTrophyProgress.setText(String.format(this.trophyProgressText, unlocked, total));
    }
}