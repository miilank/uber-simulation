package com.example.mobileapp.features.shared.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.models.Notification;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTime;
        View unreadIndicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
        }

        void bind(Notification notification) {
            tvMessage.setText(notification.getMessage());
            tvTime.setText(formatTime(notification.getCreatedAt()));

            unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

            itemView.setBackgroundResource(notification.isRead()
                    ? R.color.white
                    : R.color.notification_unread_bg);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }

        private String formatTime(LocalDateTime timestamp) {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(timestamp, now);

            long minutes = duration.toMinutes();
            if (minutes < 1) return "Just now";
            if (minutes < 60) return minutes + "m ago";

            long hours = duration.toHours();
            if (hours < 24) return hours + "h ago";

            long days = duration.toDays();
            if (days < 7) return days + "d ago";

            return timestamp.toLocalDate().toString();
        }
    }
}