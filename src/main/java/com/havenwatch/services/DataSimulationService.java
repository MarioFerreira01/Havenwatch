package com.havenwatch.services;

import com.havenwatch.database.HealthDataDAO;
import com.havenwatch.database.EnvironmentDataDAO;
import com.havenwatch.database.ResidentDAO;
import com.havenwatch.models.HealthData;
import com.havenwatch.models.EnvironmentData;
import com.havenwatch.models.Resident;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service to simulate sensor data for demonstration purposes
 */
public class DataSimulationService {
    private final ResidentDAO residentDAO;
    private final HealthDataDAO healthDataDAO;
    private final EnvironmentDataDAO environmentDataDAO;
    private final AlertService alertService;
    private final Random random;

    private ScheduledExecutorService scheduler;
    private boolean isRunning = false;

    // Normal ranges for health data (updated without temperature, weight, glucose)
    private static final int HEART_RATE_MIN = 60;
    private static final int HEART_RATE_MAX = 100;
    private static final int HEART_RATE_VARIATION = 5;

    private static final int SYSTOLIC_MIN = 110;
    private static final int SYSTOLIC_MAX = 140;
    private static final int DIASTOLIC_MIN = 70;
    private static final int DIASTOLIC_MAX = 90;
    private static final int BP_VARIATION = 5;

    private static final int OXYGEN_MIN = 94;
    private static final int OXYGEN_MAX = 99;
    private static final int OXYGEN_VARIATION = 1;

    // Normal ranges for environment data (removed motion_detected)
    private static final double ROOM_TEMP_MIN = 18.0;
    private static final double ROOM_TEMP_MAX = 25.0;
    private static final double ROOM_TEMP_VARIATION = 0.5;

    private static final int HUMIDITY_MIN = 40;
    private static final int HUMIDITY_MAX = 60;
    private static final int HUMIDITY_VARIATION = 3;

    private static final int AIR_QUALITY_MIN = 70;
    private static final int AIR_QUALITY_MAX = 95;
    private static final int AIR_QUALITY_VARIATION = 5;

    private static final int GAS_LEVEL_MIN = 0;
    private static final int GAS_LEVEL_MAX = 20;
    private static final int GAS_LEVEL_VARIATION = 2;

    // Chance of abnormal readings (to trigger alerts)
    private static final double ABNORMAL_READING_CHANCE = 0.05;  // 5% chance

    public DataSimulationService() {
        this.residentDAO = new ResidentDAO();
        this.healthDataDAO = new HealthDataDAO();
        this.environmentDataDAO = new EnvironmentDataDAO();
        this.alertService = new AlertService();
        this.random = new Random();
    }

