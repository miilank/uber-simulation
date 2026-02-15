package com.example.mobileapp.features.shared.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mobileapp.features.shared.models.enums.RideStatus;

public class Ride implements Parcelable {
    @Nullable private final Integer id;
    private final String date;
    private final String time;
    private final String from;
    private final String to;
    private final RideStatus status;
    private final boolean panic;
    private final String price;
    @Nullable private final String cancelledBy;
    @Nullable private final String actualEndTime;
    @Nullable private Boolean alreadyRated;
    @Nullable private Boolean canRate;
    @Nullable private String ratingDisabledReason;

    public Ride(@Nullable Integer id, String date, String time, String from, String to,
                RideStatus status, boolean panic, String price, @Nullable String cancelledBy) {
        this(id, date, time, from, to, status, panic, price, cancelledBy, null, null);
    }

    public Ride(@Nullable Integer id, String date, String time, String from, String to,
                RideStatus status, boolean panic, String price, @Nullable String cancelledBy,
                @Nullable String actualEndTime, @Nullable Boolean alreadyRated) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.from = from;
        this.to = to;
        this.status = status;
        this.panic = panic;
        this.price = price;
        this.cancelledBy = cancelledBy;
        this.actualEndTime = actualEndTime;
        this.alreadyRated = alreadyRated;
        computeCanRate();
    }

    protected Ride(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        date = in.readString();
        time = in.readString();
        from = in.readString();
        to = in.readString();
        String st = in.readString();
        status = st != null ? RideStatus.valueOf(st) : RideStatus.SCHEDULED;
        panic = in.readByte() != 0;
        price = in.readString();
        cancelledBy = in.readString();
        actualEndTime = in.readString();

        byte tmpAlreadyRated = in.readByte();
        alreadyRated = tmpAlreadyRated == 2 ? null : tmpAlreadyRated != 0;

        byte tmpCanRate = in.readByte();
        canRate = tmpCanRate == 2 ? null : tmpCanRate != 0;

        ratingDisabledReason = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(from);
        dest.writeString(to);
        dest.writeString(status.name());
        dest.writeByte((byte) (panic ? 1 : 0));
        dest.writeString(price);
        dest.writeString(cancelledBy);
        dest.writeString(actualEndTime);

        if (alreadyRated == null) {
            dest.writeByte((byte) 2);
        } else {
            dest.writeByte((byte) (alreadyRated ? 1 : 0));
        }

        if (canRate == null) {
            dest.writeByte((byte) 2);
        } else {
            dest.writeByte((byte) (canRate ? 1 : 0));
        }

        dest.writeString(ratingDisabledReason);
    }

    private void computeCanRate() {
        if (Boolean.TRUE.equals(alreadyRated)) {
            canRate = false;
            ratingDisabledReason = "You have already rated this ride";
            return;
        }

        if (status == RideStatus.CANCELLED) {
            canRate = false;
            ratingDisabledReason = "Cancelled rides cannot be rated";
            return;
        }

        if (status != RideStatus.COMPLETED && status != RideStatus.STOPPED) {
            canRate = false;
            ratingDisabledReason = "Only completed rides can be rated";
            return;
        }

        if (actualEndTime != null && !actualEndTime.trim().isEmpty()) {
            double daysDiff = getDaysSinceEnd();
            if (daysDiff > 3) {
                canRate = false;
                ratingDisabledReason = "Rating period expired (max 3 days)";
                return;
            }
        }

        canRate = true;
        ratingDisabledReason = null;
    }

    private double getDaysSinceEnd() {
        if (actualEndTime == null) return 0;

        try {
            java.text.SimpleDateFormat[] formats = {
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", java.util.Locale.US),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", java.util.Locale.US),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US),
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            };

            java.util.Date endDate = null;
            for (java.text.SimpleDateFormat format : formats) {
                try {
                    endDate = format.parse(actualEndTime);
                    if (endDate != null) break;
                } catch (Exception e) {
                }
            }

            if (endDate == null) return 0;

            long diffMillis = System.currentTimeMillis() - endDate.getTime();
            return diffMillis / (1000.0 * 60 * 60 * 24);
        } catch (Exception e) {
            return 0;
        }
    }

    public void markAsRated(String reason) {
        this.alreadyRated = true;
        this.canRate = false;
        this.ratingDisabledReason = reason != null ? reason : "You have already rated this ride";
    }

    public static final Creator<Ride> CREATOR = new Creator<>() {
        @Override
        public Ride createFromParcel(Parcel in) {
            return new Ride(in);
        }

        @Override
        public Ride[] newArray(int size) {
            return new Ride[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    // Getteri
    @Nullable public Integer getId() { return id; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public RideStatus getStatus() { return status; }
    public boolean isPanic() { return panic; }
    public String getPrice() { return price; }
    @Nullable public String getCancelledBy() { return cancelledBy; }
    @Nullable public String getActualEndTime() { return actualEndTime; }
    @Nullable public Boolean getAlreadyRated() { return alreadyRated; }
    @Nullable public Boolean getCanRate() { return canRate; }
    @Nullable public String getRatingDisabledReason() { return ratingDisabledReason; }
}