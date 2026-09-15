package com.iwish.client;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public final class Ui {
    private Ui(){}
    public static void info(String title,String msg){Alert a=new Alert(Alert.AlertType.INFORMATION);a.setTitle(title);a.setHeaderText(null);a.setContentText(msg);a.showAndWait();}
    public static void error(String msg){Alert a=new Alert(Alert.AlertType.ERROR);a.setTitle("i-Wish");a.setHeaderText("Something went wrong");a.setContentText(msg);a.showAndWait();}
    public static Optional<String> input(String title,String prompt){TextInputDialog d=new TextInputDialog();d.setTitle(title);d.setHeaderText(null);d.setContentText(prompt);return d.showAndWait();}
}
