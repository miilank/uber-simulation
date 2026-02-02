package com.example.mobileapp.features.driver.profile;

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

public class ChangePasswordDialogFragment extends DialogFragment {
    EditText etOldPassword, etNewPassword, etConfirmPassword;
    AppCompatButton btnCancel, btnConfirm;
    ImageButton btnClose;

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
    }

    private void submitPasswordChange() {
        String errorMsg = validatePasswords();

        if (errorMsg != null) {
            tvErrorMessage.setText(errorMsg);
            return;
        }

        tvErrorMessage.setText(null);

        dismiss();
        //TODO Backend
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
