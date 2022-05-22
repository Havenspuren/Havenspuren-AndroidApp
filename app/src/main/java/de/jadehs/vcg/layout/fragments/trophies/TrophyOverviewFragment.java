package de.jadehs.vcg.layout.fragments.trophies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints;
import de.jadehs.vcg.layout.fragments.trophies.recycler.RouteTrophyAdapter;
import de.jadehs.vcg.view_models.AllRoutesViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrophyOverviewFragment extends Fragment {


    private AllRoutesViewModel mViewModel;
    private RecyclerView recyclerView;
    private RouteTrophyAdapter recylcerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(
                requireActivity(),
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
        ).get(AllRoutesViewModel.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trophy_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupObserver();
        recyclerView = view.findViewById(R.id.trophy_grid_layout);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recylcerAdapter = new RouteTrophyAdapter();
        recyclerView.setAdapter(recylcerAdapter);
    }


    private void setupObserver() {
        mViewModel.getRoutes().observe(this.getViewLifecycleOwner(), new Observer<List<RouteWithWaypoints>>() {
            @Override
            public void onChanged(List<RouteWithWaypoints> poiRoutes) {

                // TODO change to filter in select
                List<RouteWithWaypoints> routeWithWaypoints = new LinkedList<>();

                for (RouteWithWaypoints r : poiRoutes) {
                    if (r.getTrophyCount() > 0) {
                        routeWithWaypoints.add(r);
                    }
                }
                recylcerAdapter.submitList(routeWithWaypoints);
            }
        });
    }
}