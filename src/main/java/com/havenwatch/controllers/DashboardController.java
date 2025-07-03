package com.havenwatch.controllers;

import com.havenwatch.models.Alert;
import com.havenwatch.models.HealthData;
import com.havenwatch.models.EnvironmentData;
import com.havenwatch.models.Resident;
import com.havenwatch.services.DashboardService;
import com.havenwatch.services.DataSimulationService;
import com.havenwatch.services.AccessControlService;
import com.havenwatch.utils.DateTimeUtils;
import com.havenwatch.utils.AlertUtils;

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
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for the dashboard view with proper access control
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
    private final AccessControlService accessControlService = new AccessControlService();

    private Timeline refreshTimeline;

    /**
     * Data model for the resident health status table (updated without temperature and weight)
     */
    public static class ResidentHealthStatus {
        private final String name;
        private Integer heartRate;
        private String bloodPressure;
        private Integer bloodOxygen;
        private Double roomTemperature;
        private String lastUpdated;
        private boolean hasAlert;

        public ResidentHealthStatus(String name) {
            this.name = name;
        }

        // Getters and setters
        public String getName() { return name; }
        public Integer getHeartRate() { return heartRate; }
        public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }
        public String getBloodPressure() { return bloodPressure; }
        public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }
        public Integer getBloodOxygen() { return bloodOxygen; }
        public void setBloodOxygen(Integer bloodOxygen) { this.bloodOxygen = bloodOxygen; }
        public Double getRoomTemperature() { return roomTemperature; }
        public void setRoomTemperature(Double roomTemperature) { this.roomTemperature = roomTemperature; }
        public String getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
        public boolean isHasAlert() { return hasAlert; }
        public void setHasAlert(boolean hasAlert) { this.hasAlert = hasAlert; }
    }

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        try {
            // Set up table columns
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            heartRateColumn.setCellValueFactory(new PropertyValueFactory<>("heartRate"));
            bpColumn.setCellValueFactory(new PropertyValueFactory<>("bloodPressure"));
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

        } catch (Exception e) {
            System.err.println("Error initializing dashboard: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Failed to initialize dashboard: " + e.getMessage());
        }
    }

    /**
     * Handle refresh button click
     */
    @FXML
    private void handleRefresh() {
        try {
            // Generate a new data point
            dataSimulationService.generateDataPointNow();

            // Refresh the dashboard
            refreshDashboard();
        } catch (Exception e) {
            System.err.println("Error refreshing dashboard: " + e.getMessage());
            showErrorMessage("Failed to refresh data: " + e.getMessage());
        }
    }

    /**
     * Refresh all dashboard data with proper access control
     */
    private void refreshDashboard() {
        try {
            // Get only accessible residents
            List<Resident> residents = accessControlService.getAccessibleResidents();
            totalResidentsLabel.setText(String.valueOf(residents.size()));

            // Get only accessible alerts
            List<Alert> accessibleAlerts = accessControlService.getAccessibleAlerts();
            activeAlertsLabel.setText(String.valueOf(accessibleAlerts.size()));

            // Update critical alerts list
            updateCriticalAlertsList();

            // Update resident status table
            updateResidentStatusTable(residents);

            // Update alert charts
            updateAlertsPieChart();
            updateAlertsBarChart();

        } catch (Exception e) {
            System.err.println("Error refreshing dashboard data: " + e.getMessage());
            showErrorMessage("Failed to refresh dashboard: " + e.getMessage());
        }
    }

    /**
     * Update the critical alerts list with access control
     */
    private void updateCriticalAlertsList() {
        try {
            List<Alert> criticalAlerts = accessControlService.getAccessibleAlerts().stream()
                    .filter(alert -> alert.getSeverity() == Alert.AlertSeverity.CRITICAL ||
                            alert.getSeverity() == Alert.AlertSeverity.HIGH)
                    .toList();

            ObservableList<String> alertItems = FXCollections.observableArrayList();

            for (Alert alert : criticalAlerts) {
                List<Resident> accessibleResidents = accessControlService.getAccessibleResidents();
                Resident resident = accessibleResidents.stream()
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
        } catch (Exception e) {
            System.err.println("Error updating critical alerts: " + e.getMessage());
        }
    }

    /**
     * Update the resident status table with access control
     */
    private void updateResidentStatusTable(List<Resident> residents) {
        try {
            ObservableList<ResidentHealthStatus> statusItems = FXCollections.observableArrayList();

            // Get latest health and environment data
            Map<Integer, HealthData> healthDataMap = dashboardService.getLatestHealthData();
            Map<Integer, EnvironmentData> environmentDataMap = dashboardService.getLatestEnvironmentData();

            for (Resident resident : residents) {
                ResidentHealthStatus status = new ResidentHealthStatus(resident.getFullName());

                // Add health data if available
                HealthData healthData = healthDataMap.get(resident.getResidentId());
                if (healthData != null) {
                    status.setHeartRate(healthData.getHeartRate());
                    status.setBloodPressure(healthData.getBloodPressure());
                    status.setBloodOxygen(healthData.getBloodOxygen());
                    status.setLastUpdated(DateTimeUtils.formatRelativeTime(healthData.getTimestamp()));
                }

                // Add environment data if available
                EnvironmentData environmentData = environmentDataMap.get(resident.getResidentId());
                if (environmentData != null) {
                    status.setRoomTemperature(environmentData.getRoomTemperature());
                }

                // Check if resident has any alerts
                List<Alert> alerts = accessControlService.getAccessibleAlertsForResident(resident.getResidentId());
                status.setHasAlert(!alerts.isEmpty());

                statusItems.add(status);
            }

            residentStatusTable.setItems(statusItems);
        } catch (Exception e) {
            System.err.println("Error updating resident status table: " + e.getMessage());
        }
    }

    /**
     * Update the alerts pie chart
     */
    private void updateAlertsPieChart() {
        try {
            int[] alertCounts = dashboardService.getAlertCountsBySeverity();

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Low", alertCounts[0]),
                    new PieChart.Data("Medium", alertCounts[1]),
                    new PieChart.Data("High", alertCounts[2]),
                    new PieChart.Data("Critical", alertCounts[3])
            );

            alertsPieChart.setData(pieChartData);

            // Add color to slices
            Platform.runLater(() -> {
                if (pieChartData.size() > 0 && pieChartData.get(0).getNode() != null) {
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
            });
        } catch (Exception e) {
            System.err.println("Error updating pie chart: " + e.getMessage());
        }
    }

    /**
     * Update the alerts bar chart
     */
    private void updateAlertsBarChart() {
        try {
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
            Platform.runLater(() -> {
                if (series.getData().size() > 0) {
                    series.getData().get(0).getNode().setStyle("-fx-bar-fill: #a5d6a7;"); // Low - Green
                    series.getData().get(1).getNode().setStyle("-fx-bar-fill: #fff59d;"); // Medium - Yellow
                    series.getData().get(2).getNode().setStyle("-fx-bar-fill: #ffab91;"); // High - Orange
                    series.getData().get(3).getNode().setStyle("-fx-bar-fill: #ef9a9a;"); // Critical - Red
                }
            });
        } catch (Exception e) {
            System.err.println("Error updating bar chart: " + e.getMessage());
        }
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
     * Show error message to user
     */
    private void showErrorMessage(String message) {
        Platform.runLater(() -> {
            if (rootPane != null && rootPane.getScene() != null) {
                AlertUtils.showError(rootPane.getScene().getWindow(), "Dashboard Error", message);
            }
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