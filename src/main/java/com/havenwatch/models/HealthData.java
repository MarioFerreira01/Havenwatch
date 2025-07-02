package com.havenwatch.models;

import java.time.LocalDateTime;

public class HealthData {
    private int healthId;
    private int residentId;
    private int heartRate;
    private String bloodPressure;
    private int bloodOxygen;
    private LocalDateTime timestamp;

    // Default constructor
    public HealthData() {
    }

    // Constructor with essential fields
    public HealthData(int residentId, int heartRate, String bloodPressure, int bloodOxygen) {
        this.residentId = residentId;
        this.heartRate = heartRate;
        this.bloodPressure = bloodPressure;
        this.bloodOxygen = bloodOxygen;
    }

    // Full constructor
    public HealthData(int healthId, int residentId, int heartRate, String bloodPressure,
                      int bloodOxygen, LocalDateTime timestamp) {
        this.healthId = healthId;
        this.residentId = residentId;
        this.heartRate = heartRate;
        this.bloodPressure = bloodPressure;
        this.bloodOxygen = bloodOxygen;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getHealthId() {
        return healthId;
    }

    public void setHealthId(int healthId) {
        this.healthId = healthId;
    }

    public int getResidentId() {
        return residentId;
    }

    public void setResidentId(int residentId) {
        this.residentId = residentId;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public int getBloodOxygen() {
        return bloodOxygen;
    }

    public void setBloodOxygen(int bloodOxygen) {
        this.bloodOxygen = bloodOxygen;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "HealthData [healthId=" + healthId + ", residentId=" + residentId +
                ", heartRate=" + heartRate + ", bloodPressure=" + bloodPressure +
                ", bloodOxygen=" + bloodOxygen + ", timestamp=" + timestamp + "]";
    }
}