    /**
     * Start the data simulation service
     * @param intervalSeconds How often to generate new data (in seconds)
     */
    public void startSimulation(int intervalSeconds) {
        if (isRunning) {
            return;
        }

        isRunning = true;
        scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                generateSimulatedData();
            } catch (Exception e) {
                System.err.println("Error generating simulated data: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);

        System.out.println("Data simulation started with interval of " + intervalSeconds + " seconds");
    }

    /**
     * Stop the data simulation service
     */
    public void stopSimulation() {
        if (!isRunning) {
            return;
        }

        isRunning = false;
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Data simulation stopped");
    }

    /**
     * Generate simulated data for all residents
     */
    private void generateSimulatedData() {
        List<Resident> residents = residentDAO.getAllResidents();
        for (Resident resident : residents) {
            generateHealthData(resident);
            generateEnvironmentData(resident);
        }
    }

    /**
     * Generate health data for a specific resident
     * @param resident The resident
     */
    private void generateHealthData(Resident resident) {
        HealthData latestData = healthDataDAO.getLatestHealthData(resident.getResidentId());
        HealthData newData = new HealthData();
        newData.setResidentId(resident.getResidentId());

        boolean shouldBeAbnormal = random.nextDouble() < ABNORMAL_READING_CHANCE;

        // Generate heart rate
        if (latestData == null) {
            newData.setHeartRate(randomBetween(HEART_RATE_MIN, HEART_RATE_MAX));
        } else {
            int baseHeartRate = latestData.getHeartRate();
            int variation = randomBetween(-HEART_RATE_VARIATION, HEART_RATE_VARIATION);
            int newHeartRate = baseHeartRate + variation;

            // Keep within normal range unless abnormal flag is set
            if (!shouldBeAbnormal) {
                newHeartRate = Math.max(HEART_RATE_MIN, Math.min(HEART_RATE_MAX, newHeartRate));
            } else if (random.nextBoolean()) {
                // High heart rate
                newHeartRate = HEART_RATE_MAX + randomBetween(5, 30);
            } else {
                // Low heart rate
                newHeartRate = HEART_RATE_MIN - randomBetween(5, 20);
            }

            newData.setHeartRate(newHeartRate);
        }

        // Generate blood pressure
        int systolic = randomBetween(SYSTOLIC_MIN, SYSTOLIC_MAX);
        int diastolic = randomBetween(DIASTOLIC_MIN, DIASTOLIC_MAX);
        if (shouldBeAbnormal && random.nextBoolean()) {
            // High blood pressure
            systolic += randomBetween(20, 50);
            diastolic += randomBetween(10, 30);
        }
        newData.setBloodPressure(systolic + "/" + diastolic);

        // Generate blood oxygen
        int oxygen = randomBetween(OXYGEN_MIN, OXYGEN_MAX);
        if (shouldBeAbnormal && random.nextBoolean()) {
            // Low oxygen
            oxygen = OXYGEN_MIN - randomBetween(3, 10);
        }
        newData.setBloodOxygen(oxygen);

        // Save to database
        if (healthDataDAO.insertHealthData(newData)) {
            // Check for alert conditions
            alertService.checkHealthDataForAlerts(newData);
        }
    }

    /**
     * Generate environment data for a specific resident
     * @param resident The resident
     */
    private void generateEnvironmentData(Resident resident) {
        EnvironmentData latestData = environmentDataDAO.getLatestEnvironmentData(resident.getResidentId());
        EnvironmentData newData = new EnvironmentData();
        newData.setResidentId(resident.getResidentId());

        boolean shouldBeAbnormal = random.nextDouble() < ABNORMAL_READING_CHANCE;

        // Generate room temperature
        if (latestData == null) {
            newData.setRoomTemperature(randomDouble(ROOM_TEMP_MIN, ROOM_TEMP_MAX));
        } else {
            double baseTemp = latestData.getRoomTemperature();
            double variation = randomDouble(-ROOM_TEMP_VARIATION, ROOM_TEMP_VARIATION);
            double newTemp = baseTemp + variation;

            // Keep within normal range unless abnormal flag is set
            if (!shouldBeAbnormal) {
                newTemp = Math.max(ROOM_TEMP_MIN, Math.min(ROOM_TEMP_MAX, newTemp));
            } else if (random.nextBoolean()) {
                // High temperature
                newTemp = ROOM_TEMP_MAX + randomDouble(5.0, 10.0);
            } else {
                // Low temperature
                newTemp = ROOM_TEMP_MIN - randomDouble(3.0, 8.0);
            }

            newData.setRoomTemperature(Math.round(newTemp * 10) / 10.0);  // Round to 1 decimal place
        }

        // Generate humidity
        int humidity = randomBetween(HUMIDITY_MIN, HUMIDITY_MAX);
        if (shouldBeAbnormal && random.nextBoolean()) {
            if (random.nextBoolean()) {
                // High humidity
                humidity = HUMIDITY_MAX + randomBetween(10, 30);
            } else {
                // Low humidity
                humidity = HUMIDITY_MIN - randomBetween(10, 20);
            }
        }
        newData.setHumidity(humidity);

        // Generate air quality
        int airQuality = randomBetween(AIR_QUALITY_MIN, AIR_QUALITY_MAX);
        if (shouldBeAbnormal && random.nextBoolean()) {
            // Poor air quality
            airQuality = AIR_QUALITY_MIN - randomBetween(20, 50);
        }
        newData.setAirQuality(airQuality);

        // Generate gas level
        int gasLevel = randomBetween(GAS_LEVEL_MIN, GAS_LEVEL_MAX);
        if (shouldBeAbnormal && random.nextBoolean()) {
            // High gas level
            gasLevel = GAS_LEVEL_MAX + randomBetween(30, 70);
        }
        newData.setGasLevel(gasLevel);

        // Save to database
        if (environmentDataDAO.insertEnvironmentData(newData)) {
            // Check for alert conditions
            alertService.checkEnvironmentDataForAlerts(newData);
        }
    }

    /**
     * Generate a random integer between min and max (inclusive)
     */
    private int randomBetween(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    /**
     * Generate a random double between min and max
     */
    private double randomDouble(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    /**
     * Generate a single data point for all residents on demand
     */
    public void generateDataPointNow() {
        generateSimulatedData();
    }
}