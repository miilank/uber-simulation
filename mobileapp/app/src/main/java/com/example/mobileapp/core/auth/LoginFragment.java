package com.example.mobileapp.core.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobileapp.AdminMainActivity;
import com.example.mobileapp.DriverMainActivity;
import com.example.mobileapp.R;
import com.example.mobileapp.UserMainActivity;
import com.example.mobileapp.core.api.AuthApi;
import com.example.mobileapp.core.api.dto.LoginRequest;
import com.example.mobileapp.core.api.dto.LoginResponse;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.UserApi;
import com.example.mobileapp.features.shared.models.User;
import com.example.mobileapp.features.shared.models.enums.UserRole;
import com.example.mobileapp.features.shared.repositories.UserRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvSignUp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Use your existing layout
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        initViews(view);
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        tvSignUp = view.findViewById(R.id.tvSignUp);
    }

    private void setupClickListeners() {
        tvSignUp.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_view, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
               showError("All fields must be input in");
                return;
            }

            login(email, password);
        });
    }
    private void login(String email, String password) {
        AuthApi authApi = ApiClient.get().create(AuthApi.class);
        UserApi userApi = ApiClient.get().create(UserApi.class);
        LoginRequest request = new LoginRequest(email, password);

        authApi.login(request).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<LoginResponse> call,
                                   retrofit2.Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    String token = body.getToken();
                    String userEmail = body.getEmail();
                    String firstName = body.getFirstName();

                    // store what you need
                    SharedPreferences prefs = requireContext()
                            .getSharedPreferences("auth", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putString("jwt", token)
                            .putString("email", userEmail)
                            .putString("firstName", firstName)
                            .apply();

                    fetchUserProfile(userApi);
                } else {
                    showError("Invalid credentials");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // handle network error
            }
        });
    }
    private void fetchUserProfile(UserApi userApi) {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt", null);

        if (token == null) {
            showError("No token found");
            return;
        }
        userApi.fetchMe().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> meCall, Response<User> meResponse) {
                if (meResponse.isSuccessful() && meResponse.body() != null) {
                    User user = meResponse.body();
                    if (!user.isActivated()) {
                        showError("Please verify your email first");
                        return;
                    }

                    UserRepository.getInstance().setCurrentUser(user);

                    if (user.getRole() == UserRole.DRIVER) {
                        goToDriverMain();
                    } else if (user.getRole() == UserRole.PASSENGER) {
                        goToPassengerMain();
                    } else {
                        goToAdminMain();
                    }
                } else {
                    showError("Failed to load profile");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showError("Failed to load profile");
            }
        });
    }
    private void showError(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }
    private void goToDriverMain() {
        Intent intent = new Intent(requireActivity(), DriverMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void goToPassengerMain() {
        Intent intent = new Intent(requireActivity(), UserMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void goToAdminMain() {
        Intent intent = new Intent(requireActivity(), AdminMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
