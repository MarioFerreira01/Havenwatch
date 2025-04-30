package com.havenwatch.controllers;

import com.havenwatch.database.ResidentDAO;
import com.havenwatch.database.UserDAO;
import com.havenwatch.models.Resident;
import com.havenwatch.models.User;
import com.havenwatch.utils.AlertUtils;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller for the care team management dialog
 */
public class CareTeamDialogController {
    @FXML
    private VBox rootPane;

    @FXML
    private Label residentNameLabel;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, Boolean> accessColumn;

    @FXML
    private Button closeButton;

    private final UserDAO userDAO = new UserDAO();
    private final ResidentDAO residentDAO = new ResidentDAO();

    private Resident resident;
    private List<Integer> usersWithAccess;

    /**
     * Set the resident for this dialog
     * @param resident The resident to manage care team for
     */
    public void setResident(Resident resident) {
        this.resident = resident;
        residentNameLabel.setText(resident.getFullName());

        // Get users with access to this resident
        usersWithAccess = residentDAO.getUsersWithAccessToResident(resident.getResidentId());

        // Load users
        loadUsers();
    }

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Set up table columns
        usernameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUsername()));

        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFullName()));

        roleColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRole().toString()));

        // Add checkbox for access
        accessColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            boolean hasAccess = usersWithAccess != null && usersWithAccess.contains(user.getUserId());
            return new SimpleBooleanProperty(hasAccess);
        });

        accessColumn.setCellFactory(column -> new TableCell<User, Boolean>() {
            private final javafx.scene.control.CheckBox checkBox = new javafx.scene.control.CheckBox();

            {
                checkBox.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    boolean newValue = checkBox.isSelected();

                    if (newValue) {
                        // Grant access
                        if (residentDAO.assignResidentToUser(resident.getResidentId(), user.getUserId())) {
                            usersWithAccess.add(user.getUserId());
                        } else {
                            // If failed, revert checkbox
                            checkBox.setSelected(false);
                            AlertUtils.showError(rootPane.getScene().getWindow(),
                                    "Error", "Failed to grant access to user.");
                        }
                    } else {
                        // Remove access
                        if (residentDAO.removeResidentFromUser(resident.getResidentId(), user.getUserId())) {
                            usersWithAccess.remove(Integer.valueOf(user.getUserId()));
                        } else {
                            // If failed, revert checkbox
                            checkBox.setSelected(true);
                            AlertUtils.showError(rootPane.getScene().getWindow(),
                                    "Error", "Failed to remove access from user.");
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(checkBox);
                }
            }
        });
    }

    /**
     * Load users from the database
     */
    private void loadUsers() {
        List<User> users = userDAO.getAllUsers();
        usersTable.setItems(FXCollections.observableArrayList(users));
    }

    /**
     * Handle close button click
     */
    @FXML
    private void handleClose() {
        ((Stage) rootPane.getScene().getWindow()).close();
    }
}