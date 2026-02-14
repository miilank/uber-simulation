package com.example.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.mobileapp.core.auth.AuthActivity;
import com.example.mobileapp.features.passenger.bookedRides.PassengerBookedRidesFragment;
import com.example.mobileapp.features.passenger.currentride.CurrentRideFragment;
import com.example.mobileapp.features.passenger.dashboard.UserDashboardFragment;
import com.example.mobileapp.features.passenger.favoriteRoutes.FavoriteRoutesFragment;
import com.example.mobileapp.features.passenger.rideBooking.RideBookingFragment;
import com.example.mobileapp.features.passenger.rideHistory.PassengerRideHistoryFragment;
import com.example.mobileapp.features.shared.chat.SupportChatFragment;
import com.example.mobileapp.features.shared.models.Notification;
import com.example.mobileapp.features.shared.notifications.NotificationsBottomSheetFragment;
import com.example.mobileapp.features.shared.pages.historyReport.UserHistoryReportFragment;
import com.example.mobileapp.features.shared.pages.profile.ProfileFragment;
import com.example.mobileapp.features.shared.repositories.NotificationRepository;
import com.example.mobileapp.features.shared.repositories.UserRepository;
import com.example.mobileapp.features.shared.services.WebSocketManager;
import com.google.android.material.navigation.NavigationView;

import java.time.LocalDateTime;

public class UserMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Root DrawerLayout (contains main content + navigation drawer)
    private DrawerLayout drawerLayout;

    // NavigationView that shows the menu items inside the drawer
    private NavigationView navigationView;

    // Buttons from the custom header inside the Toolbar
    private ImageButton btnMenu;

    // Notification system
    private WebSocketManager webSocketManager;
    private NotificationRepository notificationRepository;
    private TextView tvNotificationBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_main);

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

        // Setup notification bell and badge
        FrameLayout btnNotifications = toolbar.findViewById(R.id.btn_notifications);
        tvNotificationBadge = toolbar.findViewById(R.id.tv_notification_badge);
        btnNotifications.setOnClickListener(v -> showNotifications());

        // Initialize notification repository
        notificationRepository = NotificationRepository.getInstance();
        notificationRepository.loadNotifications();

        // Observe unread count and update badge
        notificationRepository = NotificationRepository.getInstance();
        notificationRepository.loadNotifications();

        notificationRepository.getHasUnread().observe(this, hasUnread -> {
            if (hasUnread != null && hasUnread) {
                tvNotificationBadge.setVisibility(View.VISIBLE);
            } else {
                tvNotificationBadge.setVisibility(View.GONE);
            }
        });

        // Setup Websocket
        webSocketManager = new WebSocketManager(this);
        UserRepository.getInstance().getCurrentUser().observe(this, user -> {
            if (user != null && user.getId() != null) {
                webSocketManager.connect(user.getId(), null);
                webSocketManager.setNotificationListener(notificationDto -> {
                    runOnUiThread(() -> {
                        Notification notification = notificationDto.toModel();
                        notification.setRead(false);
                        notificationRepository.addNotification(notification);
                    });
                });
            }
        });

        // Listen for navigation item clicks from the drawer menu
        navigationView.setNavigationItemSelectedListener(this);

        // Initial fragment when opening MainActivity
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new UserDashboardFragment())
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

    // Show notifications bottom sheet
    private void showNotifications() {
        NotificationsBottomSheetFragment bottomSheet = new NotificationsBottomSheetFragment();
        bottomSheet.show(getSupportFragmentManager(), "notifications");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle clicks on drawer menu items
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new UserDashboardFragment())
                    .addToBackStack(null)
                    .commit();


        } else if (id == R.id.nav_ride_history) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new PassengerRideHistoryFragment())
                    .addToBackStack(null)
                    .commit();

        } else if (id == R.id.nav_booked_rides) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new PassengerBookedRidesFragment())
                    .addToBackStack(null)
                    .commit();

        } else if (id == R.id.nav_favorite_routes) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new FavoriteRoutesFragment())
                    .addToBackStack(null)
                    .commit();

        } else if (id == R.id.nav_book_ride) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RideBookingFragment())
                    .addToBackStack(null)
                    .commit();

        } else if (id == R.id.nav_current_ride) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CurrentRideFragment())
                    .addToBackStack(null)
                    .commit();

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
            NotificationRepository.getInstance().clearAll();
            Intent intent = new Intent(UserMainActivity.this, AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        // Always close the drawer after handling a click
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setNavigationCheckedItem(int itemId) {
        if (navigationView != null) {
            navigationView.setCheckedItem(itemId);
        }
    }

    // Cleanup WebSocket on destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
    }
}