package com.example.mobileapp.features.admin.profileChanges;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobileapp.R;
import com.example.mobileapp.features.shared.api.dto.DriverUpdateDto;

import java.util.ArrayList;
import java.util.List;

public class ProfileChangesAdapter extends RecyclerView.Adapter<ProfileChangesAdapter.ChangeVH> {
    public interface OnApprovalListener {
        void onApproval(DriverUpdateDto update);
    }

    public interface OnRejectListener {
        void onReject(DriverUpdateDto update);
    }

    private final List<DriverUpdateDto> items;
    private final OnApprovalListener approvalListener;
    private final OnRejectListener rejectListener;

    public ProfileChangesAdapter(@NonNull List<DriverUpdateDto> items, OnApprovalListener approvalListener, OnRejectListener rejectListener) {
        this.items = items;
        this.approvalListener = approvalListener;
        this.rejectListener = rejectListener;
    }


    @NonNull
    @Override
    public ProfileChangesAdapter.ChangeVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile_change, parent, false);
        return new ProfileChangesAdapter.ChangeVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileChangesAdapter.ChangeVH holder, int position) {
        DriverUpdateDto update = items.get(position);

        holder.etOldFirst.setText(update.getOldFirstName());
        holder.etOldLast.setText(update.getOldLastName());
        holder.etOldEmail.setText(update.getEmail());
        holder.etOldAddress.setText(update.getOldAddress());
        holder.etOldPhone.setText(update.getOldPhoneNumber());

        holder.etNewFirst.setText(update.getNewFirstName());
        holder.etNewLast.setText(update.getNewLastName());
        holder.etNewEmail.setText(update.getEmail());
        holder.etNewAddress.setText(update.getNewAddress());
        holder.etNewPhone.setText(update.getNewPhoneNumber());

        holder.btnApprove.setOnClickListener(v -> {
            if (approvalListener != null) approvalListener.onApproval(update);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (rejectListener != null) rejectListener.onReject(update);
        });

        if (update.getOldProfilePicture() != null && !update.getOldProfilePicture().isEmpty()) {
            Glide.with(holder.itemView)
                    .load(update.getOldProfilePicture())
                    .placeholder(R.drawable.img_defaultprofile)
                    .error(R.drawable.img_defaultprofile)
                    .circleCrop()
                    .into(holder.ivOldProfile);
        } else {
            holder.ivOldProfile.setImageResource(R.drawable.img_defaultprofile);
        }

        if (update.getNewProfilePicture() != null && !update.getNewProfilePicture().isEmpty()) {
            Glide.with(holder.itemView)
                    .load(update.getNewProfilePicture())
                    .placeholder(R.drawable.img_defaultprofile)
                    .error(R.drawable.img_defaultprofile)
                    .circleCrop()
                    .into(holder.ivNewProfile);
        } else {
            holder.ivNewProfile.setImageResource(R.drawable.img_defaultprofile);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public static final class ChangeVH extends RecyclerView.ViewHolder {
        // Old
        final ImageView ivOldProfile;
        final EditText etOldFirst;
        final EditText etOldLast;
        final EditText etOldEmail;
        final EditText etOldAddress;
        final EditText etOldPhone;

        // New
        final ImageView ivNewProfile;
        final EditText etNewFirst;
        final EditText etNewLast;
        final EditText etNewEmail;
        final EditText etNewAddress;
        final EditText etNewPhone;

        // Actions
        final Button btnApprove;
        final Button btnReject;

        public ChangeVH(@NonNull View itemView) {
            super(itemView);

            // Old
            ivOldProfile = itemView.findViewById(R.id.profile_image_old);
            etOldFirst = itemView.findViewById(R.id.et_old_first);
            etOldLast = itemView.findViewById(R.id.et_old_last);
            etOldEmail = itemView.findViewById(R.id.et_old_email);
            etOldAddress = itemView.findViewById(R.id.et_old_address);
            etOldPhone = itemView.findViewById(R.id.et_old_phone);

            // New
            ivNewProfile = itemView.findViewById(R.id.profile_image_new);
            etNewFirst = itemView.findViewById(R.id.et_new_first);
            etNewLast = itemView.findViewById(R.id.et_new_last);
            etNewEmail = itemView.findViewById(R.id.et_new_email);
            etNewAddress = itemView.findViewById(R.id.et_new_address);
            etNewPhone = itemView.findViewById(R.id.et_new_phone);

            // Buttons
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);

        }
    }
}
