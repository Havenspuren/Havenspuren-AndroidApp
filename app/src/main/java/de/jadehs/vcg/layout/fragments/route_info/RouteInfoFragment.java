package de.jadehs.vcg.layout.fragments.route_info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.models.POIRoute;
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints;
import de.jadehs.vcg.layout.fragments.route_view.RouteViewFragment;

public class RouteInfoFragment extends RouteViewFragment {


    private TextView titleView;
    private TextView descView;
    private ImageView imageView;
    private TextView progressView;
    private TextView trophyProgressView;
    private String progressText;
    private String trophyProgressText;

    public RouteInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_route_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupInfoViews(view,savedInstanceState);
        setupObserver();
    }

    private void setupInfoViews(@NonNull View view, @Nullable Bundle savedInstanceState){
        this.titleView = view.findViewById(R.id.route_info_title);
        this.descView = view.findViewById(R.id.route_info_desc);
        this.imageView = view.findViewById(R.id.route_info_image);

        this.progressView = view.findViewById(R.id.route_info_progress);
        this.progressText = view.getContext().getString(R.string.route_progress_text);

        this.trophyProgressView = view.findViewById(R.id.route_info_trophy_progress);
        this.trophyProgressText = view.getContext().getString(R.string.trophy_progress);
    }

    private void setupObserver(){
        getCurrentRoute().observe(getViewLifecycleOwner(), new Observer<RouteWithWaypoints>() {
            @Override
            public void onChanged(RouteWithWaypoints routeWithWaypoints) {
                bindData(routeWithWaypoints);
            }
        });
    }

    private void bindData(RouteWithWaypoints route){
        POIRoute poiRoute = route.getPoiRoute();
        this.titleView.setText(poiRoute.getName());
        this.descView.setText(poiRoute.getDescription());
        this.imageView.setImageDrawable(ContextCompat.getDrawable(imageView.getContext(), R.drawable.ic_launcher_foreground));


        setProgress((int) (route.getProgress() * 100));
        int[] trophyProgress = route.getTrophyProgress();
        setTrophyProgress(trophyProgress[0], trophyProgress[1]);
    }

    private void setProgress(int percent) {
        this.progressView.setText(String.format(this.progressText, percent));
    }

    private void setTrophyProgress(int unlocked, int total) {
        this.trophyProgressView.setText(String.format(this.trophyProgressText, unlocked, total));
    }
}