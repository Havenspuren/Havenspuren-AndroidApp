package de.jadehs.vcg.layout.fragments.trophies.recycler;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.models.POIRoute;
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints;
import de.jadehs.vcg.layout.fragments.trophies.TrophyMapFragment;
import de.jadehs.vcg.utils.data.FileProvider;
import lombok.Getter;
import lombok.Setter;

public class RouteTrophyOverviewViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    private final ImageView image;
    private final CardView container;
    private final ClickHandler listener = new ClickHandler();
    private NavController controller;

    public RouteTrophyOverviewViewHolder(@NonNull View itemView,NavController controller) {
        super(itemView);
        this.controller = controller;
        title = itemView.findViewById(R.id.trophy_route_title);
        image = itemView.findViewById(R.id.trophy_route_image);
        container = itemView.findViewById(R.id.trophy_overview_container);
        container.setOnClickListener(listener);
    }

    public void bind(RouteWithWaypoints r){
        POIRoute route = r.getPoiRoute();
        title.setText(route.getName());
        FileProvider provider = new FileProvider(itemView.getContext());
        if(route.hasPathToRouteImage())
            image.setImageURI(provider.getMediaUri(route.getPathToRouteImage()));
        listener.setId(route.getId());
        listener.setTitle(route.getName());
        listener.setMapPath(route.getPathToMapImage());
    }

    @Getter
    @Setter
    private class ClickHandler implements View.OnClickListener{
        private long id;
        private String title;
        private String mapPath;
        @Override
        public void onClick(View v) {
            Bundle b = TrophyMapFragment.createArguments(getId(), getTitle(), getMapPath());
            controller.navigate(R.id.action_trophy_overview_to_trophy_map,b);
        }
    }

}
