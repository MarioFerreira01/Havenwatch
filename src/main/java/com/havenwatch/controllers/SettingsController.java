package com.havenwatch.controllers;

import com.havenwatch.database.UserDAO;
import com.havenwatch.models.User;
import com.havenwatch.services.AuthService;
import com.havenwatch.services.DataSimulationService;
import com.havenwatch.utils.AlertUtils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

/**
 * Controller for the settings view
 */
public class SettingsController {
    @FXML
    private VBox rootPane;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Slider refreshIntervalSlider;

    @FXML
    private Label refreshIntervalLabel;

    @FXML
    private Slider simulationIntervalSlider;

    @FXML
    private Label simulationIntervalLabel;

    @FXML
    private CheckBox emailNotificationsCheckBox;

    @FXML
    private CheckBox smsNotificationsCheckBox;

    @FXML
    private CheckBox autoRefreshCheckBox;

    @FXML
    private Button saveSettingsButton;

    @FXML
    private Button resetSettingsButton;

    @FXML
    private Button generateDataButton;

    private final AuthService authService = new AuthService();
    private final UserDAO userDAO = new UserDAO();
    private final DataSimulationService dataSimulationService = new DataSimulationService();

    private User currentUser;

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Get current user
        currentUser = authService.getCurrentUser();

        if (currentUser != null) {
            // Set user info
            userNameLabel.setText(currentUser.getUsername());
            roleLabel.setText(currentUser.getRole().toString());
        }

        // Set up refresh interval slider
        refreshIntervalSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int value = newValue.intValue();
            refreshIntervalLabel.setText(value + " seconds");
        });

        // Set up simulation interval slider
        simulationIntervalSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int value = newValue.intValue();
            simulationIntervalLabel.setText(value + " seconds");
        });

        // Set default values
        refreshIntervalSlider.setValue(30);
        simulationIntervalSlider.setValue(60);
        emailNotificationsCheckBox.setSelected(true);
        smsNotificationsCheckBox.setSelected(false);
        autoRefreshCheckBox.setSelected(true);

        // Show/hide data generation button based on role
        generateDataButton.setVisible(authService.isAdmin());
        generateDataButton.setManaged(authService.isAdmin());
    }

    /**
     * Handle save settings button click
     */
    @FXML
    private void handleSaveSettings() {
        // In a real application, these settings would be saved to the database
        // For this demo, just show a success message
        AlertUtils.showInformation(
                rootPane.getScene().getWindow(),
                "Settings Saved",
                "Your settings have been successfully saved."
        );
    }

    /**
     * Handle reset settings button click
     */
    @FXML
    private void handleResetSettings() {
        // Reset to default values
        refreshIntervalSlider.setValue(30);
        simulationIntervalSlider.setValue(60);
        emailNotificationsCheckBox.setSelected(true);
        smsNotificationsCheckBox.setSelected(false);
        autoRefreshCheckBox.setSelected(true);

        AlertUtils.showInformation(
                rootPane.getScene().getWindow(),
                "Settings Reset",
                "Settings have been reset to default values."
        );
    }

    /**
     * Handle generate data button click
     */
    @FXML
    private void handleGenerateData() {
        // Generate a single data point for demo purposes
        dataSimulationService.generateDataPointNow();

        AlertUtils.showInformation(
                rootPane.getScene().getWindow(),
                "Data Generated",
                "A new set of data has been generated for all residents."
        );
    }
}