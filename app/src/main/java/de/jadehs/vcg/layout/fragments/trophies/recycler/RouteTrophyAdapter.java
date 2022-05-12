package de.jadehs.vcg.layout.fragments.trophies.recycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import org.jetbrains.annotations.NotNull;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints;

public class RouteTrophyAdapter extends ListAdapter<RouteWithWaypoints,RouteTrophyOverviewViewHolder> {

    public static DiffUtil.ItemCallback<RouteWithWaypoints> DIFF_CALLBACK = new DiffUtil.ItemCallback<RouteWithWaypoints>() {
        @Override
        public boolean areItemsTheSame(@NonNull RouteWithWaypoints oldItem, @NonNull RouteWithWaypoints newItem) {
            return oldItem.getPoiRoute().getId() == newItem.getPoiRoute().getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull RouteWithWaypoints oldItem, @NonNull RouteWithWaypoints newItem) {
            return areItemsTheSame(oldItem,newItem);
        }
    };

    public RouteTrophyAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public RouteTrophyOverviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        NavController controller = Navigation.findNavController(parent);
        return new RouteTrophyOverviewViewHolder(layoutInflater.inflate(R.layout.route_trophy_grid_item,parent,false),controller);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RouteTrophyOverviewViewHolder holder, int position) {
        holder.bind(this.getItem(position));
    }

}
