package com.example.mobileapp.features.shared.models;

import android.os.Parcel;
import android.os.Parcelable; // packs objects in Bundle so Android can send it between Activity/Fragment/DialogFragment

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

    public Ride(@Nullable Integer id, String date, String time, String from, String to,
                RideStatus status, boolean panic, String price, @Nullable String cancelledBy) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.from = from;
        this.to = to;
        this.status = status;
        this.panic = panic;
        this.price = price;
        this.cancelledBy = cancelledBy;
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
    }

    @Nullable public Integer getId() { return id; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public RideStatus getStatus() { return status; }
    public boolean isPanic() { return panic; }
    public String getPrice() { return price; }
    @Nullable public String getCancelledBy() { return cancelledBy; }

}
