package com.example.mobileapp.features.shared.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

import com.example.mobileapp.R;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.UserApi;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordDialogFragment extends DialogFragment {
    EditText etOldPassword, etNewPassword, etConfirmPassword;
    AppCompatButton btnCancel, btnConfirm;
    ImageButton btnClose;

    UserApi userApi;
    TextView tvErrorMessage;

    @Override
    public View onCreateView(
            @NonNull
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.dialog_fragment_change_password, container, false);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        btnConfirm.setOnClickListener(v -> submitPasswordChange());
        btnCancel.setOnClickListener(v -> dismiss());
        btnClose.setOnClickListener(v -> dismiss());

        userApi = ApiClient.get().create(UserApi.class);
    }

    private void submitPasswordChange() {
        String newPassword = etNewPassword.getText().toString();
        String oldPassword = etOldPassword.getText().toString();
        String errorMsg = validatePasswords();

        if (errorMsg != null) {
            tvErrorMessage.setText(errorMsg);
            return;
        }

        tvErrorMessage.setText(null);

        Map<String, String> body = new HashMap<>();
        body.put("oldPassword", oldPassword);
        body.put("newPassword", newPassword);

        userApi.changePassword(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                dismiss();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                tvErrorMessage.setText(t.getMessage());
            }
        });
    }

    private String validatePasswords() {
        String newPassword = etNewPassword.getText().toString();
        String oldPassword = etOldPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (oldPassword.isEmpty()) {
            return "Please enter your old password.";
        }
        if (newPassword.isEmpty()) {
            return "Please enter your new password.";
        }
        if (confirmPassword.isEmpty()) {
            return "Please confirm your new password.";
        }
        if (!(newPassword.equals(confirmPassword))) {
            return "The passwords do not match.";
        }
        if (newPassword.length() < 8) {
            return "Password must be at least 8 characters.";
        }
        return null;
    }


    private void initViews(View root) {
        etOldPassword = root.findViewById(R.id.et_old_password);
        etNewPassword = root.findViewById(R.id.et_new_password);
        etConfirmPassword = root.findViewById(R.id.et_confirm_password);

        btnCancel = root.findViewById(R.id.btn_cancel);
        btnConfirm = root.findViewById(R.id.btn_confirm);
        btnClose = root.findViewById(R.id.btn_close);

        tvErrorMessage = root.findViewById(R.id.tv_error_message);
    }
}
