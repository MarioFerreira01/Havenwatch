package com.havenwatch;

import com.havenwatch.database.DatabaseConnection;
import com.havenwatch.services.DataSimulationService;
import com.havenwatch.utils.NavigationManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main application class for HavenWatch
 */
public class Main extends Application {
    private DataSimulationService dataSimulationService;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Set the application icon
            // primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

            // Set up navigation manager
            NavigationManager.getInstance().setPrimaryStage(primaryStage);

            // Start the data simulation service (generate new data every 60 seconds)
            dataSimulationService = new DataSimulationService();
            dataSimulationService.startSimulation(60);

            // Navigate to login screen
            NavigationManager.getInstance().navigateToLogin();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Main method
     */
    public static void main(String[] args) {
        launch(args);
    }
}