package com.example.mobileapp.core.auth;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobileapp.R;

public class RegisterFragment extends Fragment {

    private EditText etName, etSurname, etEmail, etPhone, etAddress, etPassword, etConfirmPassword;
    private ImageView ivProfile;
    private Button btnRegister;
    private TextView tvSignIn;
    private Uri profileUri;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        initViews(view);
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.etName);
        etSurname = view.findViewById(R.id.etSurname);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        ivProfile = view.findViewById(R.id.ivProfile);
        btnRegister = view.findViewById(R.id.btnRegister);
        tvSignIn = view.findViewById(R.id.tvSignIn);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        tvSignIn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_view, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        });

    }
}
