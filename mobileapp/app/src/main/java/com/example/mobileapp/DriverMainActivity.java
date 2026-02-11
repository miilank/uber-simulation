package com.example.mobileapp;

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

import com.bumptech.glide.Glide;
import com.example.mobileapp.features.driver.dashboard.DriverDashboardFragment;
import com.example.mobileapp.features.driver.ridehistory.RideHistoryFragment;
import com.example.mobileapp.features.shared.chat.SupportChatFragment;
import com.example.mobileapp.features.shared.profile.ProfileFragment;
import com.example.mobileapp.features.shared.pages.historyReport.UserHistoryReportFragment;
import com.example.mobileapp.features.shared.pages.profile.ProfileFragment;
import com.example.mobileapp.features.shared.repositories.UserRepository;
import com.google.android.material.navigation.NavigationView;

import com.example.mobileapp.core.auth.AuthActivity;

import java.time.LocalDateTime;

public class DriverMainActivity extends AppCompatActivity
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
                    .replace(R.id.fragment_container, new DriverDashboardFragment())
                    .commit();

            // Mark ride history as selected in the drawer
            navigationView.setCheckedItem(R.id.nav_dashboard);
        }

        // Set pfp
        ImageButton profileImage = toolbar.findViewById(R.id.btn_profile);
        UserRepository.getInstance().getCurrentUser().observe(this, user -> {
            if (user != null && user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                Glide.with(this)
                        .load(user.getProfilePicture() + "?cb=" + LocalDateTime.now().toString())
                        .placeholder(R.drawable.img_defaultprofile)
                        .error(R.drawable.img_defaultprofile)
                        .circleCrop()
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.img_defaultprofile);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle clicks on drawer menu items
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new DriverDashboardFragment())
                    .addToBackStack(null)
                    .commit();


        } else if (id == R.id.nav_ride_history) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RideHistoryFragment())
                    .addToBackStack(null)
                    .commit();

        } else if (id == R.id.nav_booked_rides) {
            // TODO: open BookedRidesFragment

        } else if (id == R.id.nav_reports) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new UserHistoryReportFragment())
                    .addToBackStack(null)
                    .commit();

        } else if (id == R.id.nav_support) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SupportChatFragment())
                    .addToBackStack(null)
                    .commit();

        } else if (id == R.id.nav_profile) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();

        } else if (id == R.id.nav_sign_out) {
            UserRepository.getInstance().clearUser();
            Intent intent = new Intent(DriverMainActivity.this, AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        // Always close the drawer after handling a click
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
