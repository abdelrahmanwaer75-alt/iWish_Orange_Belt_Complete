package com.iwish.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class HomeController {

    public Node view(javafx.stage.Stage stage) {

        Label brand = new Label("i-Wish 🎁");
        brand.getStyleClass().add("brand");

        Label welcome = new Label("Welcome back, " + Session.name() + " 👋");
        welcome.getStyleClass().add("welcome");

        Label subtitle = new Label("Manage your wishes and stay connected with your friends.");
        subtitle.getStyleClass().add("subtitle");

        VBox headerText = new VBox(6, brand, welcome, subtitle);
        headerText.setAlignment(Pos.CENTER_LEFT);

        Button logout = new Button("Sign Out");
        logout.getStyleClass().add("logout-button");

        logout.setOnAction(e -> {
            Session.clear();
            SceneNav.open(stage, "login", "i-Wish — Sign in");
        });

        BorderPane topBar = new BorderPane();
        topBar.setLeft(headerText);
        topBar.setRight(logout);
        topBar.setPadding(new Insets(25, 35, 25, 35));
        topBar.getStyleClass().add("topbar");

        Label pageTitle = new Label("Your Dashboard");
        pageTitle.getStyleClass().add("page-title");

        Label pageSubtitle = new Label("What would you like to do today?");
        pageSubtitle.getStyleClass().add("page-subtitle");

        VBox pageHeader = new VBox(5, pageTitle, pageSubtitle);
        pageHeader.setAlignment(Pos.CENTER_LEFT);

        Button wishlist = createCard(
                "🎁",
                "My Wish List",
                "View and manage your wishes"
        );

        Button friends = createCard(
                "👥",
                "Friends",
                "Manage your friends and requests"
        );

        Button discover = createCard(
                "💝",
                "Friends' Wish Lists",
                "Discover what your friends wish for"
        );

        Button notifications = createCard(
                "🔔",
                "Notifications",
                "Check your latest notifications"
        );

        wishlist.setOnAction(e ->
                SceneNav.open(stage, "wishlist", "My Wish List")
        );

        friends.setOnAction(e ->
                SceneNav.open(stage, "friends", "Friends")
        );

        discover.setOnAction(e ->
                SceneNav.open(stage, "discover", "Friends' Wish Lists")
        );

        notifications.setOnAction(e ->
                SceneNav.open(stage, "notifications", "Notifications")
        );

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        grid.add(wishlist, 0, 0);
        grid.add(friends, 1, 0);
        grid.add(discover, 0, 1);
        grid.add(notifications, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);

        grid.getColumnConstraints().addAll(col1, col2);

        VBox content = new VBox(25, pageHeader, grid);
        content.setPadding(new Insets(35));
        content.setMaxWidth(900);

        StackPane center = new StackPane(content);
        center.setAlignment(Pos.TOP_CENTER);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(center);
        root.getStyleClass().add("home-root");

        return root;
    }

    private Button createCard(String icon, String title, String description) {

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("card-icon");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("card-description");

        VBox content = new VBox(
                10,
                iconLabel,
                titleLabel,
                descriptionLabel
        );

        content.setAlignment(Pos.CENTER_LEFT);

        Button card = new Button();
        card.setGraphic(content);
        card.setPrefHeight(180);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMaxHeight(Double.MAX_VALUE);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("dashboard-card");

        return card;
    }
}