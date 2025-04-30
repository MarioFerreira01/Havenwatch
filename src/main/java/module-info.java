module com.havenwatch {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.havenwatch to javafx.fxml;
    opens com.havenwatch.controllers to javafx.fxml;
    opens com.havenwatch.models to javafx.base;

    exports com.havenwatch;
    exports com.havenwatch.controllers;
    exports com.havenwatch.models;
    exports com.havenwatch.utils;
    exports com.havenwatch.services;
}