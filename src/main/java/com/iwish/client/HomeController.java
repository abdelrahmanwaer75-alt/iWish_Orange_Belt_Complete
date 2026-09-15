package com.iwish.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class HomeController {
    public Node view(javafx.stage.Stage stage) {
        Label brand = new Label("i-Wish 🎁");
        Label welcome = new Label("Welcome, " + Session.name() + " 👋");

        VBox top = new VBox(5, brand, welcome);
        top.setPadding(new Insets(20));
        top.getStyleClass().add("topbar");

        Button wishlist = new Button("🎁 My Wish List");
        Button friends = new Button("👥 Friends");
        Button discover = new Button("💝 Friends' Wish Lists");
        Button notifications = new Button("🔔 Notifications");
        Button logout = new Button("Sign Out");

        for (Button b : new Button[]{wishlist, friends, discover, notifications}) {
            b.setPrefSize(250, 80);
            b.getStyleClass().add("tile");
        }

        wishlist.setOnAction(e -> SceneNav.open(stage, "wishlist", "My Wish List"));
        friends.setOnAction(e -> SceneNav.open(stage, "friends", "Friends"));
        discover.setOnAction(e -> SceneNav.open(stage, "discover", "Friends' Wish Lists"));
        notifications.setOnAction(e -> SceneNav.open(stage, "notifications", "Notifications"));
        logout.setOnAction(e -> {
            Session.clear();
            SceneNav.open(stage, "login", "i-Wish — Sign in");
        });

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        grid.add(wishlist, 0, 0);
        grid.add(friends, 1, 0);
        grid.add(discover, 0, 1);
        grid.add(notifications, 1, 1);

        Label title = new Label("What would you like to do?");
        title.getStyleClass().add("title2");

        VBox center = new VBox(18, title, grid, logout);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(30));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(center);
        return root;
    }
}
