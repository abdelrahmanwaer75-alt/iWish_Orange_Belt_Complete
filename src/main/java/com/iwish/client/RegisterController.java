package com.iwish.client;

import com.iwish.common.Request;
import com.iwish.common.Response;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController{
    @FXML TextField name,email;
    @FXML PasswordField password,confirm;
    @FXML Label status;

    @FXML void register(){
        if(name.getText().isBlank()||email.getText().isBlank()||password.getText().isBlank()){
            status.setText("Fill all fields");
            return;
        }
        if(!password.getText().equals(confirm.getText())){
            status.setText("Passwords do not match");
            return;
        }
        try{
            Response r=ClientApp.api().send(new Request("REGISTER")
                    .put("name",name.getText())
                    .put("email",email.getText())
                    .put("password",password.getText()));
            if(r.success){Ui.info("Welcome",r.message);back();}
            else status.setText(r.message);
        }catch(Exception e){status.setText(e.getMessage());}
    }

    @FXML void back() throws Exception{
        SceneNav.open((Stage)name.getScene().getWindow(),"login.fxml","i-Wish — Sign in");
    }
}