package com.havenwatch.utils;

import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;

import java.util.regex.Pattern;

/**
 * Utility class for form input validation
 */
public class ValidationUtils {
    // Regular expressions for validation
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9. ()-]{7,15}$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_]{4,20}$");

    /**
     * Validate email format
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number format
     * @param phone The phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate username format
     * @param username The username to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate password strength
     * @param password The password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        // Password must be at least 6 characters
        return password != null && password.length() >= 6;
    }

    /**
     * Validate that a string is not empty
     * @param value The string to validate
     * @return true if not empty, false otherwise
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Apply error styling to a control
     * @param control The control to style
     */
    public static void setErrorStyle(Control control) {
        control.getStyleClass().add("error");
    }

    /**
     * Remove error styling from a control
     * @param control The control to style
     */
    public static void removeErrorStyle(Control control) {
        control.getStyleClass().remove("error");
    }

    /**
     * Validate a text field for non-empty content
     * @param field The field to validate
     * @param fieldName The name of the field (for error messages)
     * @return true if valid, false otherwise
     */
    public static boolean validateRequired(TextInputControl field, String fieldName) {
        boolean valid = isNotEmpty(field.getText());
        if (!valid) {
            setErrorStyle(field);
            field.setPromptText(fieldName + " is required");
        } else {
            removeErrorStyle(field);
        }
        return valid;
    }

    /**
     * Validate a combo box for selection
     * @param comboBox The combo box to validate
     * @param fieldName The name of the field (for error messages)
     * @return true if valid, false otherwise
     */
    public static boolean validateComboBox(ComboBox<?> comboBox, String fieldName) {
        boolean valid = comboBox.getValue() != null;
        if (!valid) {
            setErrorStyle(comboBox);
            comboBox.setPromptText(fieldName + " is required");
        } else {
            removeErrorStyle(comboBox);
        }
        return valid;
    }

    /**
     * Validate a date picker for selection
     * @param datePicker The date picker to validate
     * @param fieldName The name of the field (for error messages)
     * @return true if valid, false otherwise
     */
    public static boolean validateDatePicker(DatePicker datePicker, String fieldName) {
        boolean valid = datePicker.getValue() != null;
        if (!valid) {
            setErrorStyle(datePicker);
            datePicker.setPromptText(fieldName + " is required");
        } else {
            removeErrorStyle(datePicker);
        }
        return valid;
    }

    /**
     * Validate an email field
     * @param field The field to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateEmail(TextField field) {
        boolean valid = isValidEmail(field.getText());
        if (!valid) {
            setErrorStyle(field);
            field.setPromptText("Invalid email format");
        } else {
            removeErrorStyle(field);
        }
        return valid;
    }

    /**
     * Validate a phone field
     * @param field The field to validate
     * @return true if valid, false otherwise
     */
    public static boolean validatePhone(TextField field) {
        boolean valid = isValidPhone(field.getText());
        if (!valid) {
            setErrorStyle(field);
            field.setPromptText("Invalid phone format");
        } else {
            removeErrorStyle(field);
        }
        return valid;
    }

    /**
     * Validate a username field
     * @param field The field to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateUsername(TextField field) {
        boolean valid = isValidUsername(field.getText());
        if (!valid) {
            setErrorStyle(field);
            field.setPromptText("Username must be 4-20 alphanumeric chars or underscore");
        } else {
            removeErrorStyle(field);
        }
        return valid;
    }

    /**
     * Validate a password field
     * @param field The field to validate
     * @return true if valid, false otherwise
     */
    public static boolean validatePassword(PasswordField field) {
        boolean valid = isValidPassword(field.getText());
        if (!valid) {
            setErrorStyle(field);
            field.setPromptText("Password must be at least 6 characters");
        } else {
            removeErrorStyle(field);
        }
        return valid;
    }

    /**
     * Validate two password fields match
     * @param field1 The first password field
     * @param field2 The confirm password field
     * @return true if they match, false otherwise
     */
    public static boolean validatePasswordsMatch(PasswordField field1, PasswordField field2) {
        boolean valid = field1.getText().equals(field2.getText());
        if (!valid) {
            setErrorStyle(field2);
            field2.setPromptText("Passwords do not match");
        } else {
            removeErrorStyle(field2);
        }
        return valid;
    }
}