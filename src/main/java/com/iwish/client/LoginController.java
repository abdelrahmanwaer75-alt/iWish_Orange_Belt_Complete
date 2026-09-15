package com.iwish.client;

import com.iwish.common.Request;
import com.iwish.common.Response;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML TextField email;
    @FXML PasswordField password;
    @FXML Label status;

    @FXML void login(){
        try{
            Response r=ClientApp.api().send(new Request("LOGIN").put("email",email.getText()).put("password",password.getText()));
            if(r.success){Session.set((java.util.Map<String,Object>)r.data);SceneNav.open((Stage)email.getScene().getWindow(),"home.fxml","i-Wish — Home");}
            else status.setText(r.message);
        }catch(Exception e){status.setText("Cannot connect to server. Start the server first.");}
    }

    @FXML void register() throws Exception {SceneNav.open((Stage)email.getScene().getWindow(),"register.fxml","i-Wish — Register");}
}
