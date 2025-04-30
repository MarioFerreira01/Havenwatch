package com.havenwatch.controllers;

import com.havenwatch.models.User;
import com.havenwatch.services.AuthService;
import com.havenwatch.utils.AlertUtils;
import com.havenwatch.utils.NavigationManager;
import com.havenwatch.utils.ValidationUtils;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

/**
 * Controller for the registration screen
 */
public class RegisterController {
    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;

    private final AuthService authService = new AuthService();

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Set up role combo box with non-admin roles only
        roleComboBox.setItems(FXCollections.observableArrayList(
                "CAREGIVER", "FAMILY", "HEALTHCARE"
        ));
        roleComboBox.getSelectionModel().selectFirst();
    }

    /**
     * Handle register button click
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        // Clear any existing styles
        ValidationUtils.removeErrorStyle(usernameField);
        ValidationUtils.removeErrorStyle(passwordField);
        ValidationUtils.removeErrorStyle(confirmPasswordField);
        ValidationUtils.removeErrorStyle(roleComboBox);
        ValidationUtils.removeErrorStyle(fullNameField);
        ValidationUtils.removeErrorStyle(emailField);
        ValidationUtils.removeErrorStyle(phoneField);

        // Validate input fields
        boolean isValid = true;

        if (!ValidationUtils.validateRequired(usernameField, "Username")) {
            isValid = false;
        } else if (!ValidationUtils.validateUsername(usernameField)) {
            isValid = false;
        }

        if (!ValidationUtils.validateRequired(passwordField, "Password")) {
            isValid = false;
        } else if (!ValidationUtils.validatePassword(passwordField)) {
            isValid = false;
        }

        if (!ValidationUtils.validateRequired(confirmPasswordField, "Confirm Password")) {
            isValid = false;
        } else if (!ValidationUtils.validatePasswordsMatch(passwordField, confirmPasswordField)) {
            isValid = false;
        }

        if (!ValidationUtils.validateComboBox(roleComboBox, "Role")) {
            isValid = false;
        }

        if (!ValidationUtils.validateRequired(fullNameField, "Full Name")) {
            isValid = false;
        }

        if (!ValidationUtils.validateRequired(emailField, "Email")) {
            isValid = false;
        } else if (!ValidationUtils.validateEmail(emailField)) {
            isValid = false;
        }

        if (!ValidationUtils.validateRequired(phoneField, "Phone")) {
            isValid = false;
        } else if (!ValidationUtils.validatePhone(phoneField)) {
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Create user object
        User user = new User();
        user.setUsername(usernameField.getText().trim());
        user.setPassword(passwordField.getText());
        user.setRole(User.UserRole.valueOf(roleComboBox.getValue()));
        user.setFullName(fullNameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setPhone(phoneField.getText().trim());

        // Attempt to register
        if (authService.registerUser(user)) {
            AlertUtils.showInformation(
                    rootPane.getScene().getWindow(),
                    "Registration Successful",
                    "Your account has been created. You can now log in."
            );
            NavigationManager.getInstance().navigateToLogin();
        } else {
            AlertUtils.showError(
                    rootPane.getScene().getWindow(),
                    "Registration Failed",
                    "Username already exists or there was an error with registration. Please try again."
            );
        }
    }

    /**
     * Handle back button click
     */
    @FXML
    private void handleBack(ActionEvent event) {
        NavigationManager.getInstance().navigateToLogin();
    }


}