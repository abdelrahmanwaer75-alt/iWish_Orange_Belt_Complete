package com.iwish.client;

import com.iwish.common.Request;
import com.iwish.common.Response;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.*;

public class NotificationsController {
    @FXML ListView<String> list;
    @FXML Label status;
    private final List<Map<String,Object>> data=new ArrayList<>();
    @FXML public void initialize(){load();}
    @FXML void load(){
        try{Response r=ClientApp.api().send(new Request("NOTIFICATIONS").put("userId",Session.id()));data.clear();data.addAll(r.rows);
            List<String>x=new ArrayList<>();for(var n:data)x.add((Boolean.TRUE.equals(n.get("is_read"))?"✓ ":"● ")+n.get("message")+" ["+n.get("type")+"]");list.setItems(FXCollections.observableArrayList(x));
        }catch(Exception e){Ui.error(e.getMessage());}
    }
    @FXML void markRead(){
        int i=list.getSelectionModel().getSelectedIndex();if(i<0)return;
        try{Response r=ClientApp.api().send(new Request("MARK_READ").put("userId",Session.id()).put("notificationId",((Number)data.get(i).get("id")).intValue()));status.setText(r.message);load();}catch(Exception e){Ui.error(e.getMessage());}
    }
    @FXML void back() throws Exception {SceneNav.open((Stage)list.getScene().getWindow(),"home.fxml","i-Wish — Home");}
}
