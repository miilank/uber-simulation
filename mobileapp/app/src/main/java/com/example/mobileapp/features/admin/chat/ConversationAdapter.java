package com.example.mobileapp.features.admin.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.api.dto.ConversationPreviewDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {

    private final List<ConversationPreviewDto> conversations = new ArrayList<>();
    private Integer selectedUserId = null;
    private OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Integer userId);
    }

    public ConversationAdapter(OnConversationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConversationPreviewDto conversation = conversations.get(position);
        holder.bind(conversation, conversation.getUserId().equals(selectedUserId));
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public void setConversations(List<ConversationPreviewDto> conversations) {
        this.conversations.clear();
        this.conversations.addAll(conversations);
        notifyDataSetChanged();
    }

    public void setSelectedUserId(Integer userId) {
        this.selectedUserId = userId;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView lastMessage;
        TextView time;
        TextView unreadBadge;
        TextView roleBadge;

        ViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            time = itemView.findViewById(R.id.time);
            unreadBadge = itemView.findViewById(R.id.unreadBadge);
            roleBadge = itemView.findViewById(R.id.roleBadge);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    ConversationPreviewDto conversation = conversations.get(position);
                    listener.onConversationClick(conversation.getUserId());
                }
            });
        }

        void bind(ConversationPreviewDto conversation, boolean isSelected) {
            userName.setText(conversation.getUserName());
            lastMessage.setText(truncateMessage(conversation.getLastMessage(), 40));
            time.setText(formatTime(conversation.getLastMessageTime()));

            // Unread badge
            if (conversation.getUnreadCount() > 0) {
                unreadBadge.setVisibility(View.VISIBLE);
                String count = conversation.getUnreadCount() > 9 ? "9+" : String.valueOf(conversation.getUnreadCount());
                unreadBadge.setText(count);
                lastMessage.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                unreadBadge.setVisibility(View.GONE);
                lastMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            // Role badge
            roleBadge.setText(conversation.getUserRole());
            int bgResource = getRoleBadgeBackground(conversation.getUserRole());
            roleBadge.setBackgroundResource(bgResource);

            // Selection highlight
            itemView.setBackgroundResource(isSelected ? R.drawable.bg_card : android.R.color.transparent);
        }

        private String truncateMessage(String message, int maxLength) {
            if (message.length() > maxLength) {
                return message.substring(0, maxLength) + "...";
            }
            return message;
        }

        private String formatTime(LocalDateTime timestamp) {
            if (timestamp == null) return "";

            LocalDateTime now = LocalDateTime.now();
            long minutes = ChronoUnit.MINUTES.between(timestamp, now);
            long hours = ChronoUnit.HOURS.between(timestamp, now);
            long days = ChronoUnit.DAYS.between(timestamp, now);

            if (minutes < 1) return "Just now";
            if (minutes < 60) return minutes + "m ago";
            if (hours < 24) return hours + "h ago";
            if (days < 7) return days + "d ago";

            return timestamp.getMonthValue() + "/" + timestamp.getDayOfMonth();
        }

        private int getRoleBadgeBackground(String role) {
            switch (role) {
                case "DRIVER":
                    return R.drawable.bg_role_driver;
                case "PASSENGER":
                    return R.drawable.bg_role_passenger;
                default:
                    return R.drawable.bg_neutral;
            }
        }
    }
}