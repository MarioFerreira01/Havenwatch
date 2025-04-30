package com.havenwatch.controllers;

import com.havenwatch.database.ResidentDAO;
import com.havenwatch.models.Alert;
import com.havenwatch.models.Resident;
import com.havenwatch.services.AlertService;
import com.havenwatch.utils.AlertUtils;
import com.havenwatch.utils.DateTimeUtils;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Predicate;

/**
 * Controller for the alerts view
 */
public class AlertsController {
    @FXML
    private VBox rootPane;

    @FXML
    private TableView<Alert> alertsTable;

    @FXML
    private TableColumn<Alert, Integer> idColumn;

    @FXML
    private TableColumn<Alert, String> residentColumn;

    @FXML
    private TableColumn<Alert, Alert.AlertType> typeColumn;

    @FXML
    private TableColumn<Alert, Alert.AlertSeverity> severityColumn;

    @FXML
    private TableColumn<Alert, String> messageColumn;

    @FXML
    private TableColumn<Alert, Alert.AlertStatus> statusColumn;

    @FXML
    private TableColumn<Alert, String> createdAtColumn;

    @FXML
    private TableColumn<Alert, String> resolvedAtColumn;

    @FXML
    private TableColumn<Alert, Void> actionsColumn;

    @FXML
    private ComboBox<String> filterResidentComboBox;

    @FXML
    private ComboBox<String> filterTypeComboBox;

    @FXML
    private ComboBox<String> filterSeverityComboBox;

    @FXML
    private ComboBox<String> filterStatusComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private Label totalAlertsLabel;

    @FXML
    private Label criticalAlertsLabel;

    private final AlertService alertService = new AlertService();
    private final ResidentDAO residentDAO = new ResidentDAO();

    private ObservableList<Alert> alertsList = FXCollections.observableArrayList();
    private FilteredList<Alert> filteredAlerts;

