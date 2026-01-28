package com.example.mobileapp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mobileapp.R;

public class AuthActivity extends AppCompatActivity {

    private Button btnLogin;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

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
}
