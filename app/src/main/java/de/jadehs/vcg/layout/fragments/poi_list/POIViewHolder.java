package de.jadehs.vcg.layout.fragments.poi_list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.models.POIWaypoint;
import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia;
import de.jadehs.vcg.layout.fragments.map.MapViewFragment;
import de.jadehs.vcg.utils.data.FileProvider;

/**
 * Does display one waypoint
 */
public class POIViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView imageView;
    private final TextView titleView;
    private final TextView descView;
    private final View trophyStatusView;
    private final View dottedTop;
    private final View dottedBottom;
    private final ViewGroup titleContainer;
    private POIWaypoint waypoint;

    private final FileProvider fileProvider;

    public POIViewHolder(@NonNull View itemView, FileProvider fileProvider) {
        super(itemView);
        this.imageView = itemView.findViewById(R.id.poi_image);
        this.titleView = itemView.findViewById(R.id.poi_title);
        this.titleContainer = itemView.findViewById(R.id.title_container);
        this.descView = itemView.findViewById(R.id.poi_desc);
        this.trophyStatusView = itemView.findViewById(R.id.poi_trophy_status);
        this.dottedTop = itemView.findViewById(R.id.dotted1);
        this.dottedBottom = itemView.findViewById(R.id.dotted2);


        this.fileProvider = fileProvider;
        itemView.setOnClickListener(this);
    }


    /**
     * Does bind the given data to the views
     *
     * @param waypoint the waypoint which will provide the data
     */
    public void bindData(POIWaypointWithMedia waypoint) {
        this.waypoint = waypoint;
        boolean visited = waypoint.isVisited();
        this.titleView.setText(waypoint.getTitle());
        this.descView.setText(waypoint.getShortDescription());
        this.trophyStatusView.setVisibility((visited && waypoint.hasTrophy()) ? View.VISIBLE : View.INVISIBLE);
        this.imageView.setImageDrawable(
                ContextCompat.getDrawable(itemView.getContext(),
                        visited ? R.drawable.ic_checkmark : R.drawable.ic_empty_checkmark
                ));

        this.dottedTop.setVisibility(View.VISIBLE);
        this.dottedBottom.setVisibility(View.VISIBLE);

        this.titleContainer.getChildAt(0).forceLayout();
    }



    public void setVisibilityDottedTop(boolean visible){
        this.dottedTop.setVisibility(visible ? View.VISIBLE: View.INVISIBLE);
    }

    public void setVisibilityDottedBottom(boolean visible){
        this.dottedBottom.setVisibility(visible ? View.VISIBLE: View.INVISIBLE);
    }



    /**
     * Add current waypoint id to the save state of the previous destination and navigate up
     *
     * @param view the view which got clicked on
     */
    @Override
    public void onClick(View view) {
//        Toast.makeText(this.itemView.getContext(), "Click on " + waypoint.getTitle(), Toast.LENGTH_SHORT).show();
        NavController controller = Navigation.findNavController(view);
        NavBackStackEntry prevBackStack = controller.getPreviousBackStackEntry();
        if (prevBackStack != null) {
            prevBackStack.getSavedStateHandle().set(MapViewFragment.FOCUS_WAYPOINT_KEY, waypoint.getId());
        }
        controller.navigateUp();

    }
}
