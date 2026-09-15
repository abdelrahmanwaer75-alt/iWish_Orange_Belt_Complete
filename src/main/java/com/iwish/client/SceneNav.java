package com.iwish.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SceneNav {
    private SceneNav(){}
    public static void open(Stage stage,String fxml,String title) throws Exception {
        FXMLLoader l=new FXMLLoader(SceneNav.class.getResource("/fxml/"+fxml));
        Scene s=new Scene(l.load(),1000,650);
        s.getStylesheets().add(SceneNav.class.getResource("/css/style.css").toExternalForm());
        stage.setTitle(title);stage.setScene(s);
    }
}
