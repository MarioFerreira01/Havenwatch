package com.havenwatch.controllers;

import com.havenwatch.database.UserDAO;
import com.havenwatch.models.User;
import com.havenwatch.services.AccessControlService;
import com.havenwatch.utils.AlertUtils;
import com.havenwatch.utils.ValidationUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controller for the user management view with proper access control
 */
public class UsersController {
    @FXML
    private VBox rootPane;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> phoneColumn;

    @FXML
    private TableColumn<User, Void> actionsColumn;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private VBox userFormPane;

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
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private final UserDAO userDAO = new UserDAO();
    private final AccessControlService accessControlService = new AccessControlService();

    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private User currentUser;
    private boolean isEditMode = false;

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        try {
            // Check if user has admin privileges
            if (!accessControlService.canManageUsers()) {
                showErrorMessage("You do not have permission to access user management.");
                return;
            }

            // Set up table columns
            usernameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getUsername()));

            nameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getFullName()));

            roleColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getRole().toString()));

            emailColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getEmail()));

            phoneColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getPhone()));

            // Set up actions column with buttons
            actionsColumn.setCellFactory(param -> new TableCell<User, Void>() {
                private final Button editButton = new Button("Edit");
                private final Button deleteButton = new Button("Delete");
                private final HBox pane = new HBox(5, editButton, deleteButton);

                {
                    editButton.getStyleClass().add("small-button");
                    deleteButton.getStyleClass().add("small-button");

                    editButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        editUser(user);
                    });

                    deleteButton.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        deleteUser(user);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty) {
                        setGraphic(null);
                    } else {
                        User user = getTableView().getItems().get(getIndex());

                        // Don't allow deleting the current user
                        User currentLoggedInUser = com.havenwatch.utils.SessionManager.getInstance().getCurrentUser();
                        deleteButton.setDisable(user.getUserId() == currentLoggedInUser.getUserId());

                        setGraphic(pane);
                    }
                }
            });

            // Set up role combo box
            roleComboBox.setItems(FXCollections.observableArrayList(
                    "ADMIN", "CAREGIVER", "HEALTHCARE", "FAMILY"));

            // Hide form initially
            userFormPane.setVisible(false);
            userFormPane.setManaged(false);

            // Load initial data
            loadUsers();

        } catch (Exception e) {
            System.err.println("Error initializing users controller: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Failed to initialize user management: " + e.getMessage());
        }
    }

    /**
     * Load users from the database
     */
    private void loadUsers() {
        try {
            if (!accessControlService.canManageUsers()) {
                showErrorMessage("You do not have permission to view users.");
                return;
            }

            List<User> users = userDAO.getAllUsers();
            usersList.setAll(users);
            usersTable.setItems(usersList);

            // Update count label
            totalUsersLabel.setText(String.valueOf(users.size()));

        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            showErrorMessage("Failed to load users: " + e.getMessage());
        }
    }

    /**
     * Handle add user button click
     */
    @FXML
    private void handleAddUser() {
        try {
            if (!accessControlService.canManageUsers()) {
                showErrorMessage("You do not have permission to add users.");
                return;
            }

            // Clear form
            clearForm();

            // Show form for adding
            isEditMode = false;
            currentUser = null;
            saveButton.setText("Add User");

            // Show password fields for new users
            passwordField.setVisible(true);
            confirmPasswordField.setVisible(true);

            userFormPane.setVisible(true);
            userFormPane.setManaged(true);

        } catch (Exception e) {
            System.err.println("Error handling add user: " + e.getMessage());
            showErrorMessage("Failed to open add user form: " + e.getMessage());
        }
    }

    /**
     * Edit an existing user
     */
    private void editUser(User user) {
        try {
            if (!accessControlService.canManageUsers()) {
                showErrorMessage("You do not have permission to edit users.");
                return;
            }

            // Set up form for editing
            isEditMode = true;
            currentUser = user;
            populateForm(user);

            // Hide password fields for editing
            passwordField.setVisible(false);
            confirmPasswordField.setVisible(false);

            // Show form
            userFormPane.setVisible(true);
            userFormPane.setManaged(true);
            saveButton.setText("Update User");

            // Don't allow changing username for existing users
            usernameField.setEditable(false);

        } catch (Exception e) {
            System.err.println("Error editing user: " + e.getMessage());
            showErrorMessage("Failed to edit user: " + e.getMessage());
        }
    }

    /**
     * Delete a user
     */
    private void deleteUser(User user) {
        try {
            if (!accessControlService.canManageUsers()) {
                showErrorMessage("You do not have permission to delete users.");
                return;
            }

            // Don't allow deleting current user
            User currentLoggedInUser = com.havenwatch.utils.SessionManager.getInstance().getCurrentUser();
            if (user.getUserId() == currentLoggedInUser.getUserId()) {
                showErrorMessage("You cannot delete your own user account.");
                return;
            }

            boolean confirmed = AlertUtils.showConfirmation(
                    rootPane.getScene().getWindow(),
                    "Confirm Delete",
                    "Are you sure you want to delete user " + user.getUsername() + "? This action cannot be undone."
            );

            if (confirmed) {
                if (userDAO.deleteUser(user.getUserId())) {
                    // Refresh the list
                    loadUsers();
                } else {
                    showErrorMessage("Failed to delete user. Please try again.");
                }
            }

        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            showErrorMessage("Failed to delete user: " + e.getMessage());
        }
    }

    /**
     * Populate the form with user data
     */
    private void populateForm(User user) {
        usernameField.setText(user.getUsername());
        roleComboBox.setValue(user.getRole().toString());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());

        // Clear passwords
        passwordField.clear();
        confirmPasswordField.clear();
    }

    /**
     * Clear the form
     */
    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
        fullNameField.clear();
        emailField.clear();
        phoneField.clear();

        // Make username editable for new users
        usernameField.setEditable(true);
    }

    /**
     * Handle save button click
     */
    @FXML
    private void handleSave() {
        try {
            if (!accessControlService.canManageUsers()) {
                showErrorMessage("You do not have permission to save users.");
                return;
            }

            // Validate form
            if (!validateForm()) {
                return;
            }

            // Create or update user
            User user = isEditMode ? currentUser : new User();

            user.setUsername(usernameField.getText().trim());
            user.setRole(User.UserRole.valueOf(roleComboBox.getValue()));
            user.setFullName(fullNameField.getText().trim());
            user.setEmail(emailField.getText().trim());
            user.setPhone(phoneField.getText().trim());

            // Set password only for new users
            if (!isEditMode) {
                user.setPassword(passwordField.getText());
            }

            boolean success;
            if (isEditMode) {
                success = userDAO.updateUser(user);
            } else {
                // Check if username already exists
                if (userDAO.usernameExists(user.getUsername())) {
                    showErrorMessage("This username is already taken. Please choose another.");
                    return;
                }
                success = userDAO.insertUser(user);
            }

            if (success) {
                // Hide form
                userFormPane.setVisible(false);
                userFormPane.setManaged(false);

                // Refresh list
                loadUsers();
            } else {
                showErrorMessage("Failed to save user. Please try again.");
            }

        } catch (Exception e) {
            System.err.println("Error saving user: " + e.getMessage());
            showErrorMessage("Failed to save user: " + e.getMessage());
        }
    }

    /**
     * Handle cancel button click
     */
    @FXML
    private void handleCancel() {
        // Hide form
        userFormPane.setVisible(false);
        userFormPane.setManaged(false);
    }

    /**
     * Validate form input
     * @return true if valid, false otherwise
     */
    private boolean validateForm() {
        boolean valid = true;

        // Clear previous validation styles
        ValidationUtils.removeErrorStyle(usernameField);
        ValidationUtils.removeErrorStyle(passwordField);
        ValidationUtils.removeErrorStyle(confirmPasswordField);
        ValidationUtils.removeErrorStyle(roleComboBox);
        ValidationUtils.removeErrorStyle(fullNameField);
        ValidationUtils.removeErrorStyle(emailField);
        ValidationUtils.removeErrorStyle(phoneField);

        // Validate required fields
        if (!ValidationUtils.validateRequired(usernameField, "Username")) {
            valid = false;
        } else if (!ValidationUtils.validateUsername(usernameField)) {
            valid = false;
        }

        // Only validate passwords for new users
        if (!isEditMode) {
            if (!ValidationUtils.validateRequired(passwordField, "Password")) {
                valid = false;
            } else if (!ValidationUtils.validatePassword(passwordField)) {
                valid = false;
            }

            if (!ValidationUtils.validateRequired(confirmPasswordField, "Confirm Password")) {
                valid = false;
            } else if (!ValidationUtils.validatePasswordsMatch(passwordField, confirmPasswordField)) {
                valid = false;
            }
        }

        if (!ValidationUtils.validateComboBox(roleComboBox, "Role")) {
            valid = false;
        }

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
     * Show error message to user
     */
    private void showErrorMessage(String message) {
        if (rootPane != null && rootPane.getScene() != null) {
            AlertUtils.showError(rootPane.getScene().getWindow(), "User Management Error", message);
        }
    }
}