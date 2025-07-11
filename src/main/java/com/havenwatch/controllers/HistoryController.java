package com.havenwatch.controllers;

import com.havenwatch.models.EnvironmentData;
import com.havenwatch.models.HealthData;
import com.havenwatch.models.Resident;
import com.havenwatch.services.DashboardService;
import com.havenwatch.services.AccessControlService;
import com.havenwatch.utils.AlertUtils;
import com.havenwatch.utils.DateTimeUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for the historical data view with proper access control
 */
public class HistoryController {
    @FXML
    private VBox rootPane;

    @FXML
    private ComboBox<Resident> residentComboBox;

    @FXML
    private ComboBox<String> dataTypeComboBox;

    @FXML
    private ComboBox<Integer> dayRangeComboBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Label noDataLabel;

    @FXML
    private TableView<HistoricalDataEntry> dataTable;

    @FXML
    private TableColumn<HistoricalDataEntry, String> timestampColumn;

    @FXML
    private TableColumn<HistoricalDataEntry, Number> value1Column;

    @FXML
    private TableColumn<HistoricalDataEntry, String> value2Column;

    @FXML
    private TableColumn<HistoricalDataEntry, Number> value3Column;

    @FXML
    private TableColumn<HistoricalDataEntry, Number> value4Column;

    @FXML
    private LineChart<Number, Number> dataChart;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    private final DashboardService dashboardService = new DashboardService();
    private final AccessControlService accessControlService = new AccessControlService();

    /**
     * Data model for historical data entries (updated for new schema)
     */
    public static class HistoricalDataEntry {
        private LocalDateTime timestamp;
        private Number value1;
        private String value2;
        private Number value3;
        private Number value4;

        public HistoricalDataEntry(LocalDateTime timestamp, Number value1, String value2, Number value3, Number value4) {
            this.timestamp = timestamp;
            this.value1 = value1;
            this.value2 = value2;
            this.value3 = value3;
            this.value4 = value4;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getFormattedTimestamp() {
            return DateTimeUtils.formatDateTime(timestamp);
        }

        public Number getValue1() {
            return value1;
        }

        public void setValue1(Number value1) {
            this.value1 = value1;
        }

        public String getValue2() {
            return value2;
        }

        public void setValue2(String value2) {
            this.value2 = value2;
        }

        public Number getValue3() {
            return value3;
        }

        public void setValue3(Number value3) {
            this.value3 = value3;
        }

        public Number getValue4() {
            return value4;
        }

        public void setValue4(Number value4) {
            this.value4 = value4;
        }
    }

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        try {
            // Set up resident combo box with access control
            loadResidents();

            // Set up data type combo box
            dataTypeComboBox.setItems(FXCollections.observableArrayList(
                    "Health Data", "Environment Data"
            ));
            dataTypeComboBox.getSelectionModel().selectFirst();

            // Set up day range combo box
            dayRangeComboBox.setItems(FXCollections.observableArrayList(
                    1, 3, 7, 14, 30, 90
            ));
            dayRangeComboBox.getSelectionModel().select(2); // Default to 7 days

            // Set up date pickers
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(7);
            startDatePicker.setValue(startDate);
            endDatePicker.setValue(endDate);

            // Set up table columns
            timestampColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedTimestamp()));

            value1Column.setCellValueFactory(new PropertyValueFactory<>("value1"));
            value2Column.setCellValueFactory(new PropertyValueFactory<>("value2"));
            value3Column.setCellValueFactory(new PropertyValueFactory<>("value3"));
            value4Column.setCellValueFactory(new PropertyValueFactory<>("value4"));

