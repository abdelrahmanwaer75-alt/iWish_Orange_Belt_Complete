package com.iwish.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    private static ApiClient api;

    public static ApiClient api() throws Exception {
        if(api==null) api=new ApiClient("127.0.0.1",5555);
        return api;
    }

    @Override public void start(Stage stage) throws Exception {
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene=new Scene(loader.load(),900,600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setTitle("i-Wish");
        stage.setScene(scene);
        stage.setMinWidth(800); stage.setMinHeight(550);
        stage.show();
    }

    @Override public void stop(){if(api!=null)api.close();}
    public static void main(String[] args){launch(args);}
}
