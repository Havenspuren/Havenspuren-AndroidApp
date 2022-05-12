package de.jadehs.vcg.layout.fragments.poi_list;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia;
import de.jadehs.vcg.utils.data.FileProvider;

public class POIAdapter extends RecyclerView.Adapter<POIViewHolder> {

    private POIWaypointWithMedia[] waypoints;
    private FileProvider fileProvider;

    public void setWaypoints(List<POIWaypointWithMedia> waypoints){
        setWaypoints(waypoints.toArray(new POIWaypointWithMedia[0]));
    }

    public void setWaypoints(POIWaypointWithMedia[] waypoints) {
        this.waypoints = waypoints;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public POIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(fileProvider == null)
            fileProvider = new FileProvider(parent.getContext());
        return new POIViewHolder(inflater.inflate(R.layout.poi_list_item, parent, false),fileProvider);
    }

    @Override
    public void onBindViewHolder(@NonNull POIViewHolder holder, int position) {
        holder.bindData(waypoints[position]);
        if(position == 0) {
            holder.setVisibilityDottedTop(false);
        }

        if(position == getItemCount() - 1){
            holder.setVisibilityDottedBottom(false);
        }
    }

    @Override
    public int getItemCount() {
        if(waypoints == null)
            return 0;
        return waypoints.length;
    }
}
