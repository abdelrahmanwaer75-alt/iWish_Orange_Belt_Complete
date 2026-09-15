package com.iwish.client;

import java.util.Map;

import com.iwish.common.Request;
import com.iwish.common.Response;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController{
    @FXML TextField email;
    @FXML PasswordField password;
    @FXML Label status;

    @FXML void login(){
        if(email.getText().isBlank()||password.getText().isBlank()){
            status.setText("Enter email and password");
            return;
        }
        try{
            Response r=ClientApp.api().send(new Request("LOGIN")
                    .put("email",email.getText())
                    .put("password",password.getText()));
            if(r.success){
                if(r.data instanceof Map<?,?> m) Session.set((Map<String,Object>)m);
                SceneNav.open((Stage)email.getScene().getWindow(),"home.fxml","i-Wish — Home");
            }else status.setText(r.message);
        }catch(Exception e){
            status.setText(e.getMessage());
        }
    }

    @FXML void register() throws Exception{
        SceneNav.open((Stage)email.getScene().getWindow(),"register.fxml","i-Wish — Register");
    }
}