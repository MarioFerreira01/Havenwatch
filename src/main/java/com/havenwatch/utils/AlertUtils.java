package com.havenwatch.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Utility class for showing application alerts and dialogs
 */
public class AlertUtils {
    /**
     * Show an information alert
     * @param owner The owner window
     * @param title The alert title
     * @param message The alert message
     */
    public static void showInformation(Window owner, String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }

    /**
     * Show an error alert
     * @param owner The owner window
     * @param title The alert title
     * @param message The alert message
     */
    public static void showError(Window owner, String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }

    /**
     * Show a warning alert
     * @param owner The owner window
     * @param title The alert title
     * @param message The alert message
     */
    public static void showWarning(Window owner, String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }

    /**
     * Show a confirmation dialog
     * @param owner The owner window
     * @param title The alert title
     * @param message The alert message
     * @return true if OK clicked, false otherwise
     */
    public static boolean showConfirmation(Window owner, String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Show a custom alert dialog with specific buttons
     * @param owner The owner window
     * @param alertType The type of alert
     * @param title The alert title
     * @param message The alert message
     * @param buttonTypes The button types to include
     * @return The button type that was clicked
     */
    public static ButtonType showCustomAlert(Window owner, AlertType alertType, String title,
                                             String message, ButtonType... buttonTypes) {
        Alert alert = new Alert(alertType, message, buttonTypes);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.initOwner(owner);

        Optional<ButtonType> result = alert.showAndWait();
        return result.orElse(ButtonType.CANCEL);
    }

    /**
     * Show an alert for a health emergency
     * @param owner The owner window
     * @param residentName The name of the resident
     * @param condition The health condition
     */
    public static void showHealthEmergency(Window owner, String residentName, String condition) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("HEALTH EMERGENCY");
        alert.setHeaderText("Health Emergency for " + residentName);
        alert.setContentText("Critical health condition detected: " + condition + "\n\n" +
                "Please take immediate action!");
        alert.initOwner(owner);
        alert.showAndWait();
    }

    /**
     * Show an alert for an environmental emergency
     * @param owner The owner window
     * @param residentName The name of the resident
     * @param condition The environmental condition
     */
    public static void showEnvironmentEmergency(Window owner, String residentName, String condition) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("ENVIRONMENT EMERGENCY");
        alert.setHeaderText("Environmental Emergency for " + residentName);
        alert.setContentText("Critical environmental condition detected: " + condition + "\n\n" +
                "Please take immediate action!");
        alert.initOwner(owner);
        alert.showAndWait();
    }
}