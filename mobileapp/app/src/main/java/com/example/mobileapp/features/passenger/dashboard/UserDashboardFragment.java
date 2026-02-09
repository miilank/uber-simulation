package com.example.mobileapp.features.passenger.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.map.MapFragment;
import com.google.android.material.navigation.NavigationView;

public class UserDashboardFragment extends Fragment {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_dashboard, container, false);

        setupMapChild();

        return view;
    }


    private void setupMapChild() {
        if (getChildFragmentManager().findFragmentById(R.id.mapContainer) == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, new MapFragment())
                    .commit();
        }
    }
}
