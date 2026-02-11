package com.example.mobileapp.features.shared.input;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.models.User;
import com.example.mobileapp.features.shared.models.enums.UserRole;

import java.util.ArrayList;
import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserSearchVH> {

    public interface OnItemClick {
        void onClick(User user);
    }

    private final List<User> items;
    private Integer selectedUserId = null;
    private final OnItemClick onItemClick;

    public UserSearchAdapter(List<User> items, OnItemClick listener) {
        this.items = items;
        this.onItemClick = listener;
    }

    public void setSelectedUserId(Integer id) {
        this.selectedUserId = id;
        notifyDataSetChanged();
    }

    public Integer getSelectedUserId() { return selectedUserId; }

    @Override
    public UserSearchVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_user, parent, false);
        return new UserSearchVH(v);
    }

    @Override
    public void onBindViewHolder(UserSearchVH holder, int position) {
        final User u = items.get(position);
        holder.nameText.setText(u.getFirstName()+u.getLastName());
        holder.emailText.setText(u.getEmail());

        UserRole role = u.getRole();
        if (role == UserRole.ADMIN) {
            holder.roleBadge.setText("Admin");
            holder.roleBadge.setBackgroundColor(0xffdff4e0);
            holder.roleBadge.setTextColor(0xff2f7a2f);
        } else if (role == UserRole.DRIVER) {
            holder.roleBadge.setText("Driver");
            holder.roleBadge.setBackgroundColor(0xfff2f2f2);
            holder.roleBadge.setTextColor(0xff666666);
        } else {
            holder.roleBadge.setText("Passenger");
            holder.roleBadge.setBackgroundColor(0xfff2f2f2);
            holder.roleBadge.setTextColor(0xff666666);
        }

        if (u.isBlocked()) {
            holder.card.setCardBackgroundColor(0xffffefef); //light red
            holder.card.setPreventCornerOverlap(false);
        } else {
            holder.card.setCardBackgroundColor(0xffffffff);
        }

        if (u.getId() != null && u.getId().equals(selectedUserId)) {
            holder.card.setCardBackgroundColor(0xffe6f0ff); // blue-ish
            holder.nameText.setTypeface(null, Typeface.BOLD);
        } else {
            holder.nameText.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(u);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class UserSearchVH extends RecyclerView.ViewHolder {
        TextView nameText, emailText, roleBadge;
        CardView card;
        UserSearchVH(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            emailText = itemView.findViewById(R.id.emailText);
            roleBadge = itemView.findViewById(R.id.roleBadge);
            card = itemView.findViewById(R.id.card);
        }
    }
}
