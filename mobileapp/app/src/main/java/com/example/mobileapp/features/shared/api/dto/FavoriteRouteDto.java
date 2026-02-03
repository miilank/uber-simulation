package com.example.mobileapp.features.shared.api.dto;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.mobileapp.features.shared.models.enums.VehicleType;

import java.time.LocalDateTime;
import java.util.List;

public class FavoriteRouteDto implements Parcelable {
    private Integer id;
    private String name;
    private LocationDto startLocation;
    private LocationDto endLocation;
    private List<LocationDto> waypoints;
    private VehicleType vehicleType;
    private boolean babyFriendly;
    private boolean petsFriendly;
    private LocalDateTime createdAt;

    public FavoriteRouteDto() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocationDto getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LocationDto startLocation) {
        this.startLocation = startLocation;
    }

    public LocationDto getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LocationDto endLocation) {
        this.endLocation = endLocation;
    }

    public List<LocationDto> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<LocationDto> waypoints) {
        this.waypoints = waypoints;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public boolean isBabyFriendly() {
        return babyFriendly;
    }

    public void setBabyFriendly(boolean babyFriendly) {
        this.babyFriendly = babyFriendly;
    }

    public boolean isPetsFriendly() {
        return petsFriendly;
    }

    public void setPetsFriendly(boolean petsFriendly) {
        this.petsFriendly = petsFriendly;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeValue(id);

        dest.writeString(name);

        dest.writeParcelable(startLocation, flags);
        dest.writeParcelable(endLocation, flags);

        dest.writeTypedList(waypoints);

        dest.writeString(vehicleType == null ? null : vehicleType.name());

        dest.writeByte((byte) (babyFriendly ? 1 : 0));
        dest.writeByte((byte) (petsFriendly ? 1 : 0));

        dest.writeString(createdAt == null ? null : createdAt.toString());
    }

    protected FavoriteRouteDto(@NonNull Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());

        this.name = in.readString();

        this.startLocation = in.readParcelable(LocationDto.class.getClassLoader());
        this.endLocation = in.readParcelable(LocationDto.class.getClassLoader());

        this.waypoints = in.createTypedArrayList(LocationDto.CREATOR);

        String vehicleName = in.readString();
        this.vehicleType = vehicleName == null ? null : VehicleType.valueOf(vehicleName);

        this.babyFriendly = in.readByte() != 0;
        this.petsFriendly = in.readByte() != 0;

        String createdAtStr = in.readString();
        this.createdAt = createdAtStr == null ? null : LocalDateTime.parse(createdAtStr);
    }

    public static final Creator<FavoriteRouteDto> CREATOR = new Creator<FavoriteRouteDto>() {
        @Override
        public FavoriteRouteDto createFromParcel(Parcel in) {
            return new FavoriteRouteDto(in);
        }

        @Override
        public FavoriteRouteDto[] newArray(int size) {
            return new FavoriteRouteDto[size];
        }
    };
}