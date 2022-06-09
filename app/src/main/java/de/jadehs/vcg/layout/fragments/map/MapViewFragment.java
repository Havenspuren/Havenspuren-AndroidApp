package de.jadehs.vcg.layout.fragments.map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;

import org.jetbrains.annotations.NotNull;
import org.oscim.android.MapPreferences;
import org.oscim.android.MapView;
import org.oscim.android.canvas.AndroidBitmap;
import org.oscim.backend.CanvasAdapter;
import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.event.Event;
import org.oscim.event.MotionEvent;
import org.oscim.layers.LocationLayer;
import org.oscim.layers.PathLayer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerInterface;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.renderer.GLViewport;
import org.oscim.scalebar.DefaultMapScaleBar;
import org.oscim.scalebar.MapScaleBar;
import org.oscim.scalebar.MapScaleBarLayer;
import org.oscim.theme.IRenderTheme;
import org.oscim.theme.VtmThemes;
import org.oscim.theme.styles.LineStyle;
import org.oscim.tiling.source.mapfile.MapFileTileSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.jadehs.vcg.R;
import de.jadehs.vcg.broadcast_receiver.TrophyBroadcastReceiver;
import de.jadehs.vcg.data.db.models.POIWaypoint;
import de.jadehs.vcg.data.db.pojo.POIWaypointWithMedia;
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints;
import de.jadehs.vcg.layout.behaviours.BottomSheetController;
import de.jadehs.vcg.layout.dialogs.RouteFinishedDialog;
import de.jadehs.vcg.layout.fragments.InstructionsDisplayFragment;
import de.jadehs.vcg.layout.fragments.audio_control.AudioControlBottomFragment;
import de.jadehs.vcg.layout.fragments.route_view.RouteViewFragment;
import de.jadehs.vcg.utils.BottomSheetControllerProvider;
import de.jadehs.vcg.utils.data.FileProvider;
import de.jadehs.vcg.view_models.MapViewModel;


/**
 * Map Fragment, which contains a map with waypoints and a center view button
 * <p>
 * Brief summary of how this fragment works:
 * <p>
 * - View Model saves the current route
 * - Observer of route adds markers to the map
 */
public class MapViewFragment extends RouteViewFragment implements BottomSheetControllerProvider, RouteLocationObserver.NavigationInstructionListener {

    public static final String ARG_MAP_PATH = "de.jadehs.vcg.MAP_PATH";

