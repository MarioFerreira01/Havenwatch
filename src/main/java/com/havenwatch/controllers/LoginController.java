package com.havenwatch.controllers;

import com.havenwatch.services.AuthService;
import com.havenwatch.utils.AlertUtils;
import com.havenwatch.utils.NavigationManager;
import com.havenwatch.utils.ValidationUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

/**
 * Controller for the login screen
 */
public class LoginController {
    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    private final AuthService authService = new AuthService();

    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        // Clear any existing styles
        ValidationUtils.removeErrorStyle(usernameField);
        ValidationUtils.removeErrorStyle(passwordField);

        // Validate input fields
        boolean isValid = true;

        if (!ValidationUtils.validateRequired(usernameField, "Username")) {
            isValid = false;
        }

        if (!ValidationUtils.validateRequired(passwordField, "Password")) {
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Attempt to login
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (authService.login(username, password)) {
            // Login successful, navigate to main screen
            NavigationManager.getInstance().navigateToMainScreen();
        } else {
            // Login failed
            AlertUtils.showError(rootPane.getScene().getWindow(),
                    "Login Failed",
                    "Invalid username or password. Please try again.");

            // Clear password field
            passwordField.clear();
            passwordField.requestFocus();
        }
    }

    /**
     * Handle register button click
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        // Navigate to registration screen
        NavigationManager.getInstance().navigateToRegister();
    }

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Set up enter key handler for login
        passwordField.setOnAction(event -> handleLogin(event));

        // For development - preset admin credentials
        // usernameField.setText("admin");
        // passwordField.setText("admin123");
    }
}