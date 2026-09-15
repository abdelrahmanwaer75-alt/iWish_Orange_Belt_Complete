package com.iwish.client;

import com.iwish.common.Request;
import com.iwish.common.Response;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class RegisterController {
    private TextField name;
    private TextField email;
    private PasswordField password;
    private PasswordField confirm;
    private Label status;

    public Node view(javafx.stage.Stage stage) {
        Label brand = new Label("i-Wish");
        Label title = new Label("Create Account");

        name = new TextField();
        name.setPromptText("Full Name");
        email = new TextField();
        email.setPromptText("Email");
        password = new PasswordField();
        password.setPromptText("Password");
        confirm = new PasswordField();
        confirm.setPromptText("Confirm Password");
        status = new Label();

        Button create = new Button("Create Account");
        Button back = new Button("Back to Login");
        create.setOnAction(e -> register(stage));
        back.setOnAction(e -> SceneNav.open(stage, "login", "i-Wish — Sign in"));

        VBox box = new VBox(15, brand, title, name, email, password, confirm, create, back, status);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(400);
        box.setPadding(new Insets(30));

        return new BorderPane(box);
    }

    private void register(javafx.stage.Stage stage) {
        if (name.getText().isBlank() || email.getText().isBlank() || password.getText().isBlank()) {
            status.setText("Fill all fields");
            return;
        }
        if (!password.getText().equals(confirm.getText())) {
            status.setText("Passwords do not match");
            return;
        }
        if (password.getText().length() < 8) {
            status.setText("Password must be at least 8 characters");
            return;
        }

        try {
            Response r = ClientApp.api().send(new Request("REGISTER")
                    .put("name", name.getText().trim())
                    .put("email", email.getText().trim())
                    .put("password", password.getText()));

            if (r.success) {
                Ui.info("Welcome", r.message);
                SceneNav.open(stage, "login", "i-Wish — Sign in");
            } else {
                status.setText(r.message);
            }
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }
}
