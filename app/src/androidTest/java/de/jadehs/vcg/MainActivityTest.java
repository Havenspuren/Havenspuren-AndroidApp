package de.jadehs.vcg;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.jadehs.vcg.layout.activities.MainActivity;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {


    @Rule
    public final ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public final GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);


    @BeforeClass
    public static void setupFirst() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences preferences = context.getSharedPreferences("GENERAL", Context.MODE_PRIVATE);
        preferences.edit().putBoolean("de.jadehs.greeting_showed", true).commit();
    }


    @Test
    public void openRouteInfo() {

        onView(allOf(
                withId(R.id.continue_route_button),
                hasSibling(withText("Testroute1"))
        )).perform(click());
    }

}