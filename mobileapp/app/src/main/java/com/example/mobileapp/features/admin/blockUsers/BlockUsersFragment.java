package com.example.mobileapp.features.admin.blockUsers;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.mobileapp.R;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.RidesApi;
import com.example.mobileapp.features.shared.api.UserApi;
import com.example.mobileapp.features.shared.api.dto.HistoryReportDto;
import com.example.mobileapp.features.shared.input.UserSearchInputView;
import com.example.mobileapp.features.shared.models.User;
import com.example.mobileapp.features.shared.models.enums.UserRole;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlockUsersFragment extends Fragment {

    private UserSearchInputView userSearch;
    private AppCompatButton btnBlock, btnUnblock;
    private TextInputEditText etReason;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_block_users, container, false);

        initViews(v);
        initListeners();

        return v;
    }

    private void initViews(@NonNull View v) {

        userSearch = v.findViewById(R.id.user_search);

        btnBlock = v.findViewById(R.id.btn_block);
        btnUnblock = v.findViewById(R.id.btn_unblock);

        etReason = v.findViewById(R.id.et_reason);
    }

    private void initListeners() {
        btnBlock.setOnClickListener(v -> blockSelectedUser());

        btnUnblock.setOnClickListener(v -> unblockSelectedUser());
    }

    private void blockSelectedUser() {
        if (userSearch.getSelectedUser()==null) {
            showMessage("Please select a user.", false);
            return;
        }

        if (userSearch.getSelectedUser().getRole() == UserRole.ADMIN) {
            showMessage("Unable to block admin.", false);
            return;
        }

        String reason = etReason.getText().toString().trim();
        if(reason.isEmpty()) {
            showMessage("Please write the reason for blocking this user.", false);
            return;
        }

        Integer id = userSearch.getSelectedUser().getId();
        RequestBody body = RequestBody.create(reason, okhttp3.MediaType.parse("text/plain"));

        ApiClient.get().create(UserApi.class).blockUser(id, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(!response.isSuccessful()) {
                    showMessage("Failed to block user: " + response.code(), false);
                    return;
                }
                showMessage("User successfully blocked.", true);
                User user = userSearch.getSelectedUser();
                user.setBlocked(true);
                userSearch.setSelectedUser(user);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showMessage("Failed to block user: " + t.getMessage(), false);
            }
        });
    }

    private void unblockSelectedUser() {
        if (userSearch.getSelectedUser()==null) {
            showMessage("Please select a user.", false);
            return;
        }

        if (!userSearch.getSelectedUser().isBlocked()) {
            showMessage("User is not blocked.", false);
            return;
        }

        Integer id = userSearch.getSelectedUser().getId();

        ApiClient.get().create(UserApi.class).unblockUser(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(!response.isSuccessful()) {
                    showMessage("Failed to unblock user: " + response.code(), false);
                    return;
                }
                showMessage("User successfully unblocked.", true);
                User user = userSearch.getSelectedUser();
                user.setBlocked(false);
                userSearch.setSelectedUser(user);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showMessage("Failed to unblock user: " + t.getMessage(), false);
            }
        });
    }
    private void showMessage(String message, boolean isSuccess) {
        Snackbar snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(isSuccess ?
                getResources().getColor(R.color.completed_ride) :
                getResources().getColor(com.google.android.material.R.color.design_default_color_error));
        snackbar.setTextColor(Color.WHITE);
        snackbar.show();
    }

    private boolean tooManyMonthsApart(LocalDate start, LocalDate end, int months) {
        if(end.isBefore(start)) {
            LocalDate tmpDate = start;
            start = end;
            end = tmpDate;
        }

        LocalDate plusMonths = start.plusMonths(months);
        return plusMonths.isBefore(end);
    }

}
