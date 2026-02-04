package com.example.mobileapp.features.shared.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.api.dto.DriverDto;
import com.example.mobileapp.features.shared.models.User;
import com.example.mobileapp.features.shared.models.enums.UserRole;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ProfileFragment extends Fragment {

    private TextView tvFullName, tvEmail, tvVehicleModel, tvVehicleType, tvLicensePlate, tvSeats,
            tvInfantSupport, tvPetSupport, tvTimeWorkedValue, tvErrorMessage;
    private EditText etName, etLastName, etEmail, etPhone, etAddress;

    private AppCompatButton btnChangePswd, btnUpdateProfile, btnCancel;
    private ImageView ivProfile;
    private ProgressBar barTimeWorked;
    private CardView cvDriverActivity, cvVehicleInfo;
    private Uri profileUri;
    ProfileViewModel viewModel;
    private final ActivityResultLauncher<String[]> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri result) {
                            if (result != null) {
                                profileUri = result;

                                Glide.with(ProfileFragment.this)
                                        .load(profileUri)
                                        .placeholder(R.drawable.img_defaultprofile)
                                        .error(R.drawable.img_defaultprofile)
                                        .circleCrop()
                                        .into(ivProfile);
                            }
                        }
                    }
            );

    public ProfileFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
    }

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

        ivProfile.setOnClickListener(v -> {
            imagePickerLauncher.launch(new String[]{"image/*"});
        });

        setupObservers();
    }

    private void initViews(View root) {
        // Profile pic
        ivProfile       = root.findViewById(R.id.profile_image);

        // TextViews
        tvFullName        = root.findViewById(R.id.tv_full_name);
        tvEmail           = root.findViewById(R.id.tv_email);
        tvVehicleModel    = root.findViewById(R.id.tv_vehicle_model);
        tvVehicleType     = root.findViewById(R.id.tv_vehicle_type);
        tvLicensePlate    = root.findViewById(R.id.tv_license_plate);
        tvSeats           = root.findViewById(R.id.tv_seats);
        tvInfantSupport   = root.findViewById(R.id.tv_infant_support);
        tvPetSupport      = root.findViewById(R.id.tv_pet_support);
        tvTimeWorkedValue = root.findViewById(R.id.tv_time_worked_value);
        tvErrorMessage    = root.findViewById(R.id.tv_error_message);

        // EditTexts
        etName    = root.findViewById(R.id.et_first_name);
        etLastName= root.findViewById(R.id.et_last_name);
        etEmail   = root.findViewById(R.id.et_email);
        etPhone   = root.findViewById(R.id.et_phone);
        etAddress = root.findViewById(R.id.et_address);

        // Buttons
        btnChangePswd   = root.findViewById(R.id.btn_change_password);
        btnUpdateProfile= root.findViewById(R.id.btn_update_profile);
        btnCancel       = root.findViewById(R.id.btn_cancel);

        // ProgressBar
        barTimeWorked = root.findViewById(R.id.progress_time_worked);

        // Cards
        cvDriverActivity = root.findViewById(R.id.card_driver_activity);
        cvVehicleInfo    = root.findViewById(R.id.card_vehicle_information);
    }

    private void setupObservers() {
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                populateUserData(user);
                if (user.getRole() == UserRole.DRIVER) {
                    viewModel.fetchDriverInfo();
                    viewModel.getDriverInfo().observe(getViewLifecycleOwner(), this::populateDriverData);
                } else {
                    cvDriverActivity.setVisibility(View.GONE);
                    cvVehicleInfo.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                tvErrorMessage.setText(error);
                tvErrorMessage.setVisibility(View.VISIBLE);
            } else {
                tvErrorMessage.setVisibility(View.GONE);
            }
        });

        viewModel.message.observe(getViewLifecycleOwner(), messageResult -> {
            if(messageResult != null) {
                showMessage(messageResult.message, messageResult.isSuccess);
            }
        });
    }

    private void populateUserData(User user) {
        tvFullName.setText(user.getFirstName() + " " + user.getLastName());
        tvEmail.setText(user.getEmail());

        etName.setText(user.getFirstName());
        etLastName.setText(user.getLastName());
        etEmail.setText(user.getEmail());
        etPhone.setText(user.getPhoneNumber());
        etAddress.setText(user.getAddress());

        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfilePicture())
                    .placeholder(R.drawable.img_defaultprofile)
                    .error(R.drawable.img_defaultprofile)
                    .circleCrop()
                    .into(ivProfile);
        } else {
            ivProfile.setImageResource(R.drawable.img_defaultprofile);
        }
    }

    private void populateDriverData(DriverDto driver) {
        tvVehicleModel.setText(driver.getVehicle().getModel());
        tvVehicleType.setText(driver.getVehicle().getType().getDisplayName());
        tvLicensePlate.setText(driver.getVehicle().getLicensePlate());
        tvSeats.setText(Integer.toString(driver.getVehicle().getSeatCount()));
        tvInfantSupport.setText(driver.getVehicle().isBabyFriendly() ? "Yes" : "No");
        tvPetSupport.setText(driver.getVehicle().isPetsFriendly() ? "Yes" : "No");

        Duration duration = Duration.ofMinutes((long) driver.getWorkedMinutesLast24h());
        long hours = duration.toHours();
        long mins = duration.minusHours(hours).toMinutes();
        @SuppressLint("DefaultLocale") String formatted = String.format("%dhrs %02dmins", hours, mins);

        tvTimeWorkedValue.setText(formatted);
        barTimeWorked.setProgress((int) driver.getWorkedMinutesLast24h());

        cvVehicleInfo.setVisibility(View.VISIBLE);
        cvDriverActivity.setVisibility(View.VISIBLE);
    }

    private void updateProfile() {
        String first = etName.getText().toString().trim();
        String last   = etLastName.getText().toString().trim();
        String address= etAddress.getText().toString().trim();
        String phone  = etPhone.getText().toString().trim();

        MultipartBody.Part imagePart = null;

        if (profileUri != null) {
            try {
                String mime = requireActivity().getContentResolver().getType(profileUri);
                if (mime == null) mime = "image/*";

                String filename = queryFileName(profileUri);
                if (filename == null) filename = "upload_image";

                byte[] bytes = readBytesFromUri(profileUri);
                RequestBody imageBody = RequestBody.create(bytes, MediaType.parse(mime));
                imagePart = MultipartBody.Part.createFormData("avatar", filename, imageBody);
            } catch (Exception e) {
                tvErrorMessage.setText("Error fetching image: " + e.getMessage());
                return;
            }
        }
        tvErrorMessage.setText(null);

        viewModel.updateUser(first, last, address, phone, imagePart);
    }

    private void showMessage(String message, boolean isSuccess) {
        Snackbar snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(isSuccess ?
                getResources().getColor(R.color.completed_ride) :
                getResources().getColor(com.google.android.material.R.color.design_default_color_error));
        snackbar.setTextColor(Color.WHITE);
        snackbar.show();
    }

    private String queryFileName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = requireActivity().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    return cursor.getString(nameIndex);
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    private byte[] readBytesFromUri(Uri uri) throws IOException {
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            is = requireActivity().getContentResolver().openInputStream(uri);
            if (is == null) throw new IOException("Unable to open input stream for URI");
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } finally {
            if (is != null) is.close();
            if (baos != null) baos.close();
        }
    }
}