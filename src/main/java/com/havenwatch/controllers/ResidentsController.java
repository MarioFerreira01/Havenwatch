package com.havenwatch.controllers;

import com.havenwatch.database.ResidentDAO;
import com.havenwatch.models.Resident;
import com.havenwatch.services.AccessControlService;
import com.havenwatch.utils.AlertUtils;
import com.havenwatch.utils.DateTimeUtils;
import com.havenwatch.utils.NavigationManager;
import com.havenwatch.utils.ValidationUtils;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for the residents management view with proper access control
 */
public class ResidentsController {
    @FXML
    private VBox rootPane;

    @FXML
    private TableView<Resident> residentsTable;

    @FXML
    private TableColumn<Resident, Integer> idColumn;

    @FXML
    private TableColumn<Resident, String> nameColumn;

    @FXML
    private TableColumn<Resident, String> ageColumn;

    @FXML
    private TableColumn<Resident, String> genderColumn;

    @FXML
    private TableColumn<Resident, String> conditionsColumn;

    @FXML
    private TableColumn<Resident, String> contactColumn;

    @FXML
    private TableColumn<Resident, Void> actionsColumn;

    @FXML
    private Label totalResidentsLabel;

    @FXML
    private VBox residentFormPane;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private DatePicker dateOfBirthPicker;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private TextField addressField;

    @FXML
    private TextField emergencyContactField;

    @FXML
    private TextField emergencyPhoneField;

    @FXML
    private TextArea medicalConditionsArea;

    @FXML
    private TextArea medicationsArea;

    @FXML
    private TextArea allergiesArea;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private final ResidentDAO residentDAO = new ResidentDAO();
    private final AccessControlService accessControlService = new AccessControlService();

    private ObservableList<Resident> residentsList = FXCollections.observableArrayList();
    private Resident currentResident;
    private boolean isEditMode = false;

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        try {
            // Set up table columns
            idColumn.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(cellData.getValue().getResidentId()).asObject());

            nameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getFullName()));

            ageColumn.setCellValueFactory(cellData -> {
                int age = DateTimeUtils.calculateAge(cellData.getValue().getDateOfBirth());
                return new SimpleStringProperty(age + " years");
            });

            genderColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getGender().toString()));

            conditionsColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getMedicalConditions()));

            contactColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getEmergencyContact() + " (" +
                            cellData.getValue().getEmergencyPhone() + ")"));

            // Set up actions column with buttons based on permissions
            actionsColumn.setCellFactory(param -> new TableCell<Resident, Void>() {
                private final Button viewButton = new Button("View");
                private final Button editButton = new Button("Edit");
                private final Button deleteButton = new Button("Delete");
                private final HBox pane = new HBox(5, viewButton, editButton, deleteButton);

                {
                    viewButton.getStyleClass().add("small-button");
                    editButton.getStyleClass().add("small-button");
                    deleteButton.getStyleClass().add("small-button");

                    viewButton.setOnAction(event -> {
                        Resident resident = getTableView().getItems().get(getIndex());
                        showResidentDetails(resident);
                    });

                    editButton.setOnAction(event -> {
                        Resident resident = getTableView().getItems().get(getIndex());
                        editResident(resident);
                    });

                    deleteButton.setOnAction(event -> {
                        Resident resident = getTableView().getItems().get(getIndex());
                        deleteResident(resident);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Resident resident = getTableView().getItems().get(getIndex());

                        // Show/hide buttons based on permissions
                        editButton.setVisible(accessControlService.canEditResident(resident.getResidentId()));
                        deleteButton.setVisible(accessControlService.canDeleteResident(resident.getResidentId()));

                        setGraphic(pane);
                    }
                }
            });

            // Set up gender combo box
            genderComboBox.setItems(FXCollections.observableArrayList("MALE", "FEMALE", "OTHER"));

            // Hide form initially
            residentFormPane.setVisible(false);
            residentFormPane.setManaged(false);

            // Load initial data
            loadResidents();

        } catch (Exception e) {
            System.err.println("Error initializing residents controller: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Failed to initialize residents view: " + e.getMessage());
        }
    }

    /**
     * Load residents with proper access control
     */
    private void loadResidents() {
        try {
            // Load only accessible residents
            List<Resident> residents = accessControlService.getAccessibleResidents();

            residentsList.setAll(residents);
            residentsTable.setItems(residentsList);

            // Update count label
            totalResidentsLabel.setText(String.valueOf(residents.size()));

        } catch (Exception e) {
            System.err.println("Error loading residents: " + e.getMessage());
            showErrorMessage("Failed to load residents: " + e.getMessage());
        }
    }

    /**
     * Handle add resident button click
     */
    @FXML
    private void handleAddResident() {
        try {
            // Check permission to create residents
            if (!accessControlService.canCreateResidents()) {
                showErrorMessage("You do not have permission to create new residents.");
                return;
            }

            // Clear form
            clearForm();

            // Show form for adding
            isEditMode = false;
            currentResident = null;
            saveButton.setText("Add Resident");

            residentFormPane.setVisible(true);
            residentFormPane.setManaged(true);

        } catch (Exception e) {
            System.err.println("Error handling add resident: " + e.getMessage());
            showErrorMessage("Failed to open add resident form: " + e.getMessage());
        }
    }

    /**
     * Show resident details in read-only mode
     */
    private void showResidentDetails(Resident resident) {
        try {
            // Check access permission
            if (!accessControlService.canAccessResident(resident.getResidentId())) {
                showErrorMessage("You do not have permission to view this resident's details.");
                return;
            }

            // Use same form in read-only mode
            currentResident = resident;
            populateForm(resident);

            // Disable all form fields
            setFormEditable(false);

            // Show form
            residentFormPane.setVisible(true);
            residentFormPane.setManaged(true);
            saveButton.setVisible(false);
            cancelButton.setText("Close");

        } catch (Exception e) {
            System.err.println("Error showing resident details: " + e.getMessage());
            showErrorMessage("Failed to show resident details: " + e.getMessage());
        }
    }

    /**
     * Edit an existing resident
     */
    private void editResident(Resident resident) {
        try {
            // Check edit permission
            if (!accessControlService.canEditResident(resident.getResidentId())) {
                showErrorMessage("You do not have permission to edit this resident.");
                return;
            }

            // Set up form for editing
            isEditMode = true;
            currentResident = resident;
            populateForm(resident);

            // Enable form fields
            setFormEditable(true);

            // Show form
            residentFormPane.setVisible(true);
            residentFormPane.setManaged(true);
            saveButton.setVisible(true);
            saveButton.setText("Update Resident");
            cancelButton.setText("Cancel");

        } catch (Exception e) {
            System.err.println("Error editing resident: " + e.getMessage());
            showErrorMessage("Failed to edit resident: " + e.getMessage());
        }
    }

    /**
     * Delete a resident
     */
    private void deleteResident(Resident resident) {
        try {
            // Check delete permission
            if (!accessControlService.canDeleteResident(resident.getResidentId())) {
                showErrorMessage("You do not have permission to delete this resident.");
                return;
            }

            boolean confirmed = AlertUtils.showConfirmation(
                    rootPane.getScene().getWindow(),
                    "Confirm Delete",
                    "Are you sure you want to delete resident " + resident.getFullName() + "? This action cannot be undone."
            );

            if (confirmed) {
                if (residentDAO.deleteResident(resident.getResidentId())) {
                    // Refresh the list
                    loadResidents();
                } else {
                    showErrorMessage("Failed to delete resident. Please try again.");
                }
            }

        } catch (Exception e) {
            System.err.println("Error deleting resident: " + e.getMessage());
            showErrorMessage("Failed to delete resident: " + e.getMessage());
        }
    }

    /**
     * Populate the form with resident data
     */
    private void populateForm(Resident resident) {
        firstNameField.setText(resident.getFirstName());
        lastNameField.setText(resident.getLastName());
        dateOfBirthPicker.setValue(resident.getDateOfBirth());
        genderComboBox.setValue(resident.getGender().toString());
        addressField.setText(resident.getAddress());
        emergencyContactField.setText(resident.getEmergencyContact());
        emergencyPhoneField.setText(resident.getEmergencyPhone());
        medicalConditionsArea.setText(resident.getMedicalConditions());
        medicationsArea.setText(resident.getMedications());
        allergiesArea.setText(resident.getAllergies());
    }

    /**
     * Clear the form
     */
    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        dateOfBirthPicker.setValue(null);
        genderComboBox.getSelectionModel().clearSelection();
        addressField.clear();
        emergencyContactField.clear();
        emergencyPhoneField.clear();
        medicalConditionsArea.clear();
        medicationsArea.clear();
        allergiesArea.clear();

        // Enable form fields
        setFormEditable(true);

        // Reset buttons
        saveButton.setVisible(true);
        cancelButton.setText("Cancel");
    }

    /**
     * Set form fields editable state
     */
    private void setFormEditable(boolean editable) {
        firstNameField.setEditable(editable);
        lastNameField.setEditable(editable);
        dateOfBirthPicker.setDisable(!editable);
        genderComboBox.setDisable(!editable);
        addressField.setEditable(editable);
        emergencyContactField.setEditable(editable);
        emergencyPhoneField.setEditable(editable);
        medicalConditionsArea.setEditable(editable);
        medicationsArea.setEditable(editable);
        allergiesArea.setEditable(editable);
    }

    /**
     * Handle save button click
     */
    @FXML
    private void handleSave() {
        try {
            // Validate form
            if (!validateForm()) {
                return;
            }

            // Create or update resident
            Resident resident = isEditMode ? currentResident : new Resident();

            resident.setFirstName(firstNameField.getText().trim());
            resident.setLastName(lastNameField.getText().trim());
            resident.setDateOfBirth(dateOfBirthPicker.getValue());
            resident.setGender(Resident.Gender.valueOf(genderComboBox.getValue()));
            resident.setAddress(addressField.getText().trim());
            resident.setEmergencyContact(emergencyContactField.getText().trim());
            resident.setEmergencyPhone(emergencyPhoneField.getText().trim());
            resident.setMedicalConditions(medicalConditionsArea.getText().trim());
            resident.setMedications(medicationsArea.getText().trim());
            resident.setAllergies(allergiesArea.getText().trim());

            boolean success;
            if (isEditMode) {
                // Check edit permission again
                if (!accessControlService.canEditResident(resident.getResidentId())) {
                    showErrorMessage("You do not have permission to edit this resident.");
                    return;
                }
                success = residentDAO.updateResident(resident);
            } else {
                // Check create permission again
                if (!accessControlService.canCreateResidents()) {
                    showErrorMessage("You do not have permission to create new residents.");
                    return;
                }
                success = residentDAO.insertResident(resident);

                // If successful add and not admin, assign to current user
                if (success && !accessControlService.canManageUsers()) {
                    // Get current user ID from SessionManager
                    int userId = com.havenwatch.utils.SessionManager.getInstance().getCurrentUser().getUserId();
                    residentDAO.assignResidentToUser(resident.getResidentId(), userId);
                }
            }

            if (success) {
                // Hide form
                residentFormPane.setVisible(false);
                residentFormPane.setManaged(false);

                // Refresh list
                loadResidents();
            } else {
                showErrorMessage("Failed to save resident. Please try again.");
            }

        } catch (Exception e) {
            System.err.println("Error saving resident: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Failed to save resident: " + e.getMessage());
        }
    }

    /**
     * Handle cancel button click
     */
    @FXML
    private void handleCancel() {
        // Hide form
        residentFormPane.setVisible(false);
        residentFormPane.setManaged(false);
    }

    /**
     * Handle manage care team button click
     */
    @FXML
    private void handleManageCareTeam() {
        try {
            Resident selectedResident = residentsTable.getSelectionModel().getSelectedItem();

            if (selectedResident == null) {
                AlertUtils.showWarning(
                        rootPane.getScene().getWindow(),
                        "No Selection",
                        "Please select a resident first."
                );
                return;
            }

            // Check if user can assign residents (admin only)
            if (!accessControlService.canAssignResidents()) {
                showErrorMessage("You do not have permission to manage care teams.");
                return;
            }

            // Show care team management dialog
            FXMLLoader loader = NavigationManager.getInstance().showDialog(
                    "care_team_dialog",
                    "Manage Care Team - " + selectedResident.getFullName()
            );

            // Set resident data in the controller
            CareTeamDialogController controller = loader.getController();
            controller.setResident(selectedResident);

        } catch (Exception e) {
            System.err.println("Error opening care team dialog: " + e.getMessage());
            showErrorMessage("Failed to open care team management dialog: " + e.getMessage());
        }
    }

    /**
     * Validate form input
     * @return true if valid, false otherwise
     */
    private boolean validateForm() {
        boolean valid = true;

        // Clear previous validation styles
        ValidationUtils.removeErrorStyle(firstNameField);
        ValidationUtils.removeErrorStyle(lastNameField);
        ValidationUtils.removeErrorStyle(dateOfBirthPicker);
        ValidationUtils.removeErrorStyle(genderComboBox);
        ValidationUtils.removeErrorStyle(emergencyContactField);
        ValidationUtils.removeErrorStyle(emergencyPhoneField);

        // Validate required fields
        if (!ValidationUtils.validateRequired(firstNameField, "First Name")) {
            valid = false;
        }

        if (!ValidationUtils.validateRequired(lastNameField, "Last Name")) {
            valid = false;
        }

        if (!ValidationUtils.validateDatePicker(dateOfBirthPicker, "Date of Birth")) {
            valid = false;
        } else {
            // Check that date is not in the future
            if (dateOfBirthPicker.getValue().isAfter(LocalDate.now())) {
                ValidationUtils.setErrorStyle(dateOfBirthPicker);
                dateOfBirthPicker.setPromptText("Date cannot be in the future");
                valid = false;
            }
        }

        if (!ValidationUtils.validateComboBox(genderComboBox, "Gender")) {
            valid = false;
        }

        if (!ValidationUtils.validateRequired(emergencyContactField, "Emergency Contact")) {
            valid = false;
        }

        if (!ValidationUtils.validateRequired(emergencyPhoneField, "Emergency Phone")) {
            valid = false;
        } else if (!ValidationUtils.validatePhone(emergencyPhoneField)) {
            valid = false;
        }

        return valid;
    }

    /**
     * Show error message to user
     */
    private void showErrorMessage(String message) {
        if (rootPane != null && rootPane.getScene() != null) {
            AlertUtils.showError(rootPane.getScene().getWindow(), "Residents Error", message);
        }
    }
}