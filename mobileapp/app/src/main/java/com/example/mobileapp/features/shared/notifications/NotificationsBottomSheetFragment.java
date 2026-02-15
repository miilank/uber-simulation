package com.example.mobileapp.features.shared.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.UserMainActivity;
import com.example.mobileapp.features.passenger.currentride.CurrentRideFragment;
import com.example.mobileapp.features.shared.models.Notification;
import com.example.mobileapp.features.shared.repositories.NotificationRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class NotificationsBottomSheetFragment extends BottomSheetDialogFragment
        implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView tvEmpty;
    private NotificationRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = NotificationRepository.getInstance();

        recyclerView = view.findViewById(R.id.rv_notifications);
        tvEmpty = view.findViewById(R.id.tv_empty_notifications);

        adapter = new NotificationAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        repository.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications == null || notifications.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setNotifications(notifications);
            }
        });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        if (!notification.isRead()) {
            repository.markAsRead(notification.getId());
        }

        dismiss();

        if (notification.getRideId() != null) {
            if (requireActivity() instanceof UserMainActivity) {
                ((UserMainActivity) requireActivity()).setNavigationCheckedItem(R.id.nav_current_ride);
            }
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CurrentRideFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }
}