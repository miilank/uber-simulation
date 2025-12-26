package com.example.mobileapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.mobileapp.R;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Root DrawerLayout (contains main content + navigation drawer)
    private DrawerLayout drawerLayout;

    // NavigationView that shows the menu items inside the drawer
    private NavigationView navigationView;

    // Buttons from the custom header inside the Toolbar
    private ImageButton btnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Root view is the DrawerLayout with id "main"
        drawerLayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Get header view from NavigationView
        View headerView = navigationView.getHeaderView(0);

        // Find close button inside header layout
        View closeDrawerBtn = headerView.findViewById(R.id.close_drawer_btn);

        // Set click listener to close the drawer
        closeDrawerBtn.setOnClickListener(v ->
                drawerLayout.closeDrawer(GravityCompat.START)
        );

        // Toolbar that holds the custom header (toolbar_header)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get hamburger and profile buttons from toolbar_header
        btnMenu = toolbar.findViewById(R.id.btn_menu);

        // Open the navigation drawer when the hamburger button is clicked
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Listen for navigation item clicks from the drawer menu
        navigationView.setNavigationItemSelectedListener(this);

        // Initial fragment when opening MainActivity
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RideHistoryFragment())
                    .commit();

            // Mark ride history as selected in the drawer
            navigationView.setCheckedItem(R.id.nav_ride_history);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle clicks on drawer menu items
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            // TODO: open DriverDashboardFragment when you create it

        } else if (id == R.id.nav_ride_history) {
            // Open RideHistoryFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RideHistoryFragment())
                    .commit();

        } else if (id == R.id.nav_booked_rides) {
            // TODO: open BookedRidesFragment

        } else if (id == R.id.nav_reports) {
            // TODO: open ReportsFragment

        } else if (id == R.id.nav_support) {
            // TODO: open SupportFragment

        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_sign_out) {
            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        // Always close the drawer after handling a click
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
