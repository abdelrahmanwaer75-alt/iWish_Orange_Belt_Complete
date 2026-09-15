package com.iwish.client;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SceneNav {

    private SceneNav() {}

    public static void open(Stage stage, String screen, String title) {

        Parent root = switch (screen) {
            case "login" -> (Parent) new LoginController().view(stage);
            case "register" -> (Parent) new RegisterController().view(stage);
            case "home" -> (Parent) new HomeController().view(stage);
            case "friends" -> (Parent) new FriendsController().view(stage);
            case "discover" -> (Parent) new DiscoverController().view(stage);
            case "notifications" -> (Parent) new NotificationsController().view(stage);
            case "wishlist" -> (Parent) new WishlistController().view(stage);
            default -> throw new IllegalArgumentException("Unknown screen: " + screen);
        };

        Scene scene = new Scene(root, 1000, 650);

        var css = SceneNav.class.getResource("/css/style.css");

        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle(title);
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(550);
        stage.show();
    }
}