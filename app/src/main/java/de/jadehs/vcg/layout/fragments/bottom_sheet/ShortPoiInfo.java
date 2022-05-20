package de.jadehs.vcg.layout.fragments.bottom_sheet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;
import org.oscim.core.GeoPoint;

import java.util.Objects;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.models.POIWaypoint;
import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia;
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints;
import de.jadehs.vcg.services.NearbyWaypointService;
import de.jadehs.vcg.view_models.RouteViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShortPoiInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShortPoiInfo extends Fragment {


    private static final String ARG_Waypoint = "de.jadehs.vcg.arg.wayopint";


    private TextView title;
    private TextView description;
    private FloatingActionButton actionButton;
    private POIInfoListener listener;


    private POIWaypoint waypoint;
    private ConstraintLayout passwordContainer;
    private Button passwordConfirmButton;
    private TextInputLayout passwordTextLayout;
    private RouteViewModel routeViewModel;

    public ShortPoiInfo() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param waypoint The waypoint which needs to gets displayed.
     * @return A new instance of fragment ShortPoiInfo.
     */
    public static ShortPoiInfo newInstance(POIWaypoint waypoint) {
        ShortPoiInfo fragment = new ShortPoiInfo();
        Bundle args = new Bundle();
        args.putSerializable(ARG_Waypoint, waypoint);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            waypoint = (POIWaypointWithMedia) getArguments().getSerializable(ARG_Waypoint);
        }

        routeViewModel = new ViewModelProvider(this.requireActivity(), new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(RouteViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_poi_info_short, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.title = view.findViewById(R.id.detailTitle);
        this.description = view.findViewById(R.id.detailDescription);
        RouteWithWaypoints route = routeViewModel.getCurrentRoute().getValue();
        boolean isNext = false;
        if (route != null) {
            POIWaypoint waypoint = route.getNextWaypoint();
            if (waypoint != null) {
                isNext = waypoint.getId() == waypoint.getId();
            }
        }
        /*this.actionButton = view.findViewById(R.id.directions);
        actionButton.setOnClickListener(this);*/
        passwordContainer = view.findViewById(R.id.password_input_container);
        if (waypoint.getUnlockAction() == POIWaypoint.UnlockAction.PASSWORD && isNext) {
            passwordTextLayout = passwordContainer.findViewById(R.id.password_input);
            passwordConfirmButton = passwordContainer.findViewById(R.id.password_confirm_button);
            passwordConfirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPasswordConfirm();
                }
            });
        } else {
            passwordContainer.setVisibility(View.GONE);
        }

        fillInformation(waypoint);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        /*try{
            listener = (POIInfoListener) context;
        }catch (ClassCastException ex){
            throw new IllegalStateException("Attaching context needs to be an implementation of the POIInfoListener interface");
        }*/
    }

    /*@Override
    public void onClick(View view) {
        //listener.onExtraButtonClick();

        *//*if(false){
            openDetailActivity(waypoint.getId());
        }else{*//*
            // label doesn't work, already listed in google issue tracker. Implemented if the function will be supported in the future
            GeoPoint location = waypoint.getGeoPosition();
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.US,"geo:%f,%f?q=%f, %f(%s)",location.getLatitude(), location.getLongitude(),location.getLatitude(),location.getLongitude(), Uri.encode((String) title.getText()))));
            this.getContext().startActivity(i);
*//*
            if(id == waypoint.getId())
                startService(1,waypoint);*//*
        //}
    }*/


    private void fillInformation(POIWaypoint waypoint) {
        this.setTitle(waypoint.getTitle());
        this.setDescription(waypoint.getShortDescription());
    }

    @NotNull
    public String getTitle() {
        return (String) title.getText();
    }

    @NotNull
    public String getDescription() {
        return (String) description.getText();
    }


    public void setTitle(@NotNull String title) {
        this.title.setText(title);
    }


    public void setDescription(@NotNull String description) {
        this.description.setText(description);
    }


    @NonNull
    public String getPassword() {
        if (passwordTextLayout == null)
            return "null";
        EditText editText = passwordTextLayout.getEditText();
        if (editText == null)
            return "null";
        return Objects.toString(editText.getText());
    }


    private void onPasswordConfirm() {
        String password = getPassword();


        boolean same = password.equalsIgnoreCase(waypoint.getPassword());


        if (same) {
            // TODO unlock waypoint
            Toast.makeText(this.requireContext(), "Richtiges Passwort", Toast.LENGTH_LONG).show();
        }
    }

    /*private void openDetailActivity(long waypointId) {
        Intent i = new Intent(this.getContext(), DetailActivity.class);
        i.putExtra(DetailActivity.ID_EXTRA, waypointId);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        this.getContext().startActivity(i);
    }*/


    private void startService(long routeID, POIWaypoint waypoint) {
        Intent intent = new Intent(this.getContext(), NearbyWaypointService.class);
        intent.putExtra(NearbyWaypointService.EXTRA_MAPID, routeID);
        intent.putExtra(NearbyWaypointService.EXTRA_WPID, waypoint.getId());
        GeoPoint point = waypoint.getGeoPosition();
        intent.putExtra(NearbyWaypointService.EXTRA_WPLAT, point.getLatitude());
        intent.putExtra(NearbyWaypointService.EXTRA_WPLNG, point.getLongitude());
        this.getContext().startService(intent);
    }
}