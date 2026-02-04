package com.example.mobileapp.features.admin.profileChanges;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.passenger.favoriteRoutes.FavoriteRouteDetailsDialogFragment;
import com.example.mobileapp.features.passenger.favoriteRoutes.FavoriteRoutesAdapter;
import com.example.mobileapp.features.shared.api.DriversApi;
import com.example.mobileapp.features.shared.api.FavoriteRouteApi;
import com.example.mobileapp.features.shared.api.dto.DriverUpdateDto;
import com.example.mobileapp.features.shared.api.dto.FavoriteRouteDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileChangesFragment extends Fragment {

    private List<DriverUpdateDto> updates = new ArrayList<DriverUpdateDto>();
    private ProfileChangesAdapter adapter;
    private DriversApi driversApi;

    public ProfileChangesFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile_changes, container, false);

        RecyclerView rv = v.findViewById(R.id.rv_changes);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProfileChangesAdapter(updates,
                this::approveUpdate,
                this::rejectUpdate
        );
        rv.setAdapter(adapter);

        driversApi = ApiClient.get().create(DriversApi.class);

        fetchUpdates();

        return v;
    }


    private void fetchUpdates() {
        if (driversApi == null) return;

        driversApi.getPendingChanges()
                .enqueue(new Callback<List<DriverUpdateDto>>() {
                    @Override
                    public void onResponse(Call<List<DriverUpdateDto>> call, Response<List<DriverUpdateDto>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(getContext(), "Failed to load updates.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        updates.clear();
                        updates.addAll(response.body());

                        if (adapter != null) adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<List<DriverUpdateDto>> call, Throwable t) {
                        Toast.makeText(getContext(), "Failed to load updates.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void approveUpdate(DriverUpdateDto update) {
        driversApi.approveUpdate(update.getDriverId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            int idx = updates.indexOf(update);
                            if (idx != -1) {
                                updates.remove(idx);
                                adapter.notifyItemRemoved(idx);
                            }
                            Toast.makeText(requireContext(), "Successfully approved.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to approve: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void rejectUpdate(DriverUpdateDto update) {
        driversApi.rejectUpdate(update.getDriverId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            int idx = updates.indexOf(update);
                            if (idx != -1) {
                                updates.remove(idx);
                                adapter.notifyItemRemoved(idx);
                            }
                            Toast.makeText(requireContext(), "Successfully rejected.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to reject: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}