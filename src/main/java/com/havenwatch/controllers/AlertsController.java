package com.havenwatch.controllers;

import com.havenwatch.models.Alert;
import com.havenwatch.models.Resident;
import com.havenwatch.services.AlertService;
import com.havenwatch.services.AccessControlService;
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

/**
 * Controller for the alerts view with proper access control
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
    private final AccessControlService accessControlService = new AccessControlService();

    private ObservableList<Alert> alertsList = FXCollections.observableArrayList();
    private FilteredList<Alert> filteredAlerts;

    private Timeline refreshTimeline;

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        try {
            // Set up table columns
            idColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAlertId()));

            residentColumn.setCellValueFactory(cellData -> {
                int residentId = cellData.getValue().getResidentId();
                List<Resident> accessibleResidents = accessControlService.getAccessibleResidents();
                Resident resident = accessibleResidents.stream()
                        .filter(r -> r.getResidentId() == residentId)
                        .findFirst()
                        .orElse(null);
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

        } catch (Exception e) {
            System.err.println("Error initializing alerts controller: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Failed to initialize alerts view: " + e.getMessage());
        }
    }

    /**
     * Set up the filter controls with access control
     */
    private void setupFilters() {
        try {
            // Set up resident filter with only accessible residents
            List<Resident> residents = accessControlService.getAccessibleResidents();
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

        } catch (Exception e) {
            System.err.println("Error setting up filters: " + e.getMessage());
        }
    }

    /**
     * Load alerts with proper access control
     */
    private void loadAlerts() {
        try {
            // Get only accessible alerts
            List<Alert> alerts = accessControlService.getAccessibleAlerts();
            alertsList.setAll(alerts);

            // Create filtered list
            filteredAlerts = new FilteredList<>(alertsList);
            alertsTable.setItems(filteredAlerts);

            // Apply current filters
            applyFilters();

            // Update summary labels
            updateSummaryLabels();

        } catch (Exception e) {
            System.err.println("Error loading alerts: " + e.getMessage());
            showErrorMessage("Failed to load alerts: " + e.getMessage());
        }
    }

    /**
     * Apply filters to the alerts list
     */
    private void applyFilters() {
        try {
            String residentFilter = filterResidentComboBox.getValue();
            String typeFilter = filterTypeComboBox.getValue();
            String severityFilter = filterSeverityComboBox.getValue();
            String statusFilter = filterStatusComboBox.getValue();
            String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";

            filteredAlerts.setPredicate(alert -> {
                // Check if user has access to this resident
                if (!accessControlService.canAccessResident(alert.getResidentId())) {
                    return false;
                }

                // Resident filter
                if (!residentFilter.equals("All Residents")) {
                    List<Resident> accessibleResidents = accessControlService.getAccessibleResidents();
                    Resident resident = accessibleResidents.stream()
                            .filter(r -> r.getResidentId() == alert.getResidentId())
                            .findFirst()
                            .orElse(null);
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
                    List<Resident> accessibleResidents = accessControlService.getAccessibleResidents();
                    String residentName = accessibleResidents.stream()
                            .filter(r -> r.getResidentId() == alert.getResidentId())
                            .map(r -> r.getFullName().toLowerCase())
                            .findFirst()
                            .orElse("");

                    return message.contains(searchText) || residentName.contains(searchText);
                }

                return true;
            });

            // Update summary labels
            updateSummaryLabels();

        } catch (Exception e) {
            System.err.println("Error applying filters: " + e.getMessage());
        }
    }

    /**
     * Update the summary labels with current counts
     */
    private void updateSummaryLabels() {
        try {
            totalAlertsLabel.setText(String.valueOf(filteredAlerts.size()));

            // Count critical alerts
            long criticalCount = filteredAlerts.stream()
                    .filter(alert -> alert.getSeverity() == Alert.AlertSeverity.CRITICAL)
                    .count();

            criticalAlertsLabel.setText(String.valueOf(criticalCount));

        } catch (Exception e) {
            System.err.println("Error updating summary labels: " + e.getMessage());
        }
    }

    /**
     * Handle acknowledge button click
     */
    private void handleAcknowledgeAlert(Alert alert) {
        try {
            // Check access permission
            if (!accessControlService.canAccessResident(alert.getResidentId())) {
                showErrorMessage("You do not have permission to modify this alert.");
                return;
            }

            if (alertService.acknowledgeAlert(alert.getAlertId())) {
                // Refresh alerts
                loadAlerts();
            } else {
                showErrorMessage("Failed to acknowledge alert.");
            }
        } catch (Exception e) {
            System.err.println("Error acknowledging alert: " + e.getMessage());
            showErrorMessage("Failed to acknowledge alert: " + e.getMessage());
        }
    }

    /**
     * Handle resolve button click
     */
    private void handleResolveAlert(Alert alert) {
        try {
            // Check access permission
            if (!accessControlService.canAccessResident(alert.getResidentId())) {
                showErrorMessage("You do not have permission to modify this alert.");
                return;
            }

            if (alertService.resolveAlert(alert.getAlertId())) {
                // Refresh alerts
                loadAlerts();
            } else {
                showErrorMessage("Failed to resolve alert.");
            }
        } catch (Exception e) {
            System.err.println("Error resolving alert: " + e.getMessage());
            showErrorMessage("Failed to resolve alert: " + e.getMessage());
        }
    }

    /**
     * Handle refresh button click
     */
    @FXML
    private void handleRefresh() {
        try {
            loadAlerts();
        } catch (Exception e) {
            System.err.println("Error refreshing alerts: " + e.getMessage());
            showErrorMessage("Failed to refresh alerts: " + e.getMessage());
        }
    }

    /**
     * Handle reset filters button click
     */
    @FXML
    private void handleResetFilters() {
        try {
            filterResidentComboBox.getSelectionModel().selectFirst();
            filterTypeComboBox.getSelectionModel().selectFirst();
            filterSeverityComboBox.getSelectionModel().selectFirst();
            filterStatusComboBox.getSelectionModel().selectFirst();
            searchField.clear();
        } catch (Exception e) {
            System.err.println("Error resetting filters: " + e.getMessage());
        }
    }

    /**
     * Show error message to user
     */
    private void showErrorMessage(String message) {
        if (rootPane != null && rootPane.getScene() != null) {
            AlertUtils.showError(rootPane.getScene().getWindow(), "Alerts Error", message);
        }
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