package de.jadehs.vcg.layout.fragments.routes;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.List;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.models.POIRoute;
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints;
import de.jadehs.vcg.layout.fragments.map.MapViewFragment;
import de.jadehs.vcg.layout.fragments.route_view.RouteViewFragment;
import de.jadehs.vcg.utils.data.FileProvider;
import de.jadehs.vcg.view_models.AllRoutesViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class RouteOverviewFragment extends Fragment {

    private AllRoutesViewModel viewmodel;
    private FileProvider fileProvider;

    public RouteOverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.fileProvider = new FileProvider(this.requireContext());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            viewmodel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(((Activity) context).getApplication()))
                    .get(AllRoutesViewModel.class);
        } else {
            throw new IllegalArgumentException("Context needs to be an Activity");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_route_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final LinearLayout systemRoutesContainer = view.findViewById(R.id.systemRouteContainer);

        // observer
        viewmodel.getRoutes().observe(this.getViewLifecycleOwner(), new Observer<List<RouteWithWaypoints>>() {
            @Override
            public void onChanged(List<RouteWithWaypoints> poiRoutes) {
                systemRoutesContainer.removeAllViews();
                // TODO change to recycler view
                for (final RouteWithWaypoints route : poiRoutes) {
                    View routeView = getLayoutInflater().inflate(R.layout.route_list_item, systemRoutesContainer);
                    final POIRoute r = route.getPoiRoute();
                    float progress = route.getProgress();
                    ((TextView) routeView.findViewById(R.id.route_progress_text)).setText(
                            String.format(requireContext().getString(R.string.route_progress_text), (int)(progress * 100))
                    );
                    ((ProgressBar)routeView.findViewById(R.id.route_progress_bar)).setProgress((int)(progress*100));


                    ((TextView) routeView.findViewById(R.id.RouteName)).setText(r.getName());

                    ((ImageView) routeView.findViewById(R.id.route_image)).setImageURI(fileProvider.getMediaUri(r.getPathToRouteImage()));

                    routeView.findViewById(R.id.continue_route_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // route to Map view with the graph fragment
                            NavController controller = Navigation.findNavController(view);
                            Bundle args = new Bundle();
                            args.putLong(RouteViewFragment.ARG_ROUTE_ID, r.getId());
                            args.putString(MapViewFragment.ARG_MAP_PATH, r.getPathToMap());
                            args.putString("title", r.getName());
                            controller.navigate(R.id.destination_map_fragment, args);
                        }
                    });

                    routeView.findViewById(R.id.information_route_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            NavController controller = Navigation.findNavController(view);
                            Bundle args = new Bundle();
                            args.putLong(RouteViewFragment.ARG_ROUTE_ID, r.getId());
                            args.putString("title", r.getName());
                            controller.navigate(R.id.destination_route_info, args);
                        }
                    });
                }
            }
        });
    }
}