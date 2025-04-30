package com.havenwatch.controllers;

import com.havenwatch.database.UserDAO;
import com.havenwatch.models.User;
import com.havenwatch.services.AuthService;
import com.havenwatch.utils.AlertUtils;
import com.havenwatch.utils.ValidationUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller for the user profile view
 */
public class ProfileController {
    @FXML
    private VBox rootPane;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button saveProfileButton;

    @FXML
    private Button changePasswordButton;

    private final AuthService authService = new AuthService();
    private final UserDAO userDAO = new UserDAO();

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

            // Populate profile fields
            fullNameField.setText(currentUser.getFullName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhone());
        }
    }

    /**
     * Handle save profile button click
     */
    @FXML
    private void handleSaveProfile() {
        // Validate profile fields
        if (!validateProfileFields()) {
            return;
        }

        // Update user profile
        currentUser.setFullName(fullNameField.getText().trim());
        currentUser.setEmail(emailField.getText().trim());
        currentUser.setPhone(phoneField.getText().trim());

        if (userDAO.updateUser(currentUser)) {
            AlertUtils.showInformation(
                    rootPane.getScene().getWindow(),
                    "Profile Updated",
                    "Your profile has been successfully updated."
            );
        } else {
            AlertUtils.showError(
                    rootPane.getScene().getWindow(),
                    "Update Failed",
                    "Failed to update profile. Please try again."
            );
        }
    }

    /**
     * Handle change password button click
     */
    @FXML
    private void handleChangePassword() {
        // Validate password fields
        if (!validatePasswordFields()) {
            return;
        }

        // Verify current password
        User user = userDAO.authenticate(currentUser.getUsername(), currentPasswordField.getText());
        if (user == null) {
            AlertUtils.showError(
                    rootPane.getScene().getWindow(),
                    "Invalid Password",
                    "The current password you entered is incorrect."
            );
            return;
        }

        // Update password
        currentUser.setPassword(newPasswordField.getText());

        if (userDAO.updateUser(currentUser)) {
            AlertUtils.showInformation(
                    rootPane.getScene().getWindow(),
                    "Password Changed",
                    "Your password has been successfully changed."
            );

            // Clear password fields
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            AlertUtils.showError(
                    rootPane.getScene().getWindow(),
                    "Update Failed",
                    "Failed to change password. Please try again."
            );
        }
    }

    /**
     * Validate profile fields
     * @return true if valid, false otherwise
     */
    private boolean validateProfileFields() {
        boolean valid = true;

        // Clear previous validation styles
        ValidationUtils.removeErrorStyle(fullNameField);
        ValidationUtils.removeErrorStyle(emailField);
        ValidationUtils.removeErrorStyle(phoneField);

        // Validate required fields
        if (!ValidationUtils.validateRequired(fullNameField, "Full Name")) {
            valid = false;
        }

        if (!ValidationUtils.validateRequired(emailField, "Email")) {
            valid = false;
        } else if (!ValidationUtils.validateEmail(emailField)) {
            valid = false;
        }

        if (!ValidationUtils.validateRequired(phoneField, "Phone")) {
            valid = false;
        } else if (!ValidationUtils.validatePhone(phoneField)) {
            valid = false;
        }

        return valid;
    }

    /**
     * Validate password fields
     * @return true if valid, false otherwise
     */
    private boolean validatePasswordFields() {
        boolean valid = true;

        // Clear previous validation styles
        ValidationUtils.removeErrorStyle(currentPasswordField);
        ValidationUtils.removeErrorStyle(newPasswordField);
        ValidationUtils.removeErrorStyle(confirmPasswordField);

        // Validate required fields
        if (!ValidationUtils.validateRequired(currentPasswordField, "Current Password")) {
            valid = false;
        }

        if (!ValidationUtils.validateRequired(newPasswordField, "New Password")) {
            valid = false;
        } else if (!ValidationUtils.validatePassword(newPasswordField)) {
            valid = false;
        }

        if (!ValidationUtils.validateRequired(confirmPasswordField, "Confirm Password")) {
            valid = false;
        } else if (!ValidationUtils.validatePasswordsMatch(newPasswordField, confirmPasswordField)) {
            valid = false;
        }

        return valid;
    }
}