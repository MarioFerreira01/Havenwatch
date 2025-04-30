package com.havenwatch.controllers;

import com.havenwatch.models.Alert;
import com.havenwatch.models.HealthData;
import com.havenwatch.models.EnvironmentData;
import com.havenwatch.models.Resident;
import com.havenwatch.services.DashboardService;
import com.havenwatch.services.DataSimulationService;
import com.havenwatch.utils.DateTimeUtils;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for the dashboard view
 */
public class DashboardController {
    @FXML
    private VBox rootPane;

    @FXML
    private Label timeLabel;

    @FXML
    private Label totalResidentsLabel;

    @FXML
    private Label activeAlertsLabel;

    @FXML
    private PieChart alertsPieChart;

    @FXML
    private ListView<String> criticalAlertsList;

    @FXML
    private TableView<ResidentHealthStatus> residentStatusTable;

    @FXML
    private TableColumn<ResidentHealthStatus, String> nameColumn;

    @FXML
    private TableColumn<ResidentHealthStatus, Integer> heartRateColumn;

    @FXML
    private TableColumn<ResidentHealthStatus, String> bpColumn;

    @FXML
    private TableColumn<ResidentHealthStatus, Double> tempColumn;

    @FXML
    private TableColumn<ResidentHealthStatus, Integer> oxygenColumn;

    @FXML
    private TableColumn<ResidentHealthStatus, Double> roomTempColumn;

    @FXML
    private TableColumn<ResidentHealthStatus, String> lastUpdatedColumn;

    @FXML
    private BarChart<String, Number> alertsBarChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    private final DashboardService dashboardService = new DashboardService();
    private final DataSimulationService dataSimulationService = new DataSimulationService();

    private Timeline refreshTimeline;

    /**
     * Data model for the resident health status table
     */
    public static class ResidentHealthStatus {
        private final String name;
        private Integer heartRate;
        private String bloodPressure;
        private Double temperature;
        private Integer bloodOxygen;
        private Double roomTemperature;
        private String lastUpdated;
        private boolean hasAlert;

        public ResidentHealthStatus(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Integer getHeartRate() {
            return heartRate;
        }

        public void setHeartRate(Integer heartRate) {
            this.heartRate = heartRate;
        }

        public String getBloodPressure() {
            return bloodPressure;
        }

        public void setBloodPressure(String bloodPressure) {
            this.bloodPressure = bloodPressure;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Integer getBloodOxygen() {
            return bloodOxygen;
        }

        public void setBloodOxygen(Integer bloodOxygen) {
            this.bloodOxygen = bloodOxygen;
        }

        public Double getRoomTemperature() {
            return roomTemperature;
        }

        public void setRoomTemperature(Double roomTemperature) {
            this.roomTemperature = roomTemperature;
        }

        public String getLastUpdated() {
            return lastUpdated;
        }

        public void setLastUpdated(String lastUpdated) {
            this.lastUpdated = lastUpdated;
        }

        public boolean isHasAlert() {
            return hasAlert;
        }

        public void setHasAlert(boolean hasAlert) {
            this.hasAlert = hasAlert;
        }
    }

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Set up table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        heartRateColumn.setCellValueFactory(new PropertyValueFactory<>("heartRate"));
        bpColumn.setCellValueFactory(new PropertyValueFactory<>("bloodPressure"));
        tempColumn.setCellValueFactory(new PropertyValueFactory<>("temperature"));
        oxygenColumn.setCellValueFactory(new PropertyValueFactory<>("bloodOxygen"));
        roomTempColumn.setCellValueFactory(new PropertyValueFactory<>("roomTemperature"));
        lastUpdatedColumn.setCellValueFactory(new PropertyValueFactory<>("lastUpdated"));

        // Apply row styling based on alert status
        residentStatusTable.setRowFactory(tv -> new javafx.scene.control.TableRow<ResidentHealthStatus>() {
            @Override
            protected void updateItem(ResidentHealthStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.isHasAlert()) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else {
                    setStyle("");
                }
            }
        });

        // Initial data load
        refreshDashboard();

