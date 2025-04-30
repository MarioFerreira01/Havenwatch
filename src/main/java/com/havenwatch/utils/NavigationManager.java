package com.havenwatch.utils;

import com.havenwatch.Main;
import com.havenwatch.models.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to handle navigation between scenes
 */
public class NavigationManager {
    private static NavigationManager instance;
    private Stage primaryStage;
    private Scene mainScene;
    private BorderPane mainLayout;

    // Cache for loaded scenes to improve performance
    private final Map<String, Parent> sceneCache = new HashMap<>();

    private NavigationManager() {
        // Private constructor for singleton
    }

    /**
     * Get the singleton instance
     * @return NavigationManager instance
     */
    public static synchronized NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    /**
     * Set the primary stage reference
     * @param stage The primary application stage
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Navigate to login screen
     */
    public void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(Main.class.getResource("/fxml/login.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("HavenWatch - Login");
            primaryStage.setResizable(true);
            primaryStage.show();

            // Clear the scene cache when logging out
            sceneCache.clear();
        } catch (IOException e) {
            System.err.println("Error loading login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigate to registration screen
     */
    public void navigateToRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/register.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("HavenWatch - Register");
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading registration screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigate to the main application screen
     */
    public void navigateToMainScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/main.fxml"));
            mainLayout = loader.load();
            mainScene = new Scene(mainLayout);
            primaryStage.setScene(mainScene);

            User currentUser = SessionManager.getInstance().getCurrentUser();
            String role = currentUser.getRole().toString();
            primaryStage.setTitle("HavenWatch - " + role.charAt(0) + role.substring(1).toLowerCase() +
                    " - " + currentUser.getFullName());

            // Load the dashboard by default
            loadView("dashboard");

            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading main screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load a view into the main content area
     * @param viewName The name of the view to load (without .fxml extension)
     */
    public void loadView(String viewName) {
        try {
            // Check if the view is already cached
            Parent view = sceneCache.get(viewName);

            if (view == null) {
                // Load the view if not cached
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/" + viewName + ".fxml"));
                view = loader.load();
                sceneCache.put(viewName, view);
            }

            // Set the view in the center of the main layout
            mainLayout.setCenter(view);

        } catch (IOException e) {
            System.err.println("Error loading view " + viewName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the main layout
     * @return The main BorderPane layout
     */
    public BorderPane getMainLayout() {
        return mainLayout;
    }

    /**
     * Show a dialog
     * @param dialogName The name of the dialog to show (without .fxml extension)
     * @param title The title of the dialog
     * @return The dialog's FXMLLoader
     */
    public FXMLLoader showDialog(String dialogName, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/" + dialogName + ".fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initOwner(primaryStage);
            dialogStage.setScene(new Scene(root));
            dialogStage.show();

            return loader;
        } catch (IOException e) {
            System.err.println("Error showing dialog " + dialogName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}