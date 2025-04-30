package com.havenwatch.controllers;

import com.havenwatch.models.User;
import com.havenwatch.services.AuthService;
import com.havenwatch.utils.AlertUtils;
import com.havenwatch.utils.NavigationManager;
import com.havenwatch.utils.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Controller for the main application layout with sidebar navigation
 */
public class MainController {
    @FXML
    private BorderPane mainPane;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private VBox adminMenu;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button alertsButton;

    @FXML
    private Button residentsButton;

    @FXML
    private Button usersButton;

    @FXML
    private Button historyButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button logoutButton;

    private final AuthService authService = new AuthService();

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Set up user info
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getFullName());
            userRoleLabel.setText(currentUser.getRole().toString());

            // Show/hide admin menu based on user role
            adminMenu.setVisible(authService.isAdmin());
            adminMenu.setManaged(authService.isAdmin());
        }

        // Set initial selection
        selectButton(dashboardButton);
    }

    /**
     * Handle dashboard button click
     */
    @FXML
    private void handleDashboard() {
        selectButton(dashboardButton);
        NavigationManager.getInstance().loadView("dashboard");
    }

    /**
     * Handle alerts button click
     */
    @FXML
    private void handleAlerts() {
        selectButton(alertsButton);
        NavigationManager.getInstance().loadView("alerts");
    }

    /**
     * Handle residents button click
     */
    @FXML
    private void handleResidents() {
        selectButton(residentsButton);
        NavigationManager.getInstance().loadView("residents");
    }

    /**
     * Handle users button click
     */
    @FXML
    private void handleUsers() {
        selectButton(usersButton);
        NavigationManager.getInstance().loadView("users");
    }

    /**
     * Handle history button click
     */
    @FXML
    private void handleHistory() {
        selectButton(historyButton);
        NavigationManager.getInstance().loadView("history");
    }

    /**
     * Handle profile button click
     */
    @FXML
    private void handleProfile() {
        selectButton(profileButton);
        NavigationManager.getInstance().loadView("profile");
    }

    /**
     * Handle settings button click
     */
    @FXML
    private void handleSettings() {
        selectButton(settingsButton);
        NavigationManager.getInstance().loadView("settings");
    }

    /**
     * Handle logout button click
     */
    @FXML
    private void handleLogout() {
        boolean confirmed = AlertUtils.showConfirmation(
                mainPane.getScene().getWindow(),
                "Confirm Logout",
                "Are you sure you want to log out?"
        );

        if (confirmed) {
            authService.logout();
            NavigationManager.getInstance().navigateToLogin();
        }
    }

    /**
     * Update button styles to show selection
     * @param selectedButton The button that was selected
     */
    private void selectButton(Button selectedButton) {
        // Remove selection from all buttons
        dashboardButton.getStyleClass().remove("active-nav-button");
        alertsButton.getStyleClass().remove("active-nav-button");
        residentsButton.getStyleClass().remove("active-nav-button");
        usersButton.getStyleClass().remove("active-nav-button");
        historyButton.getStyleClass().remove("active-nav-button");
        profileButton.getStyleClass().remove("active-nav-button");
        settingsButton.getStyleClass().remove("active-nav-button");

        // Add selection to the clicked button
        selectedButton.getStyleClass().add("active-nav-button");
    }
}