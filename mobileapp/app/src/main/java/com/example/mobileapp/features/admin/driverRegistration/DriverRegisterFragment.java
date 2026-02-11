package com.example.mobileapp.features.admin.driverRegistration;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.mobileapp.R;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.DriversApi;
import com.example.mobileapp.features.shared.api.dto.DriverCreationDto;
import com.example.mobileapp.features.shared.api.dto.VehicleDto;
import com.example.mobileapp.features.shared.models.enums.VehicleType;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverRegisterFragment extends Fragment {

    private EditText etName, etSurname, etEmail, etPhone, etAddress, etModel, etPlate, etSeats;
    private MaterialCheckBox cbCanTransportPets, cbCanTransportInfants;
    private AutoCompleteTextView actvVehicleType;
    private ImageView ivProfile;
    private Button btnRegister;
    private Uri profileUri;
    private ProgressBar progressBar;

    private final ActivityResultLauncher<String[]> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri result) {
                            if (result != null) {
                                profileUri = result;

                                Glide.with(DriverRegisterFragment.this)
                                        .load(profileUri)
                                        .placeholder(R.drawable.img_defaultprofile)
                                        .error(R.drawable.img_defaultprofile)
                                        .circleCrop()
                                        .into(ivProfile);
                            }
                        }
                    }
            );



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_register, container, false);

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
        ivProfile = view.findViewById(R.id.ivProfile);
        btnRegister = view.findViewById(R.id.btnRegister);
        progressBar = view.findViewById(R.id.progressBar);

        List<String> displayItems = new ArrayList<>();
        for (VehicleType vt : VehicleType.values()) {
            displayItems.add(vt.getDisplayName());
        }

        actvVehicleType = view.findViewById(R.id.actvVehicleType);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireActivity(),
                android.R.layout.simple_dropdown_item_1line,
                displayItems
        );
        actvVehicleType.setAdapter(adapter);
        actvVehicleType.setThreshold(0);
        actvVehicleType.setOnClickListener(v -> actvVehicleType.showDropDown());

        etModel = view.findViewById(R.id.etModel);
        etPlate = view.findViewById(R.id.etPlate);
        etSeats = view.findViewById(R.id.etSeats);

        cbCanTransportInfants = view.findViewById(R.id.cbCanTransportInfants);
        cbCanTransportPets = view.findViewById(R.id.cbCanTransportPets);
    }

    private void setupClickListeners() {
        ivProfile.setOnClickListener(v -> {
            imagePickerLauncher.launch(new String[]{"image/*"});
        });

        btnRegister.setOnClickListener(v -> performRegister());

    }
    private void performRegister() {
        String name = etName.getText().toString().trim();
        String surname = etSurname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        String model = etModel.getText().toString().trim();
        String plate = etPlate.getText().toString().trim();
        Integer seats = Integer.parseInt(etSeats.getText().toString());
        boolean pets = cbCanTransportPets.isChecked();
        boolean infants = cbCanTransportInfants.isChecked();

        VehicleType type = getSelectedVehicleType();

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        DriverCreationDto request = new DriverCreationDto();
        request.setEmail(email);
        request.setFirstName(name);
        request.setLastName(surname);
        request.setPhoneNumber(phone);
        request.setAddress(address);

        VehicleDto vehicle = new VehicleDto();
        vehicle.setModel(model);
        vehicle.setLicensePlate(plate);
        vehicle.setSeatCount(seats);
        vehicle.setPetsFriendly(pets);
        vehicle.setBabyFriendly(infants);
        vehicle.setType(type);

        request.setVehicle(vehicle);

        DriversApi api = ApiClient.get().create(DriversApi.class);


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
                handleRegisterError("Image error");
                return;
            }
        }

        api.createDriver(request, imagePart).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                handleRegisterResponse(response);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                handleRegisterError("Network error" + t.getMessage());
            }
        });

    }
    private void handleRegisterResponse(Response<?> response) {
        progressBar.setVisibility(View.GONE);
        btnRegister.setEnabled(true);

        if (isAdded() && response.isSuccessful()) {
            showMessage("Registration successful!");

        } else if (isAdded()) {
            showError("Registration failed:" + response.code());
        }
    }

    private void handleRegisterError(String message) {
        progressBar.setVisibility(View.GONE);
        btnRegister.setEnabled(true);
        if (isAdded()) {
            showError(message);
        }
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

    private void showError(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void showMessage(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show();
    }

    private VehicleType getSelectedVehicleType() {
        String selected = actvVehicleType.getText() == null ? null : actvVehicleType.getText().toString().trim();
        return VehicleType.fromDisplayName(selected);
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
