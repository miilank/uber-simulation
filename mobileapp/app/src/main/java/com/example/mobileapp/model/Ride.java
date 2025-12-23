package com.example.mobileapp.model;

public class Ride {
    private final String date;
    private final String time;
    private final String from;
    private final String to;
    private final RideStatus status;
    private final boolean panic;
    private final String price;
    private final String cancelledBy;

    public Ride(String date, String time, String from, String to,
                RideStatus status, boolean panic, String price, String cancelledBy) {
        this.date = date;
        this.time = time;
        this.from = from;
        this.to = to;
        this.status = status;
        this.panic = panic;
        this.price = price;
        this.cancelledBy = cancelledBy;
    }

    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public RideStatus getStatus() { return status; }
    public boolean isPanic() { return panic; }
    public String getPrice() { return price; }
    public String getCancelledBy() { return cancelledBy; }

    public boolean isCancelled() {
        return status == RideStatus.CANCELLED;
    }
}
