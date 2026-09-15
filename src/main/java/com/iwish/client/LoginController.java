package com.iwish.client;

import java.util.Map;

import com.iwish.common.Request;
import com.iwish.common.Response;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginController {

    private TextField email;
    private PasswordField password;
    private Label status;

    public Node view(javafx.stage.Stage stage) {

        Label brand = new Label("i-Wish 🎁");
        brand.getStyleClass().add("login-brand");

        Label title = new Label("Welcome Back");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label("Sign in to continue to your account");
        subtitle.getStyleClass().add("login-subtitle");

        email = new TextField();
        email.setPromptText("Email");
        email.getStyleClass().add("login-field");

        password = new PasswordField();
        password.setPromptText("Password");
        password.getStyleClass().add("login-field");

        status = new Label();
        status.getStyleClass().add("error");
        status.setWrapText(true);

        Button login = new Button("Sign In");
        login.getStyleClass().add("login-button");
        login.setMaxWidth(Double.MAX_VALUE);

        Button register = new Button("Create New Account");
        register.getStyleClass().add("register-button");
        register.setMaxWidth(Double.MAX_VALUE);

        login.setOnAction(e -> login(stage));

        register.setOnAction(e ->
                SceneNav.open(stage, "register", "i-Wish — Register")
        );

        VBox card = new VBox(
                15,
                brand,
                title,
                subtitle,
                email,
                password,
                login,
                register,
                status
        );

        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(430);
        card.setPadding(new Insets(40));
        card.getStyleClass().add("login-card");

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("login-root");

        return root;
    }

    private void login(javafx.stage.Stage stage) {

        if (email.getText().isBlank() || password.getText().isBlank()) {
            status.setText("Please enter your email and password.");
            return;
        }

        try {

            Response r = ClientApp.api().send(
                    new Request("LOGIN")
                            .put("email", email.getText().trim())
                            .put("password", password.getText())
            );

            if (r.success) {

                if (r.data instanceof Map<?, ?> m) {
                    Session.set((Map<String, Object>) m);
                }

                SceneNav.open(stage, "home", "i-Wish — Home");

            } else {

                status.setText(r.message);
            }

        } catch (Exception e) {

            status.setText(e.getMessage());
        }
    }
}