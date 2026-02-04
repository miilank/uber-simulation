package com.example.mobileapp.features.shared.api.dto;

public class DriverUpdateDto {
    private Integer driverId;

    private String email;

    private String newFirstName;
    private String newLastName;
    private String newPhoneNumber;
    private String newAddress;
    private String newProfilePicture;

    private String oldFirstName;
    private String oldLastName;
    private String oldPhoneNumber;
    private String oldAddress;
    private String oldProfilePicture;

    public String getOldProfilePicture() {
        return oldProfilePicture;
    }

    public void setOldProfilePicture(String oldProfilePicture) {
        this.oldProfilePicture = oldProfilePicture;
    }

    public String getOldAddress() {
        return oldAddress;
    }

    public void setOldAddress(String oldAddress) {
        this.oldAddress = oldAddress;
    }

    public String getOldPhoneNumber() {
        return oldPhoneNumber;
    }

    public void setOldPhoneNumber(String oldPhoneNumber) {
        this.oldPhoneNumber = oldPhoneNumber;
    }

    public String getOldLastName() {
        return oldLastName;
    }

    public void setOldLastName(String oldLastName) {
        this.oldLastName = oldLastName;
    }

    public String getOldFirstName() {
        return oldFirstName;
    }

    public void setOldFirstName(String oldFirstName) {
        this.oldFirstName = oldFirstName;
    }

    public String getNewProfilePicture() {
        return newProfilePicture;
    }

    public void setNewProfilePicture(String newProfilePicture) {
        this.newProfilePicture = newProfilePicture;
    }

    public String getNewAddress() {
        return newAddress;
    }

    public void setNewAddress(String newAddress) {
        this.newAddress = newAddress;
    }

    public String getNewPhoneNumber() {
        return newPhoneNumber;
    }

    public void setNewPhoneNumber(String newPhoneNumber) {
        this.newPhoneNumber = newPhoneNumber;
    }

    public String getNewLastName() {
        return newLastName;
    }

    public void setNewLastName(String newLastName) {
        this.newLastName = newLastName;
    }

    public String getNewFirstName() {
        return newFirstName;
    }

    public void setNewFirstName(String newFirstName) {
        this.newFirstName = newFirstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
    }
}
