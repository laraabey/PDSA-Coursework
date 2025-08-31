package com.example.parking_lot_entry_tracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Car {
    private String numberPlate;
    private String tokenNumber;
    private long arrivalTime;
    private int position;
    private boolean isPriority;

    public Car(String numberPlate, String tokenNumber, long arrivalTime, int position) {
        this.numberPlate = numberPlate;
        this.tokenNumber = tokenNumber;
        this.arrivalTime = arrivalTime;
        this.position = position;
        this.isPriority = false;
    }

    // All getters and setters
    public String getNumberPlate() { return numberPlate; }
    public void setNumberPlate(String numberPlate) { this.numberPlate = numberPlate; }

    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }

    public long getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(long arrivalTime) { this.arrivalTime = arrivalTime; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public boolean isPriority() { return isPriority; }
    public void setPriority(boolean priority) { isPriority = priority; }

    public String getFormattedArrivalTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(arrivalTime));
    }

    public long getWaitingTimeMinutes() {
        return (System.currentTimeMillis() - arrivalTime) / (1000 * 60);
    }
}