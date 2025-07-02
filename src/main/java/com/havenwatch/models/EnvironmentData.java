package com.havenwatch.models;

import java.time.LocalDateTime;

public class EnvironmentData {
    private int environmentId;
    private int residentId;
    private double roomTemperature;
    private int humidity;
    private int airQuality;
    private int gasLevel;
    private LocalDateTime timestamp;

    // Default constructor
    public EnvironmentData() {
    }

    // Constructor with essential fields
    public EnvironmentData(int residentId, double roomTemperature, int humidity, int airQuality, int gasLevel) {
        this.residentId = residentId;
        this.roomTemperature = roomTemperature;
        this.humidity = humidity;
        this.airQuality = airQuality;
        this.gasLevel = gasLevel;
    }

    // Full constructor
    public EnvironmentData(int environmentId, int residentId, double roomTemperature, int humidity,
                           int airQuality, int gasLevel, LocalDateTime timestamp) {
        this.environmentId = environmentId;
        this.residentId = residentId;
        this.roomTemperature = roomTemperature;
        this.humidity = humidity;
        this.airQuality = airQuality;
        this.gasLevel = gasLevel;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(int environmentId) {
        this.environmentId = environmentId;
    }

    public int getResidentId() {
        return residentId;
    }

    public void setResidentId(int residentId) {
        this.residentId = residentId;
    }

    public double getRoomTemperature() {
        return roomTemperature;
    }

    public void setRoomTemperature(double roomTemperature) {
        this.roomTemperature = roomTemperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getAirQuality() {
        return airQuality;
    }

    public void setAirQuality(int airQuality) {
        this.airQuality = airQuality;
    }

    public int getGasLevel() {
        return gasLevel;
    }

    public void setGasLevel(int gasLevel) {
        this.gasLevel = gasLevel;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "EnvironmentData [environmentId=" + environmentId + ", residentId=" + residentId +
                ", roomTemperature=" + roomTemperature + ", humidity=" + humidity +
                ", airQuality=" + airQuality + ", gasLevel=" + gasLevel +
                ", timestamp=" + timestamp + "]";
    }
}