        // Set up auto-refresh (every 30 seconds)
        refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), event -> refreshDashboard())
        );
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();

        // Update clock every second
        Timeline clockTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> updateClock())
        );
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
    }

    /**
     * Handle refresh button click
     */
    @FXML
    private void handleRefresh() {
        // Generate a new data point
        dataSimulationService.generateDataPointNow();

        // Refresh the dashboard
        refreshDashboard();
    }

    /**
     * Refresh all dashboard data
     */
    private void refreshDashboard() {
        // Update resident count
        List<Resident> residents = dashboardService.getAccessibleResidents();
        totalResidentsLabel.setText(String.valueOf(residents.size()));

        // Update alerts count
        int totalAlerts = dashboardService.getTotalActiveAlerts();
        activeAlertsLabel.setText(String.valueOf(totalAlerts));

        // Update critical alerts list
        updateCriticalAlertsList();

        // Update resident status table
        updateResidentStatusTable(residents);

        // Update alert pie chart
        updateAlertsPieChart();

        // Update alerts bar chart
        updateAlertsBarChart();
    }

    /**
     * Update the critical alerts list
     */
    private void updateCriticalAlertsList() {
        List<Alert> criticalAlerts = dashboardService.getCriticalAlerts();
        ObservableList<String> alertItems = FXCollections.observableArrayList();

        for (Alert alert : criticalAlerts) {
            Resident resident = dashboardService.getAccessibleResidents().stream()
                    .filter(r -> r.getResidentId() == alert.getResidentId())
                    .findFirst()
                    .orElse(null);

            if (resident != null) {
                String severity = alert.getSeverity().toString();
                String alertText = severity + ": " + resident.getFullName() + " - " + alert.getMessage();
                alertItems.add(alertText);
            }
        }

        criticalAlertsList.setItems(alertItems);
    }

    /**
     * Update the resident status table
     */
    private void updateResidentStatusTable(List<Resident> residents) {
        ObservableList<ResidentHealthStatus> statusItems = FXCollections.observableArrayList();

        // Get latest health and environment data
        Map<Integer, HealthData> healthDataMap = dashboardService.getLatestHealthData();
        Map<Integer, EnvironmentData> environmentDataMap = dashboardService.getLatestEnvironmentData();
        Map<Integer, List<Alert>> alertsMap = dashboardService.getActiveAlerts();

        for (Resident resident : residents) {
            ResidentHealthStatus status = new ResidentHealthStatus(resident.getFullName());

            // Add health data if available
            HealthData healthData = healthDataMap.get(resident.getResidentId());
            if (healthData != null) {
                status.setHeartRate(healthData.getHeartRate());
                status.setBloodPressure(healthData.getBloodPressure());
                status.setTemperature(healthData.getTemperature());
                status.setBloodOxygen(healthData.getBloodOxygen());
                status.setLastUpdated(DateTimeUtils.formatRelativeTime(healthData.getTimestamp()));
            }

            // Add environment data if available
            EnvironmentData environmentData = environmentDataMap.get(resident.getResidentId());
            if (environmentData != null) {
                status.setRoomTemperature(environmentData.getRoomTemperature());
            }

            // Check if resident has any alerts
            List<Alert> alerts = alertsMap.get(resident.getResidentId());
            status.setHasAlert(alerts != null && !alerts.isEmpty());

            statusItems.add(status);
        }

        residentStatusTable.setItems(statusItems);
    }

    /**
     * Update the alerts pie chart
     */
    private void updateAlertsPieChart() {
        int[] alertCounts = dashboardService.getAlertCountsBySeverity();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Low", alertCounts[0]),
                new PieChart.Data("Medium", alertCounts[1]),
                new PieChart.Data("High", alertCounts[2]),
                new PieChart.Data("Critical", alertCounts[3])
        );

        alertsPieChart.setData(pieChartData);

        // Add color to slices
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: #a5d6a7;"); // Low - Green
        pieChartData.get(1).getNode().setStyle("-fx-pie-color: #fff59d;"); // Medium - Yellow
        pieChartData.get(2).getNode().setStyle("-fx-pie-color: #ffab91;"); // High - Orange
        pieChartData.get(3).getNode().setStyle("-fx-pie-color: #ef9a9a;"); // Critical - Red

        // Hide label if count is 0
        for (PieChart.Data data : pieChartData) {
            if (data.getPieValue() == 0) {
                data.getNode().setVisible(false);
            }
        }
    }

    /**
     * Update the alerts bar chart
     */
    private void updateAlertsBarChart() {
        int[] alertCounts = dashboardService.getAlertCountsBySeverity();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Alerts by Severity");

        series.getData().add(new XYChart.Data<>("Low", alertCounts[0]));
        series.getData().add(new XYChart.Data<>("Medium", alertCounts[1]));
        series.getData().add(new XYChart.Data<>("High", alertCounts[2]));
        series.getData().add(new XYChart.Data<>("Critical", alertCounts[3]));

        alertsBarChart.getData().clear();
        alertsBarChart.getData().add(series);

        // Add color to bars
        series.getData().get(0).getNode().setStyle("-fx-bar-fill: #a5d6a7;"); // Low - Green
        series.getData().get(1).getNode().setStyle("-fx-bar-fill: #fff59d;"); // Medium - Yellow
        series.getData().get(2).getNode().setStyle("-fx-bar-fill: #ffab91;"); // High - Orange
        series.getData().get(3).getNode().setStyle("-fx-bar-fill: #ef9a9a;"); // Critical - Red
    }

    /**
     * Update the clock display
     */
    private void updateClock() {
        Platform.runLater(() -> {
            timeLabel.setText(DateTimeUtils.formatDateTime(LocalDateTime.now()));
        });
    }

    /**
     * Clean up when controller is no longer needed
     */
    public void cleanup() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
    }
}