package com.example.mobileapp.features.passenger.favoriteRoutes;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mobileapp.R;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.driver.ridehistory.RideHistoryDetailsDialogFragment;
import com.example.mobileapp.features.shared.api.FavoriteRouteApi;
import com.example.mobileapp.features.shared.api.dto.FavoriteRouteDto;
import com.example.mobileapp.features.shared.api.dto.LocationDto;
import com.example.mobileapp.features.shared.models.enums.VehicleType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FavoriteRoutesFragment extends Fragment {

    private List<FavoriteRouteDto> routes = new ArrayList<FavoriteRouteDto>();
    private FavoriteRoutesAdapter adapter;
    private FavoriteRouteApi routesApi;

    public FavoriteRoutesFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favorite_routes, container, false);

        RecyclerView rv = v.findViewById(R.id.rv_routes);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FavoriteRoutesAdapter(routes, route ->
                FavoriteRouteDetailsDialogFragment.newInstance(route)
                        .show(getParentFragmentManager(), "route_details")
        );
        rv.setAdapter(adapter);

        routesApi = ApiClient.get().create(FavoriteRouteApi.class);

        getParentFragmentManager().setFragmentResultListener(
                "fav_route_delete", // key
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    int routeId = bundle.getInt("route_id", -1);
                    if (routeId != -1) {
                        deleteRouteById(routeId);
                    }
                }
        );

        fetchRoutes();

        return v;
    }


    private void fetchRoutes() {
        if (routesApi == null) return;

        routesApi.getFavoriteRoutes()
                .enqueue(new Callback<>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onResponse(@NonNull Call<List<FavoriteRouteDto>> call, @NonNull Response<List<FavoriteRouteDto>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(getContext(), "Failed to load routes.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        routes.clear();
                        routes.addAll(response.body());

                        if (adapter != null) adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<FavoriteRouteDto>> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to load routes.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteRouteById(int id) {
        routesApi.deleteFavoriteRoute(id).enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(), "Could not delete route.", Toast.LENGTH_SHORT).show();
                    return;
                }

                routes.removeIf(route -> route.getId() == id);

                if (adapter != null) adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Could not delete route.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}