package com.example.mobileapp.features.driver.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.mobileapp.R;

import java.util.regex.Pattern;

public class ProfileFragment extends Fragment {

    private TextView tvFullName, tvEmail, tvVehicleModel, tvVehicleType, tvLicensePlate, tvSeats,
            tvInfantSupport, tvPetSupport, tvTimeWorkedValue, tvErrorMessage;
    private EditText etName, etLastName, etEmail, etPhone, etAddress;

    private AppCompatButton btnChangePswd, btnUpdateProfile, btnCancel;

    private ProgressBar barTimeWorked;

    public ProfileFragment() {}

    @Override
    public View onCreateView(
            @NonNull
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        btnChangePswd.setOnClickListener(v -> {
            ChangePasswordDialogFragment dialog = new ChangePasswordDialogFragment();
            dialog.show(getParentFragmentManager(), "change_password");
        });
    }


    private void initViews(View root) {
        // TextViews
        tvFullName       = (TextView) root.findViewById(R.id.tv_full_name);
        tvEmail          = (TextView) root.findViewById(R.id.tv_email);
        tvVehicleModel   = (TextView) root.findViewById(R.id.tv_vehicle_model);
        tvVehicleType    = (TextView) root.findViewById(R.id.tv_vehicle_type);
        tvLicensePlate   = (TextView) root.findViewById(R.id.tv_license_plate);
        tvSeats          = (TextView) root.findViewById(R.id.tv_seats);
        tvInfantSupport  = (TextView) root.findViewById(R.id.tv_infant_support);
        tvPetSupport     = (TextView) root.findViewById(R.id.tv_pet_support);
        tvTimeWorkedValue = (TextView) root.findViewById(R.id.tv_time_worked_value);
        tvErrorMessage = (TextView) root.findViewById(R.id.tv_error_message);

        // EditTexts
        etName    = (EditText) root.findViewById(R.id.et_first_name);
        etLastName= (EditText) root.findViewById(R.id.et_last_name);
        etEmail   = (EditText) root.findViewById(R.id.et_email);
        etPhone   = (EditText) root.findViewById(R.id.et_phone);
        etAddress = (EditText) root.findViewById(R.id.et_address);

        // Buttons
        btnChangePswd   = (AppCompatButton) root.findViewById(R.id.btn_change_password);
        btnUpdateProfile= (AppCompatButton) root.findViewById(R.id.btn_update_profile);
        btnCancel       = (AppCompatButton) root.findViewById(R.id.btn_cancel);

        // ProgressBar
        barTimeWorked = (ProgressBar) root.findViewById(R.id.progress_time_worked);
    }

    private void updateProfile() {
        String first = etName.getText().toString().trim();
        String last   = etLastName.getText().toString().trim();
        String email  = etEmail.getText().toString().trim();
        String address= etAddress.getText().toString().trim();
        String phone  = etPhone.getText().toString().trim();

        String errorMsg = validateEditableUser(first, last, email, address, phone);

        if (errorMsg != null) {
            tvErrorMessage.setText(errorMsg);
            return;
        }

        tvErrorMessage.setText(null);

        // TODO: Backend
    }

    private String validateEditableUser(
            String first,
            String last,
            String email,
            String address,
            String phone
    ) {
        if (first == null || first.isEmpty()) {
            return "Please enter a first name.";
        }
        if (last == null || last.isEmpty()) {
            return "Please enter a last name.";
        }
        if (email == null || email.isEmpty()) {
            return "Please enter an email address.";
        }
        if (!isEmailValid(email)) {
            return "Please enter a valid email address.";
        }
        if (address == null || address.isEmpty()) {
            return "Please enter an address.";
        }
        if (phone == null || phone.isEmpty()) {
            return "Please enter a phone number.";
        }
        if (!isPhoneValid(phone)) {
            return "Invalid phone number.";
        }

        return null;
    }

    private boolean isEmailValid(String email) {
        if (email == null) return false;
        String emailRegex = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$";
        return Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE).matcher(email).matches();
    }

    private boolean isPhoneValid(String phone) {
        if (phone == null) return false;
        String digitsOnly = phone.replaceAll("\\D", "");
        if (digitsOnly.length() < 7) return false;

        // digits, +, -, space, parentheses, dot
        String allowed = "^[\\d+\\-\\s().]+$";
        return Pattern.compile(allowed).matcher(phone).matches();
    }
}