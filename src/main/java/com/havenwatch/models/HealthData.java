package com.havenwatch.models;

import java.time.LocalDateTime;

public class HealthData {
    private int healthId;
    private int residentId;
    private int heartRate;
    private String bloodPressure;
    private double temperature;
    private int bloodOxygen;
    private double weight;
    private int glucoseLevel;
    private LocalDateTime timestamp;

    // Default constructor
    public HealthData() {
    }

    // Constructor with essential fields
    public HealthData(int residentId, int heartRate, String bloodPressure, double temperature,
                      int bloodOxygen, double weight, int glucoseLevel) {
        this.residentId = residentId;
        this.heartRate = heartRate;
        this.bloodPressure = bloodPressure;
        this.temperature = temperature;
        this.bloodOxygen = bloodOxygen;
        this.weight = weight;
        this.glucoseLevel = glucoseLevel;
    }

    // Full constructor
    public HealthData(int healthId, int residentId, int heartRate, String bloodPressure,
                      double temperature, int bloodOxygen, double weight, int glucoseLevel,
                      LocalDateTime timestamp) {
        this.healthId = healthId;
        this.residentId = residentId;
        this.heartRate = heartRate;
        this.bloodPressure = bloodPressure;
        this.temperature = temperature;
        this.bloodOxygen = bloodOxygen;
        this.weight = weight;
        this.glucoseLevel = glucoseLevel;
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

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getBloodOxygen() {
        return bloodOxygen;
    }

    public void setBloodOxygen(int bloodOxygen) {
        this.bloodOxygen = bloodOxygen;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getGlucoseLevel() {
        return glucoseLevel;
    }

    public void setGlucoseLevel(int glucoseLevel) {
        this.glucoseLevel = glucoseLevel;
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
                ", timestamp=" + timestamp + "]";
    }
}