package com.iwish.client;

import com.iwish.common.Request;
import com.iwish.common.Response;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Map;

public class LoginController {
    private TextField email;
    private PasswordField password;
    private Label status;

    public Node view(javafx.stage.Stage stage) {
        Label brand = new Label("i-Wish");
        Label title = new Label("Sign In");
        email = new TextField();
        email.setPromptText("Email");
        password = new PasswordField();
        password.setPromptText("Password");
        status = new Label();

        Button login = new Button("Sign In");
        Button register = new Button("Create Account");
        login.setOnAction(e -> login(stage));
        register.setOnAction(e -> SceneNav.open(stage, "register", "i-Wish — Register"));

        VBox box = new VBox(15, brand, title, email, password, login, register, status);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(400);
        box.setPadding(new Insets(30));

        BorderPane root = new BorderPane(box);
        root.setPadding(new Insets(40));
        return root;
    }

    private void login(javafx.stage.Stage stage) {
        if (email.getText().isBlank() || password.getText().isBlank()) {
            status.setText("Enter email and password");
            return;
        }
        try {
            Response r = ClientApp.api().send(new Request("LOGIN")
                    .put("email", email.getText().trim())
                    .put("password", password.getText()));

            if (r.success) {
                if (r.data instanceof Map<?, ?> m) Session.set((Map<String, Object>) m);
                SceneNav.open(stage, "home", "i-Wish — Home");
            } else {
                status.setText(r.message);
            }
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }
}
