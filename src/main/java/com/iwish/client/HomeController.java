package com.iwish.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HomeController {
    @FXML Label welcome;

    @FXML public void initialize() {
        welcome.setText("Welcome, " + Session.name() + " 👋");
    }

    @FXML void wishlist() throws Exception {
        SceneNav.open((Stage) welcome.getScene().getWindow(), "wishlist.fxml", "My Wish List");
    }

    @FXML void friends() throws Exception {
        SceneNav.open((Stage) welcome.getScene().getWindow(), "friends.fxml", "Friends");
    }

    @FXML void discover() throws Exception {
        SceneNav.open((Stage) welcome.getScene().getWindow(), "discover.fxml", "Friends' Wish Lists");
    }

    @FXML void notifications() throws Exception {
        SceneNav.open((Stage) welcome.getScene().getWindow(), "notifications.fxml", "Notifications");
    }

    @FXML void logout() throws Exception {
        Session.clear();
        SceneNav.open((Stage) welcome.getScene().getWindow(), "login.fxml", "i-Wish — Sign in");
    }
}