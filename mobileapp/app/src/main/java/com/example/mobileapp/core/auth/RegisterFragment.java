package com.example.mobileapp.core.auth;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobileapp.R;
import com.example.mobileapp.core.api.AuthApi;
import com.example.mobileapp.core.api.dto.RegisterRequest;
import com.example.mobileapp.core.network.ApiClient;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private EditText etName, etSurname, etEmail, etPhone, etAddress, etPassword, etConfirmPassword;
    private ImageView ivProfile;
    private Button btnRegister;
    private TextView tvSignIn;
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
                                ivProfile.setImageURI(profileUri);
                            }
                        }
                    }
            );



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
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!password.equals(confirmPassword)) {
            showError("Passwords don't match");
            return;
        }


        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        AuthApi authApi = ApiClient.get().create(AuthApi.class);
        RegisterRequest request = new RegisterRequest(name, surname, email, phone, address, password, confirmPassword);

        MultipartBody.Part imagePart = null;

        if (profileUri != null) {
            try {
                File imageFile = new File(getRealPathFromURI(profileUri));
                MediaType mediaType = MediaType.parse("image/*");
                RequestBody imageBody = RequestBody.create(imageFile, mediaType);
                imagePart = MultipartBody.Part.createFormData("profileImage", imageFile.getName(), imageBody);
            } catch (Exception e) {
                handleRegisterError("Image error");
                return;
            }
        }

        // SINGLE CALL: pass null imagePart if no image
        authApi.register(request, imagePart).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                handleRegisterResponse(response);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                handleRegisterError("Network error");
            }
        });
    }
    private void handleRegisterResponse(Response<?> response) {
        progressBar.setVisibility(View.GONE);
        btnRegister.setEnabled(true);

        if (isAdded() && response.isSuccessful()) {
            showMessage("Registration successful!");
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_view, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        } else if (isAdded()) {
            showError("Registration failed");
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
}
