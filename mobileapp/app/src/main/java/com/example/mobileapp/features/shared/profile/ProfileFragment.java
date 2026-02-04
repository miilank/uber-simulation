package com.example.mobileapp.features.shared.profile;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.fragment.app.Fragment;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.models.User;

import java.io.File;
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
    private Uri profileUri;
    private final ActivityResultLauncher<String[]> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri result) {
                            if (result != null) {
                                profileUri = result;
                                ivProfile.setImageURI(profileUri);
                            }
                        }
                    }
            );

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
        ivProfile.setOnClickListener(v -> {
            imagePickerLauncher.launch(new String[]{"image/*"});
        });


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
    }

    private void updateProfile() {
        String first = etName.getText().toString().trim();
        String last   = etLastName.getText().toString().trim();
        String address= etAddress.getText().toString().trim();
        String phone  = etPhone.getText().toString().trim();

        String errorMsg = validateEditableUser(first, last, address, phone);


        if (errorMsg != null) {
            tvErrorMessage.setText(errorMsg);
            return;
        }
        MultipartBody.Part imagePart = null;

        if (profileUri != null) {
            try {
                File imageFile = new File(getRealPathFromURI(profileUri));
                MediaType mediaType = MediaType.parse("image/*");
                RequestBody imageBody = RequestBody.create(imageFile, mediaType);
                imagePart = MultipartBody.Part.createFormData("profileImage", imageFile.getName(), imageBody);
            } catch (Exception e) {
                tvErrorMessage.setText("Error fetching image.");
                return;
            }
        }
        tvErrorMessage.setText(null);


        // TODO: Backend
    }

    private String validateEditableUser(
            String first,
            String last,
            String address,
            String phone
    ) {
        if (first == null || first.isEmpty()) {
            return "Please enter a first name.";
        }
        if (last == null || last.isEmpty()) {
            return "Please enter a last name.";
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


    private boolean isPhoneValid(String phone) {
        if (phone == null) return false;
        String digitsOnly = phone.replaceAll("\\D", "");
        if (digitsOnly.length() < 7) return false;

        // digits, +, -, space, parentheses, dot
        String allowed = "^[\\d+\\-\\s().]+$";
        return Pattern.compile(allowed).matcher(phone).matches();
    }


    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireActivity().getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }
}