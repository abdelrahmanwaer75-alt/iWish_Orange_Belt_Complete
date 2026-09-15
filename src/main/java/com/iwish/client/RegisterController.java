package com.iwish.client;

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

public class RegisterController {

    private TextField name;
    private TextField email;
    private PasswordField password;
    private PasswordField confirm;
    private Label status;

    public Node view(javafx.stage.Stage stage) {

        Label brand = new Label("i-Wish 🎁");
        brand.getStyleClass().add("login-brand");

        Label title = new Label("Create Account");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label("Create your account and start sharing wishes");
        subtitle.getStyleClass().add("login-subtitle");

        name = new TextField();
        name.setPromptText("Full Name");
        name.getStyleClass().add("login-field");

        email = new TextField();
        email.setPromptText("Email");
        email.getStyleClass().add("login-field");

        password = new PasswordField();
        password.setPromptText("Password");
        password.getStyleClass().add("login-field");

        confirm = new PasswordField();
        confirm.setPromptText("Confirm Password");
        confirm.getStyleClass().add("login-field");

        status = new Label();
        status.getStyleClass().add("error");
        status.setWrapText(true);

        Button create = new Button("Create Account");
        create.getStyleClass().add("login-button");
        create.setMaxWidth(Double.MAX_VALUE);

        Button back = new Button("Back to Sign In");
        back.getStyleClass().add("register-button");
        back.setMaxWidth(Double.MAX_VALUE);

        create.setOnAction(e -> register(stage));

        back.setOnAction(e ->
                SceneNav.open(stage, "login", "i-Wish — Sign in")
        );

        VBox card = new VBox(
                13,
                brand,
                title,
                subtitle,
                name,
                email,
                password,
                confirm,
                create,
                back,
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

    private void register(javafx.stage.Stage stage) {

        if (name.getText().isBlank()
                || email.getText().isBlank()
                || password.getText().isBlank()
                || confirm.getText().isBlank()) {

            status.setText("Please fill in all fields.");
            return;
        }

        if (!password.getText().equals(confirm.getText())) {
            status.setText("Passwords do not match.");
            return;
        }

        if (password.getText().length() < 8) {
            status.setText("Password must be at least 8 characters.");
            return;
        }

        try {

            Response r = ClientApp.api().send(
                    new Request("REGISTER")
                            .put("name", name.getText().trim())
                            .put("email", email.getText().trim())
                            .put("password", password.getText())
            );

            if (r.success) {

                Ui.info("Welcome to i-Wish", r.message);

                SceneNav.open(
                        stage,
                        "login",
                        "i-Wish — Sign in"
                );

            } else {

                status.setText(r.message);
            }

        } catch (Exception e) {

            status.setText(e.getMessage());
        }
    }
}