            // Add change listeners
            residentComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    loadData();
                }
            });

            dataTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    updateColumnHeaders();
                    loadData();
                }
            });

            dayRangeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    // Update date pickers based on selected range
                    LocalDate end = LocalDate.now();
                    LocalDate start = end.minusDays(newValue);
                    startDatePicker.setValue(start);
                    endDatePicker.setValue(end);

                    loadData();
                }
            });

            startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && endDatePicker.getValue() != null) {
                    loadData();
                }
            });

            endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && startDatePicker.getValue() != null) {
                    loadData();
                }
            });

            // Set initial column headers
            updateColumnHeaders();

            // Show no data message initially
            showNoDataMessage(true);

        } catch (Exception e) {
            System.err.println("Error initializing history controller: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Failed to initialize historical data view: " + e.getMessage());
        }
    }

    /**
     * Load residents that the current user can access
     */
    private void loadResidents() {
        try {
            List<Resident> residents = accessControlService.getAccessibleResidents();
            residentComboBox.setItems(FXCollections.observableArrayList(residents));

            // Set converter to display resident names
            residentComboBox.setConverter(new StringConverter<Resident>() {
                @Override
                public String toString(Resident resident) {
                    return resident != null ? resident.getFullName() : "";
                }

                @Override
                public Resident fromString(String string) {
                    return null; // Not needed for combo box
                }
            });

            // Select first resident if available
            if (!residents.isEmpty()) {
                residentComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            System.err.println("Error loading residents: " + e.getMessage());
            showErrorMessage("Failed to load residents: " + e.getMessage());
        }
    }

    /**
     * Update column headers based on selected data type (updated for new schema)
     */
    private void updateColumnHeaders() {
        String dataType = dataTypeComboBox.getValue();

        if ("Health Data".equals(dataType)) {
            timestampColumn.setText("Time");
            value1Column.setText("Heart Rate");
            value2Column.setText("Blood Pressure");
            value3Column.setText("Blood Oxygen");
            value4Column.setText(""); // No fourth value for health data
        } else if ("Environment Data".equals(dataType)) {
            timestampColumn.setText("Time");
            value1Column.setText("Room Temp");
            value2Column.setText("Humidity");
            value3Column.setText("Air Quality");
            value4Column.setText("Gas Level");
        }
    }

    /**
     * Load data based on selected options with access control
     */
    private void loadData() {
        try {
            Resident selectedResident = residentComboBox.getValue();
            String dataType = dataTypeComboBox.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (selectedResident == null || dataType == null || startDate == null || endDate == null) {
                return;
            }

            // Check access permission
            if (!accessControlService.canAccessResident(selectedResident.getResidentId())) {
                showErrorMessage("You do not have permission to access this resident's data.");
                return;
            }

            LocalDate endDateInclusive = endDate.plusDays(1); // Include end date
            ObservableList<HistoricalDataEntry> dataEntries = FXCollections.observableArrayList();

            // Clear previous chart data
            dataChart.getData().clear();

            if ("Health Data".equals(dataType)) {
                // Load health data
                List<HealthData> healthDataList = dashboardService.getHealthDataHistory(
                        selectedResident.getResidentId(),
                        (int)java.time.temporal.ChronoUnit.DAYS.between(startDate, endDateInclusive)
                );

                if (healthDataList.isEmpty()) {
                    showNoDataMessage(true);
                    return;
                }

                // Create data entries (updated for new schema)
                for (HealthData data : healthDataList) {
                    dataEntries.add(new HistoricalDataEntry(
                            data.getTimestamp(),
                            data.getHeartRate(),
                            data.getBloodPressure(),
                            data.getBloodOxygen(),
                            null // No fourth value
                    ));
                }

                // Create chart series (updated for new schema)
                XYChart.Series<Number, Number> heartRateSeries = new XYChart.Series<>();
                heartRateSeries.setName("Heart Rate");

                XYChart.Series<Number, Number> oxygenSeries = new XYChart.Series<>();
                oxygenSeries.setName("Blood Oxygen");

                for (int i = 0; i < healthDataList.size(); i++) {
                    HealthData data = healthDataList.get(i);
                    heartRateSeries.getData().add(new XYChart.Data<>(i, data.getHeartRate()));
                    oxygenSeries.getData().add(new XYChart.Data<>(i, data.getBloodOxygen()));
                }

                dataChart.getData().addAll(heartRateSeries, oxygenSeries);

            } else if ("Environment Data".equals(dataType)) {
                // Load environment data
                List<EnvironmentData> environmentDataList = dashboardService.getEnvironmentDataHistory(
                        selectedResident.getResidentId(),
                        (int)java.time.temporal.ChronoUnit.DAYS.between(startDate, endDateInclusive)
                );

                if (environmentDataList.isEmpty()) {
                    showNoDataMessage(true);
                    return;
                }

                // Create data entries (updated for new schema)
                for (EnvironmentData data : environmentDataList) {
                    dataEntries.add(new HistoricalDataEntry(
                            data.getTimestamp(),
                            data.getRoomTemperature(),
                            data.getHumidity() + "%",
                            data.getAirQuality(),
                            data.getGasLevel()
                    ));
                }

                // Create chart series (updated for new schema)
                XYChart.Series<Number, Number> tempSeries = new XYChart.Series<>();
                tempSeries.setName("Room Temp");

                XYChart.Series<Number, Number> humiditySeries = new XYChart.Series<>();
                humiditySeries.setName("Humidity");

                XYChart.Series<Number, Number> airQualitySeries = new XYChart.Series<>();
                airQualitySeries.setName("Air Quality");

                for (int i = 0; i < environmentDataList.size(); i++) {
                    EnvironmentData data = environmentDataList.get(i);
                    tempSeries.getData().add(new XYChart.Data<>(i, data.getRoomTemperature()));
                    humiditySeries.getData().add(new XYChart.Data<>(i, data.getHumidity()));
                    airQualitySeries.getData().add(new XYChart.Data<>(i, data.getAirQuality()));
                }

                dataChart.getData().addAll(tempSeries, humiditySeries, airQualitySeries);
            }

            // Update table data
            dataTable.setItems(dataEntries);

            // Show/hide no data message
            showNoDataMessage(dataEntries.isEmpty());

        } catch (Exception e) {
            System.err.println("Error loading historical data: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Failed to load historical data: " + e.getMessage());
        }
    }

    /**
     * Show or hide the no data message
     */
    private void showNoDataMessage(boolean show) {
        noDataLabel.setVisible(show);
        noDataLabel.setManaged(show);
        dataTable.setVisible(!show);
        dataTable.setManaged(!show);
        dataChart.setVisible(!show);
        dataChart.setManaged(!show);
    }

    /**
     * Show error message to user
     */
    private void showErrorMessage(String message) {
        if (rootPane != null && rootPane.getScene() != null) {
            AlertUtils.showError(rootPane.getScene().getWindow(), "Historical Data Error", message);
        }
    }

    /**
     * Handle refresh button click
     */
    @FXML
    private void handleRefresh() {
        try {
            loadData();
        } catch (Exception e) {
            System.err.println("Error refreshing historical data: " + e.getMessage());
            showErrorMessage("Failed to refresh data: " + e.getMessage());
        }
    }
}