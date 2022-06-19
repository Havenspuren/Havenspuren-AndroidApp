package de.jadehs.vcg.layout.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.oscim.core.GeoPoint;

import java.util.ArrayList;

import de.jadehs.vcg.R;
import de.jadehs.vcg.data.db.models.POIWaypoint;
import de.jadehs.vcg.layout.dialogs.GreetingDialog;
import de.jadehs.vcg.services.NearbyWaypointService;
import de.jadehs.vcg.view_models.MapViewModel;


public class MainActivity extends AppCompatActivity {
    //constants
    // intent
    public static final String EXTRA_ROUTE = "MAINACTIVITY.ROUTEID";
    public static final String PREF_ROUTES = "ROUTES_PREFS";


    // Preferences
    private static final String TAG = "MainActivity";
    // permissions
    private static final int REQUEST_PERMISSION_CODE = 98647;

    private static final String[] permissionsNeeded = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    // Data
    MapViewModel viewModel;
    private DrawerLayout drawerLayout;
    private NavController controller;
    private NavigationView navDrawer;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.ThemeOverlay_App_Typography);
        requestPermissionsIfNecessary(permissionsNeeded);


        setContentView(R.layout.activity_main);


        controller = NavHostFragment.findNavController(getSupportFragmentManager().findFragmentById(R.id.main_fragment));


        setupNavDrawer(savedInstanceState);

        setupToolBar(savedInstanceState);

        // needs to get called after setupToolBar and setupNavDrawer
        setupNavigationUI(savedInstanceState);


        // view model
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(this.getApplication())).get(MapViewModel.class);
        showGreetingMessageIfNeeded();


        controller.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                int id = destination.getId();
                // TODO hacked, make beautiful (maybe move toolbar to fragment)
                if (id == R.id.destination_map_fragment
                        || id == R.id.destination_poi_list
                        || id == R.id.destination_route_overview
                        || id == R.id.destination_trophy_map
                        || id == R.id.destination_trophy_overview
                        || id == R.id.destination_trophy_detail
                        || id == R.id.destination_info_fragment
                        || id == R.id.destination_contributors
                        || id == R.id.destination_route_info) {
                    toolbar.setVisibility(View.VISIBLE);
                } else {
                    toolbar.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        stopNearbyService();
    }

    /**
     * does open the drawer of the start side
     */
    private void openDrawer() {
        changeStateOfDrawer(true);
    }

    /**
     * does close the drawer of the start side
     */
    private void closeDrawer() {
        changeStateOfDrawer(false);
    }

    /**
     * does change the current state of the start side drawer
     *
     * @param open whether the drawer should get opened
     */
    private void changeStateOfDrawer(boolean open) {
        this.changeStateOfDrawer(GravityCompat.START, open);
    }

    /**
     * does change the current state of the specified drawer
     *
     * @param drawer which drawer is affected
     * @param open   whether the drawer should get opened
     */
    private void changeStateOfDrawer(int drawer, boolean open) {
        if (open && !this.drawerLayout.isDrawerOpen(drawer)) {
            this.drawerLayout.openDrawer(drawer);
        } else if (this.drawerLayout.isDrawerOpen(drawer)) {
            this.drawerLayout.closeDrawer(drawer);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean passed = true;
        boolean rationale = false;
        for (int i = 0; i < grantResults.length && (passed || !rationale); i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                passed = false;
                rationale = this.shouldShowRequestPermissionRationale(permissions[i]);
            }
        }
        if (rationale) {
            showPermissionDialog(permissions, false);
        } else if (!passed) {
            showPermissionDialog(permissions);
        }


    }

    public void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_DENIED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }


        if (permissionsToRequest.size() > 0) {
            requestPermissions(
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSION_CODE);
        }
    }

    private void showPermissionDialog(final String[] permissions) {
        showPermissionDialog(permissions, true);
    }

    private void showPermissionDialog(final String[] permissions, boolean withYes) {
        AlertDialog.Builder b = new AlertDialog.Builder(this).setMessage(R.string.permission_needed_information).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });

        if (withYes) {
            b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermissionsIfNecessary(permissions);
                }
            });
        }

        b.create().show();
    }

    /**
     * does setup the ToolBar
     *
     * @param savedInstanceState saved data
     */
    private void setupToolBar(Bundle savedInstanceState) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
    }

    /**
     * does setup the navDrawer
     * <p>
     * does register the itemSelectedListener
     *
     * @param savedInstanceState saved data
     */
    private void setupNavDrawer(Bundle savedInstanceState) {
        drawerLayout = findViewById(R.id.drawer_layout);

        navDrawer = findViewById(R.id.navigation_view);
    }

    private void setupNavigationUI(Bundle savedInstanceState) {

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navDrawer.getMenu())
                .setOpenableLayout(drawerLayout)
                .setFallbackOnNavigateUpListener(new AppBarConfiguration.OnNavigateUpListener() {
                    @Override
                    public boolean onNavigateUp() {
                        return onSupportNavigateUp();
                    }
                })
                .build();
        NavigationUI.setupWithNavController(toolbar, controller, appBarConfiguration);
        NavigationUI.setupWithNavController(navDrawer, controller);
        navDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                if (!NavigationUI.onNavDestinationSelected(item, controller)) {
                    int id = item.getItemId();
                    if (id == R.id.feedback_item) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://www.empirio.de/s/WNmYLdLnlZ"));
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException exception) {
                            Toast.makeText(getBaseContext(), R.string.error_no_browser, Toast.LENGTH_LONG).show();
                        } finally {
                            drawerLayout.close();
                        }


                    } else if (id == R.id.support_item) {
                        // TODO change to url parameters to conform the official documentation (https://medium.com/@cketti/android-sending-email-using-intents-3da63662c58f)
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"student.jadehs@googlemail.com"});
                        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_mail_subject));
                        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedback_mail_text));
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException exception) {
                            Toast.makeText(getBaseContext(), R.string.error_no_email, Toast.LENGTH_LONG).show();
                        } finally {
                            drawerLayout.close();
                        }

                    }
                } else {
                    drawerLayout.close();
                    return true;
                }
                return false;
            }
        });
    }


    /**
     * Various Debug purposes
     */
    public void onTestClick(View view) {

    }


    private void startService(long routeID, POIWaypoint waypoint) {
        Intent intent = new Intent(this, NearbyWaypointService.class);
        intent.putExtra(NearbyWaypointService.EXTRA_MAPID, routeID);
        intent.putExtra(NearbyWaypointService.EXTRA_WPID, waypoint.getId());
        GeoPoint point = waypoint.getGeoPosition();
        intent.putExtra(NearbyWaypointService.EXTRA_WPLAT, point.getLatitude());
        intent.putExtra(NearbyWaypointService.EXTRA_WPLNG, point.getLongitude());
        this.startService(intent);
    }

    private void stopNearbyService() {
        this.stopService(new Intent(this, NearbyWaypointService.class));
    }


    private void showGreetingMessageIfNeeded() {
        final String greeting_key = "de.jadehs.greeting_showed";
        SharedPreferences preferences = getSharedPreferences("GENERAL", MODE_PRIVATE);

        if (!preferences.getBoolean(greeting_key, false)) {
            showGreetingMessage();
            preferences.edit().putBoolean(greeting_key, true).apply();
        }
    }

    private void showGreetingMessage() {
        new GreetingDialog().show(getSupportFragmentManager(), null);
    }


}