    private Timeline refreshTimeline;

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Set up table columns
        idColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAlertId()));

        residentColumn.setCellValueFactory(cellData -> {
            int residentId = cellData.getValue().getResidentId();
            Resident resident = residentDAO.getResidentById(residentId);
            String name = resident != null ? resident.getFullName() : "Unknown";
            return new SimpleStringProperty(name);
        });

        typeColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAlertType()));
        typeColumn.setCellFactory(column -> new TableCell<Alert, Alert.AlertType>() {
            @Override
            protected void updateItem(Alert.AlertType item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    switch (item) {
                        case HEALTH:
                            setTextFill(Color.DARKBLUE);
                            break;
                        case ENVIRONMENT:
                            setTextFill(Color.DARKGREEN);
                            break;
                        case SYSTEM:
                            setTextFill(Color.DARKGRAY);
                            break;
                    }
                }
            }
        });

        severityColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getSeverity()));
        severityColumn.setCellFactory(column -> new TableCell<Alert, Alert.AlertSeverity>() {
            @Override
            protected void updateItem(Alert.AlertSeverity item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    switch (item) {
                        case LOW:
                            setTextFill(Color.GREEN);
                            break;
                        case MEDIUM:
                            setTextFill(Color.ORANGE);
                            break;
                        case HIGH:
                            setTextFill(Color.RED);
                            break;
                        case CRITICAL:
                            setStyle("-fx-font-weight: bold");
                            setTextFill(Color.RED);
                            break;
                    }
                }
            }
        });

        messageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMessage()));

        statusColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStatus()));
        statusColumn.setCellFactory(column -> new TableCell<Alert, Alert.AlertStatus>() {
            @Override
            protected void updateItem(Alert.AlertStatus item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    switch (item) {
                        case ACTIVE:
                            setTextFill(Color.RED);
                            break;
                        case ACKNOWLEDGED:
                            setTextFill(Color.BLUE);
                            break;
                        case RESOLVED:
                            setTextFill(Color.GREEN);
                            break;
                    }
                }
            }
        });

        createdAtColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(DateTimeUtils.formatDateTime(cellData.getValue().getCreatedAt())));

        resolvedAtColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(DateTimeUtils.formatDateTime(cellData.getValue().getResolvedAt())));

        // Set up actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<Alert, Void>() {
            private final Button acknowledgeButton = new Button("Acknowledge");
            private final Button resolveButton = new Button("Resolve");
            private final HBox pane = new HBox(5, acknowledgeButton, resolveButton);

            {
                acknowledgeButton.getStyleClass().add("small-button");
                resolveButton.getStyleClass().add("small-button");

                acknowledgeButton.setOnAction(event -> {
                    Alert alert = getTableView().getItems().get(getIndex());
                    handleAcknowledgeAlert(alert);
                });

                resolveButton.setOnAction(event -> {
                    Alert alert = getTableView().getItems().get(getIndex());
                    handleResolveAlert(alert);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Alert alert = getTableView().getItems().get(getIndex());

                    // Show/hide buttons based on alert status
                    switch (alert.getStatus()) {
                        case ACTIVE:
                            acknowledgeButton.setVisible(true);
                            resolveButton.setVisible(true);
                            break;
                        case ACKNOWLEDGED:
                            acknowledgeButton.setVisible(false);
                            resolveButton.setVisible(true);
                            break;
                        case RESOLVED:
                            acknowledgeButton.setVisible(false);
                            resolveButton.setVisible(false);
                            break;
                    }

                    setGraphic(pane);
                }
            }
        });

        // Set up filtering
        setupFilters();

        // Load initial data
        loadAlerts();

        // Set up auto-refresh (every 30 seconds)
        refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), event -> loadAlerts())
        );
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
    }

    /**
     * Set up the filter controls
     */
    private void setupFilters() {
        // Set up resident filter
        List<Resident> residents = residentDAO.getAllResidents();
        ObservableList<String> residentOptions = FXCollections.observableArrayList("All Residents");
        for (Resident resident : residents) {
            residentOptions.add(resident.getFullName());
        }
        filterResidentComboBox.setItems(residentOptions);
        filterResidentComboBox.getSelectionModel().selectFirst();

        // Set up type filter
        ObservableList<String> typeOptions = FXCollections.observableArrayList(
                "All Types", "HEALTH", "ENVIRONMENT", "SYSTEM"
        );
        filterTypeComboBox.setItems(typeOptions);
        filterTypeComboBox.getSelectionModel().selectFirst();

        // Set up severity filter
        ObservableList<String> severityOptions = FXCollections.observableArrayList(
                "All Severities", "LOW", "MEDIUM", "HIGH", "CRITICAL"
        );
        filterSeverityComboBox.setItems(severityOptions);
        filterSeverityComboBox.getSelectionModel().selectFirst();

        // Set up status filter
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "All Statuses", "ACTIVE", "ACKNOWLEDGED", "RESOLVED"
        );
        filterStatusComboBox.setItems(statusOptions);
        filterStatusComboBox.getSelectionModel().selectFirst();

        // Set up filter change listeners
        filterResidentComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters());

        filterTypeComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters());

        filterSeverityComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters());

        filterStatusComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters());

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters());
    }

    /**
     * Load alerts from the database
     */
    private void loadAlerts() {
        List<Alert> alerts = alertService.getAllActiveAlerts();
        alertsList.setAll(alerts);

        // Create filtered list
        filteredAlerts = new FilteredList<>(alertsList);
        alertsTable.setItems(filteredAlerts);

        // Apply current filters
        applyFilters();

        // Update summary labels
        updateSummaryLabels();
    }

    /**
     * Apply filters to the alerts list
     */
    private void applyFilters() {
        String residentFilter = filterResidentComboBox.getValue();
        String typeFilter = filterTypeComboBox.getValue();
        String severityFilter = filterSeverityComboBox.getValue();
        String statusFilter = filterStatusComboBox.getValue();
        String searchText = searchField.getText().toLowerCase();

        filteredAlerts.setPredicate(alert -> {
            // Resident filter
            if (!residentFilter.equals("All Residents")) {
                Resident resident = residentDAO.getResidentById(alert.getResidentId());
                if (resident == null || !resident.getFullName().equals(residentFilter)) {
                    return false;
                }
            }

            // Type filter
            if (!typeFilter.equals("All Types") && !alert.getAlertType().toString().equals(typeFilter)) {
                return false;
            }

            // Severity filter
            if (!severityFilter.equals("All Severities") && !alert.getSeverity().toString().equals(severityFilter)) {
                return false;
            }

            // Status filter
            if (!statusFilter.equals("All Statuses") && !alert.getStatus().toString().equals(statusFilter)) {
                return false;
            }

            // Search text
            if (!searchText.isEmpty()) {
                String message = alert.getMessage().toLowerCase();
                Resident resident = residentDAO.getResidentById(alert.getResidentId());
                String residentName = resident != null ? resident.getFullName().toLowerCase() : "";

                return message.contains(searchText) || residentName.contains(searchText);
            }

            return true;
        });

        // Update summary labels
        updateSummaryLabels();
    }

    /**
     * Update the summary labels with current counts
     */
    private void updateSummaryLabels() {
        totalAlertsLabel.setText(String.valueOf(filteredAlerts.size()));

        // Count critical alerts
        long criticalCount = filteredAlerts.stream()
                .filter(alert -> alert.getSeverity() == Alert.AlertSeverity.CRITICAL)
                .count();

        criticalAlertsLabel.setText(String.valueOf(criticalCount));
    }

    /**
     * Handle acknowledge button click
     */
    private void handleAcknowledgeAlert(Alert alert) {
        if (alertService.acknowledgeAlert(alert.getAlertId())) {
            // Refresh alerts
            loadAlerts();
        } else {
            AlertUtils.showError(rootPane.getScene().getWindow(),
                    "Error",
                    "Failed to acknowledge alert.");
        }
    }

    /**
     * Handle resolve button click
     */
    private void handleResolveAlert(Alert alert) {
        if (alertService.resolveAlert(alert.getAlertId())) {
            // Refresh alerts
            loadAlerts();
        } else {
            AlertUtils.showError(rootPane.getScene().getWindow(),
                    "Error",
                    "Failed to resolve alert.");
        }
    }

    /**
     * Handle refresh button click
     */
    @FXML
    private void handleRefresh() {
        loadAlerts();
    }

    /**
     * Handle reset filters button click
     */
    @FXML
    private void handleResetFilters() {
        filterResidentComboBox.getSelectionModel().selectFirst();
        filterTypeComboBox.getSelectionModel().selectFirst();
        filterSeverityComboBox.getSelectionModel().selectFirst();
        filterStatusComboBox.getSelectionModel().selectFirst();
        searchField.clear();
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