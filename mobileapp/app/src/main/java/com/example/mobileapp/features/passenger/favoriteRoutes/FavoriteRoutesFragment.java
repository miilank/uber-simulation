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
        // Inflate the layout for this fragment
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

//    private void fetchRoutesTest() {
//        routes.clear();
//
//        FavoriteRouteDto r1 = new FavoriteRouteDto();
//        r1.setId(1);
//        r1.setName("Home → Office");
//
//        LocationDto r1Start = new LocationDto();
//        r1Start.setLatitude(44.7866);
//        r1Start.setLongitude(20.4489);
//        r1Start.setAddress("123 Home St");
//
//        LocationDto r1End = new LocationDto();
//        r1End.setLatitude(44.8170);
//        r1End.setLongitude(20.4569);
//        r1End.setAddress("456 Office Ave");
//
//        LocationDto r1Wp1 = new LocationDto();
//        r1Wp1.setAddress("Coffee Shop, 789 Cafe Rd");
//        LocationDto r1Wp2 = new LocationDto();
//        r1Wp2.setAddress("Gym, 101 Fitness Blvd");
//
//        r1.setStartLocation(r1Start);
//        r1.setEndLocation(r1End);
//        r1.setWaypoints(new ArrayList<>(Arrays.asList(r1Wp1, r1Wp2)));
//
//        r1.setVehicleType(VehicleType.STANDARD);
//
//        r1.setBabyFriendly(true);
//        r1.setPetsFriendly(false);
//        r1.setCreatedAt(java.time.LocalDateTime.now().minusDays(5));
//
//        // Route 2
//        FavoriteRouteDto r2 = new FavoriteRouteDto();
//        r2.setId(2);
//        r2.setName("Weekend Errands");
//
//        LocationDto r2Start = new LocationDto();
//        r2Start.setLatitude(44.8123);
//        r2Start.setLongitude(20.4522);
//        r2Start.setAddress("Market St");
//
//        LocationDto r2End = new LocationDto();
//        r2End.setLatitude(44.7999);
//        r2End.setLongitude(20.4844);
//        r2End.setAddress("Library Ln");
//
//        LocationDto r2Wp1 = new LocationDto();
//        r2Wp1.setAddress("Bakery, 12 Bread St");
//
//        r2.setStartLocation(r2Start);
//        r2.setEndLocation(r2End);
//        r2.setWaypoints(new ArrayList<>(Arrays.asList(r2Wp1)));
//
//        r2.setVehicleType(VehicleType.LUXURY);
//
//        r2.setBabyFriendly(false);
//        r2.setPetsFriendly(true);
//        r2.setCreatedAt(java.time.LocalDateTime.now().minusDays(30));
//
//        routes.add(r1);
//        routes.add(r2);
//
//        if (adapter != null) adapter.notifyDataSetChanged();
//    }


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

                routes = routes.stream().filter(route -> {
                    return route.getId() != id;
                }).collect(Collectors.toList());

                if (adapter != null) adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Could not delete route.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}