package de.jadehs.vcg.layout.fragments.route_view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.LinkedList;
import java.util.List;

import de.jadehs.vcg.BuildConfig;
import de.jadehs.vcg.data.db.pojo.RouteWithWaypoints;
import de.jadehs.vcg.view_models.RouteViewModel;
import de.jadehs.vcg.view_models.factories.RouteViewModelFactory;


/**
 *
 */
public abstract class RouteViewFragment extends Fragment {

    public static final String ARG_ROUTE_ID = "route_id";
    private static final String TAG = "RouteViewFragment";
    private long routeId;
    private RouteViewModel routeViewModel;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private List<LocationListener> locListeners;

    public RouteViewFragment() {
        // Required empty public constructor
        locListeners = new LinkedList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        routeId = 1;
        if (getArguments() == null || !getArguments().containsKey(ARG_ROUTE_ID)) {
            IllegalStateException ex = new IllegalStateException("Tried show a RouteViewFragment without passing an routeId as argument");
            if (BuildConfig.DEBUG) {
                throw ex;
            } else {
                Log.e(TAG, "Tried show a RouteViewFragment without passing an routeId as argument. Trying to continue with routeId 1", ex);
            }
        } else {
            routeId = getArguments().getLong(ARG_ROUTE_ID);
        }

        RouteViewModelFactory factory = new RouteViewModelFactory(requireActivity().getApplication(), routeId);
        routeViewModel = factory.getViewModel(requireActivity(), RouteViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerUnregisteredLocationCallbacks();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterRegisteredLocationCallbacks();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearLocationCallbacks();
    }

    public long getRouteId() {
        return routeId;
    }

    protected LiveData<RouteWithWaypoints> getCurrentRoute() {
        return this.routeViewModel.getCurrentRoute();
    }

    private void clearLocationCallbacks() {
        locListeners.clear();
    }

    protected void registerLocationCallback(LocationRequest request, LocationCallback callback) {
        LocationListener listener = new LocationListener(request, callback);
        this.locListeners.add(listener);
        if (this.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED) && fusedLocationProviderClient != null) {
            registerLocationCallback(listener);
        }
    }

    public RouteViewModel getRouteViewModel() {
        return routeViewModel;
    }

    private void registerLocationCallback(LocationListener listener) {
        if (fusedLocationProviderClient != null) {
            if (requireActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.requestLocationUpdates(listener.getLocRequest(), listener.getLocCallback(), Looper.getMainLooper());
                listener.setRegistered(true);
            } else {
                // TODO request Permission
            }
        }
    }

    private void registerUnregisteredLocationCallbacks() {
        if (fusedLocationProviderClient != null) {
            for (LocationListener listener :
                    this.locListeners) {
                if (!listener.isRegistered()) {
                    registerLocationCallback(listener);
                }
            }
        }
    }

    private void unregisterRegisteredLocationCallbacks() {
        if (fusedLocationProviderClient != null) {
            for (LocationListener listener :
                    this.locListeners) {
                if (listener.isRegistered()) {
                    unregisterLocationCallback(listener);
                }
            }
        }
    }

    private void unregisterLocationCallback(LocationListener listener) {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(listener.getLocCallback());
            listener.setRegistered(false);
        }

    }

    private static class LocationListener {
        private final LocationRequest locRequest;
        private final LocationCallback locCallback;
        private boolean registered;

        public LocationListener(LocationRequest locRequest, LocationCallback locCallback) {
            this.locRequest = locRequest;
            this.locCallback = locCallback;
            registered = false;
        }

        public boolean isRegistered() {
            return registered;
        }

        public void setRegistered(boolean registered) {
            this.registered = registered;
        }

        public LocationRequest getLocRequest() {
            return locRequest;
        }

        public LocationCallback getLocCallback() {
            return locCallback;
        }
    }

}