    public static final String FOCUS_WAYPOINT_KEY = "FOCUS_WAYPOINT_KEY";
    private static final String TAG = "MapViewFragment";
    private static final int DEFAULT_PATH_COLOR = 0x993366FF;
    private final RouteLocationObserver locationObserver = new RouteLocationObserver();
    // POIInfoWindow infoWindow;
    // List<Overlay> myOverlays;
    // WPMarkerWD nextPoint;
    private final DestinationLocationObserver destinationLocationObserver = new DestinationLocationObserver();
    // Views
    MapView map;
    BottomSheetControllerProvider controllerProvider;
    BottomSheetController bottomSheetController;
    private final DestinationLocationObserver.NextStepReachedListener destinationListener = new DestinationLocationObserver.NextStepReachedListener() {
        @Override
        public void onNextStepReachedListener(POIWaypointWithMedia waypoint) {
            if (waypoint.getUnlockAction() != POIWaypoint.UnlockAction.GPS) {
                return;
            }
            RouteWithWaypoints route = getCurrentRoute().getValue();
            if (route == null) {
                return;
            }
            if (waypoint.getId() == route.getNextWaypoint().getId()) {

                getRouteViewModel().unlockWaypointsUntil(waypoint);
                bottomSheetController.setAttachedTo(waypoint);
                bottomSheetController.open();
                if (route.getVisitedCount() == route.getWaypoints().size() - 1) {
                    new RouteFinishedDialog().show(getChildFragmentManager(), null);
                }
                if (waypoint.hasTrophy()) {
                    requireActivity().sendBroadcast(TrophyBroadcastReceiver.createIntent(getActivity(), route.getPoiRoute().getId()));
                }

            }

        }
    };
    MapViewModel mapViewModel;
    private String mapPath;
    private FloatingActionButton centerButton;
    private IRenderTheme theme;
    private FrameLayout bottomSheetView;
    private ItemizedLayer markerLayer;
    private LocationFollower locationFollower;
    private boolean followLocation;
    private LocationLayer locationLayer;
    private PathLayer pathLayer;
    private LineStyle.LineBuilder<?> pathLayerStyle;
    private Location lastLocation;
    private InstructionsDisplayFragment directionsFragment;
    private final LocationCallback defaultLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                lastLocation = locationResult.getLastLocation();
                directionsFragment.setCurrentLocation(lastLocation);
                if (!locationLayer.isEnabled())
                    locationLayer.setEnabled(true);
                Location loc = locationResult.getLastLocation();
                locationLayer.setPosition(loc.getLatitude(), loc.getLongitude(), loc.getAccuracy());
            }
        }
    };
    private final RouteLocationObserver.RecalculateListener recalculateListener = new RouteLocationObserver.RecalculateListener() {
        @Override
        public void onRecalculateNeeded(Location newStartLocation) {
            if (MapViewFragment.this.getCurrentRoute().getValue() != null) {
                POIWaypoint waypoint = MapViewFragment.this.getCurrentRoute().getValue().getNextWaypoint();
                if (waypoint == null) {
                    locationObserver.pause();
                    pathLayer.clearPath();
                    directionsFragment.setCurrentInstruction(null);
                } else {
                    GeoPoint point = waypoint.getGeoPosition();
                    mapViewModel.getRouteCache().calcPath(newStartLocation.getLatitude(), newStartLocation.getLongitude(), point.getLatitude(), point.getLongitude());
                }
            }
        }
    };
    private MapFileTileSource tileSource;
    private FragmentContainerView bottomFragmentContainer;


    public MapViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        mapPath = this.getArguments().getString(ARG_MAP_PATH);
        if (mapPath == null)
            NavHostFragment.findNavController(this).navigateUp();

        // DEBUG PURPOSES
        getLifecycle().addObserver(new LifecycleObserver() {


            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            public void onActivityDestroyed() {
                Log.d(TAG, "onActivityDestroyed: ");
                getLifecycle().removeObserver(this);
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            public void onActivityStopped() {
                Log.d(TAG, "onActivityStopped: ");
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            public void OnActivityStarted() {
                Log.d(TAG, "OnActivityStarted: ");
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            public void OnActivityResumed() {
                Log.d(TAG, "OnActivityResumed: ");
            }
        });


        setHasOptionsMenu(true);


        mapViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(this.requireActivity().getApplication())).get(MapViewModel.class);
        int lastDot = mapPath.lastIndexOf(".");
        mapViewModel.setGraphFolder(mapPath.substring(0, lastDot));

        registerLocationCallbacks();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map_view, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // https://developer.android.com/guide/navigation/navigation-programmatic#returning_a_result <-- to retrieve if some info window should get opened
        // VIEWS

        setupAudioControl(view, savedInstanceState);
        setupMapOfflineVTM(view, savedInstanceState);
        setupBottomSheet(view, savedInstanceState);
        registerObserver(mapViewModel);

        controllerProvider = this;


        centerButton = view.findViewById(R.id.center_pos);
        centerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapViewFragment.this.centerMapOnLocation();
            }
        });

        this.directionsFragment = (InstructionsDisplayFragment) getChildFragmentManager().findFragmentById(R.id.instructions_fragment);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.map_view_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getItemId() == R.id.map_menu_item_list_overview && getArguments() != null) {
            Bundle b = new Bundle();
            b.putString("title", getArguments().getString("title"));
            b.putLong(RouteViewFragment.ARG_ROUTE_ID, this.getRouteId());
            Navigation.findNavController(requireView()).navigate(R.id.action_map_fragment_to_poi_list, b);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: ");

        requireActivity().getOnBackPressedDispatcher()
                .addCallback(
                        getViewLifecycleOwner(),
                        bottomSheetController.getOnBackPressedListener());

        if (map != null) {
            map.onResume();
        }
        registerRouteCallbacks();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (map != null) {
            MapPreferences pref = new MapPreferences(getActivity().getClass().getName(), requireContext());
            pref.save(map.map());
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");

        bottomSheetController.getOnBackPressedListener().remove();

        if (map != null) {
            map.onPause();
        }
        unregisterRouteCallbacks();
    }

    private void registerRouteCallbacks() {
        locationObserver.addRecalculateListener(recalculateListener);
        locationObserver.addInstructionListener(this);

        destinationLocationObserver.addNextStepReachedListener(destinationListener);
    }

    private void unregisterRouteCallbacks() {
        locationObserver.removeInstructionListener(this);
        locationObserver.removeRecalculateListener(recalculateListener);
        destinationLocationObserver.removeNextStepReachedListener(destinationListener);
    }

    private void registerLocationCallbacks() {
        LocationRequest request = LocationRequest.create();
        request.setInterval(0);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        this.registerLocationCallback(request, locationObserver);
        this.registerLocationCallback(request, destinationLocationObserver);
        this.registerLocationCallback(request, defaultLocationCallback);
    }

    private void registerObserver(MapViewModel viewModel) {
        this.getCurrentRoute().observe(this.getViewLifecycleOwner(), new RouteObserver());
        viewModel.getRouteCache().getRoute().observe(this.getViewLifecycleOwner(), new Observer<ResponsePath>() {
            @Override
            public void onChanged(ResponsePath responsePath) {
                PointList list = responsePath.getPoints();
                List<GeoPoint> points = new LinkedList<>();
                for (GHPoint point : list) {
                    GeoPoint p = new GeoPoint(point.getLat(), point.getLon());
                    points.add(p);
                }
                pathLayer.setPoints(points);
            }
        });
        this.getCurrentRoute().observe(this.getViewLifecycleOwner(), destinationLocationObserver);
        viewModel.getRouteCache().getRoute().observe(this.getViewLifecycleOwner(), locationObserver);

        // list click
        NavController navController = Navigation.findNavController(this.requireView());
        navController.getCurrentBackStackEntry().getSavedStateHandle().getLiveData(FOCUS_WAYPOINT_KEY, -1L).observe(this.getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long waypointId) {
                WPMarkerWD marker = getMarker(waypointId);
                if (marker != null) {
                    openDetailActivity(marker);
                    map.map().animator().animateTo(new MapPosition(marker.getPoint().getLatitude(), marker.getPoint().getLongitude(), 1 << 18));
                }
            }
        });


    }

    private void setupAudioControl(View view, Bundle savedInstanceState) {
        this.bottomFragmentContainer = view.findViewById(R.id.map_bottom_fragment);
        AudioControlBottomFragment audioFragment = AudioControlBottomFragment.getInstance(this.getRouteId());
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.map_bottom_fragment, audioFragment);
        transaction.commit();
    }

    /*private void setupMapOffline(Bundle savedInstanceState) {
        FileProvider fileProvider = new FileProvider(requireActivity().getApplicationContext());

        XYTileSource source = new XYTileSource("whv", 13, 19, 256, ".png", new String[]{""});

        SimpleRegisterReceiver receiver = new SimpleRegisterReceiver(this.requireContext().getApplicationContext());

        IArchiveFile[] files = new IArchiveFile[]{ArchiveFileFactory.getArchiveFile(new File(fileProvider.getMapsRoot(), "whv.zip"))};

        MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(receiver, source, files);
        MapTileProviderBase provider = new MapTileProviderArray(source, receiver, new MapTileModuleProviderBase[]{archiveProvider});
        map.setTileProvider(provider);
        setupMap(savedInstanceState);
    }*/

    private void setupMapOfflineVTM(View view, Bundle savedInstanceState) {

        map = view.findViewById(R.id.mapview);
        MapPreferences pref = new MapPreferences(requireActivity().getClass().getName(), requireContext());
        pref.load(map.map());

        FileProvider provider = new FileProvider(requireContext());
        tileSource = new MapFileTileSource();
        try {
            FileInputStream stream = new FileInputStream(provider.getMapsFile(mapPath));
            tileSource.setMapFileInputStream(stream);
            VectorTileLayer tileLayer = map.map().setBaseMap(tileSource);
            map.map().layers().add(new LabelLayer(map.map(), tileLayer));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setupMap(savedInstanceState);


    }

    /**
     * does setup the bottom sheet
     *
     * @param savedInstanceState saved data
     */
    private void setupBottomSheet(View view, Bundle savedInstanceState) {

        bottomSheetView = view.findViewById(R.id.detailInfoSheet);
        bottomSheetController = new BottomSheetController(getChildFragmentManager(), bottomSheetView);
    }

    /**
     * general settings which are the same if the online or offline setup is loaded
     *
     * @param savedInstanceState default savedInstanceState
     */
    private void setupMap(Bundle savedInstanceState) {


        map.map().viewport().setMaxZoomLevel(20);
        map.map().viewport().setMinZoomLevel(12);
        map.map().viewport().setMapLimit(tileSource.getMapInfo().boundingBox);


        pathLayerStyle = LineStyle.builder().color(DEFAULT_PATH_COLOR).strokeWidth(4 * getResources().getDisplayMetrics().density);
        pathLayer = new PathLayer(map.map(), pathLayerStyle.build());
        map.map().layers().add(pathLayer);

        theme = map.map().setTheme(VtmThemes.DEFAULT);

        MapScaleBar mapScaleBar = new DefaultMapScaleBar(map.map());
        MapScaleBarLayer mapScaleBarLayer = new MapScaleBarLayer(map.map(), mapScaleBar);
        mapScaleBarLayer.getRenderer().setPosition(GLViewport.Position.BOTTOM_LEFT);
        mapScaleBarLayer.getRenderer().setOffset(5 * CanvasAdapter.getScale(), 0);

        map.map().layers().add(mapScaleBarLayer);


        markerLayer = new ItemizedLayer(map.map(), new ArrayList<MarkerInterface>(), getDefaultMarkerSymbol(), new ItemizedLayer.OnItemGestureListener<MarkerInterface>() {
            @Override
            public boolean onItemSingleTapUp(int index, MarkerInterface item) {

                if (item instanceof WPMarkerWD) {
                    WPMarkerWD marker = (WPMarkerWD) item;
                    BottomSheetController info = controllerProvider.getController();
                    if (info != null) {
                        if (info.isOpen()) {
                            if (info.getAttachedTo().getId() == marker.getWaypoint().getId()) {
                                info.close();
                            } else {
                                openInfoWindow(marker);
                            }

                        } else {
                            openInfoWindow(marker);
                        }
                        return true;
                    }
                }

                return false;
            }

            private void openInfoWindow(WPMarkerWD item) {
                openDetailActivity(item);
                GeoPoint coord = item.getWaypoint().getGeoPosition();
                map.map().animator().animateTo(coord);
            }

            @Override
            public boolean onItemLongPress(int index, MarkerInterface item) {
                return false;
            }
        });

        map.map().layers().add(markerLayer);

        locationLayer = new LocationLayer(map.map());
        map.map().layers().add(locationLayer);
        locationFollower = new LocationFollower(map.map());

        LocationRequest request = LocationRequest.create();
        request.setInterval(0);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        this.registerLocationCallback(request, locationFollower);

        map.map().input.bind(new Map.InputListener() {
            @Override
            public void onInputEvent(Event e, MotionEvent motionEvent) {
                setFollowLocation(false);
            }
        });
    }

    @Override
    public BottomSheetController getController() {
        if (bottomSheetController == null)
            bottomSheetController = new BottomSheetController(getChildFragmentManager(), bottomSheetView);
        return bottomSheetController;
    }

    @Override
    public void onNewInstruction(Location currentLocation, Instruction instruction) {

        directionsFragment.setCurrentInstruction(instruction);
    }

    private void openDetailActivity(WPMarkerWD waypoint) {
        BottomSheetController controller = controllerProvider.getController();
        controller.setAttachedTo(waypoint.getWaypoint());
        controller.open();
        /*Intent i = new Intent(this.requireContext(), DetailActivity.class);
        i.putExtra(DetailActivity.ID_EXTRA, waypointId);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        this.startActivity(i);*/
    }

    public boolean isFollowLocation() {
        return followLocation;
    }

    public void setFollowLocation(boolean followLocation) {
        this.followLocation = followLocation;
        if (locationFollower != null) {
            locationFollower.setEnabled(followLocation);
        }
    }

    private void centerMapOnLocation() {
        setFollowLocation(true);
    }

    private Drawable getDefaultIcon() {
        return ContextCompat.getDrawable(requireContext(), R.drawable.ic_anker_hellblau);
    }

    private WPMarkerWD getMarker(long id) {
        for (MarkerInterface item : markerLayer.getItemList()) {
            if (item instanceof WPMarkerWD) {
                WPMarkerWD marker = (WPMarkerWD) item;

                if (marker.getWaypoint() != null && marker.getWaypoint().getId() == id) {
                    return marker;
                }
            }
        }
        return null;
    }

    private MarkerSymbol getDefaultMarkerSymbol() {
        Bitmap bitmap = BitmapFactory.decodeResource(requireContext().getResources(), R.drawable.ic_anker_hellblau);
        return new MarkerSymbol(new AndroidBitmap(bitmap), MarkerSymbol.HotspotPlace.BOTTOM_CENTER);
    }

    private class RouteObserver implements Observer<RouteWithWaypoints> {


        @Override
        public void onChanged(RouteWithWaypoints routeWithWaypoints) {
            if (routeWithWaypoints == null) {
                return;
            }
            Integer color = routeWithWaypoints.getPoiRoute().getNavigationPathColor();
            if (color != null) {
                pathLayerStyle.color(color);
                pathLayer.setStyle(pathLayerStyle.build());
            }


            final ArrayList<GeoPoint> waypoints = new ArrayList<>();

            markerLayer.removeAllItems();
            POIWaypointWithMedia nextWaypoint = routeWithWaypoints.getNextWaypoint();
            if (nextWaypoint == null) {
                directionsFragment.getView().setVisibility(View.GONE);
            } else {
                directionsFragment.getView().setVisibility(View.VISIBLE);
                directionsFragment.setTitle(nextWaypoint.getTitle());
            }


            for (POIWaypointWithMedia waypoint : routeWithWaypoints.getWaypoints()) {


                waypoints.add(waypoint.getGeoPosition());
                WPMarkerWD m = new WPMarkerWD();
                m.setWaypoint(waypoint);
                boolean isNext = waypoint == nextWaypoint;
                int iconResource = R.drawable.ic_anker_beige;
                if (waypoint.isVisited())
                    iconResource = R.drawable.ic_anker_hellblau;
                if (isNext) {
                    iconResource = R.drawable.ic_anker_dunkelblau;
                    m.setAnchorOffsetY(0.7);
                }
                m.setIcon(ContextCompat.getDrawable(requireActivity(), iconResource));
                if (isNext)
                    markerLayer.addItem(0, m); // draw marker on top
                else
                    markerLayer.addItem(m);
            }

            locationObserver.invalidate();

            final BoundingBox bounds = routeWithWaypoints.getBounds();


            if (!bounds.contains(map.map().getMapPosition().getGeoPoint())) {
                // Does result in a delay to wait until the map is finished initializing
                map.map().post(new Runnable() {
                    @Override
                    public void run() {
                        map.map().animator().animateTo(bounds);
                    }
                });

            }
            map.invalidate();
        }
    }
}