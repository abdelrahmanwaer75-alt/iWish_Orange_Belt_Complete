package com.iwish.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    private static ApiClient api;

    public static ApiClient api() throws Exception {
        if (api == null) api = new ApiClient("127.0.0.1", 5555);
        return api;
    }

    @Override
    public void start(Stage stage) {
        SceneNav.open(stage, "login", "i-Wish — Sign in");
    }

    @Override
    public void stop() {
        if (api != null) api.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
