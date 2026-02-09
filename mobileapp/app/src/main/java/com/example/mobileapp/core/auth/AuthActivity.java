package com.example.mobileapp.core.auth;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mobileapp.AdminMainActivity;
import com.example.mobileapp.DriverMainActivity;
import com.example.mobileapp.R;
import com.example.mobileapp.UserMainActivity;
import com.example.mobileapp.features.shared.map.MapFragment;
import com.example.mobileapp.features.shared.models.User;
import com.example.mobileapp.features.shared.models.enums.UserRole;
import com.example.mobileapp.features.shared.repositories.UserRepository;

public class AuthActivity extends AppCompatActivity {

    private Button btnLogin;
    private Button btnRegister;
    private boolean isCheckingUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isCheckingUser = true;
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> isCheckingUser);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        UserRepository.getInstance().getCurrentUser().observe(this, user -> {
            if (user != null) {
                if (user.getRole() == UserRole.ADMIN) {
                    goToAdminMain();
                } else if (user.getRole() == UserRole.DRIVER) {
                    goToDriverMain();
                } else {
                    goToPassengerMain();
                }
            }
            isCheckingUser = false;
        });

        // --- Apply status bar inset to the public header (so it sits below the status bar) ---
        View header = findViewById(R.id.public_header);
        ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;

            // Add the top inset as padding (keeps original padding)
            v.setPadding(
                    v.getPaddingLeft(),
                    topInset,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );

            return insets;
        });

        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        // Default screen: Map (unregistered home)
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_view, new MapFragment())
                    .commit();
        }

        // Navigate to Login
        btnLogin.setOnClickListener(v -> getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_view, new LoginFragment())
                .addToBackStack(null)
                .commit()
        );

        // Navigate to Register
        btnRegister.setOnClickListener(v -> getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_view, new RegisterFragment())
                .addToBackStack(null)
                .commit()
        );

        // Clicking the brand always returns to the Map (home) and clears back stack
        findViewById(R.id.brand_container).setOnClickListener(v -> {
            getSupportFragmentManager().popBackStack(
                    null,
                    androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
            );

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_view, new MapFragment())
                    .commit();
        });
    }

    private void goToDriverMain() {
        Intent intent = new Intent(this, DriverMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void goToPassengerMain() {
        Intent intent = new Intent(this, UserMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void goToAdminMain() {
        Intent intent = new Intent(this, AdminMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
