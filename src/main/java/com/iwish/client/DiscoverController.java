package com.iwish.client;

import com.iwish.common.Request;
import com.iwish.common.Response;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.*;

public class DiscoverController {
    @FXML ListView<String> friendsList,wishesList;
    @FXML TextField amount;
    @FXML Label status;
    private final List<Map<String,Object>> friends=new ArrayList<>();
    private final List<Map<String,Object>> wishes=new ArrayList<>();

    @FXML public void initialize(){loadFriends();}

    @FXML void loadFriends(){
        try{Response r=ClientApp.api().send(new Request("FRIENDS").put("userId",Session.id()));friends.clear();
            for(var f:r.rows)if("ACCEPTED".equals(f.get("status")))friends.add(f);
            List<String>x=new ArrayList<>();for(var f:friends)x.add(f.get("name")+" — "+f.get("email"));
            friendsList.setItems(FXCollections.observableArrayList(x));
        }catch(Exception e){Ui.error(e.getMessage());}
    }
    @FXML void view(){
        int i=friendsList.getSelectionModel().getSelectedIndex();if(i<0)return;
        try{Response r=ClientApp.api().send(new Request("FRIEND_WISHLIST").put("userId",Session.id()).put("ownerId",((Number)friends.get(i).get("id")).intValue()));
            wishes.clear();wishes.addAll(r.rows);List<String>x=new ArrayList<>();for(var w:wishes)x.add(w.get("custom_name")+" | Remaining: "+w.get("remaining")+" EGP | "+w.get("status"));wishesList.setItems(FXCollections.observableArrayList(x));
        }catch(Exception e){Ui.error(e.getMessage());}
    }
    @FXML void contribute(){
        int i=wishesList.getSelectionModel().getSelectedIndex();if(i<0){status.setText("Select an item.");return;}
        try{Response r=ClientApp.api().send(new Request("CONTRIBUTE").put("userId",Session.id()).put("wishId",((Number)wishes.get(i).get("id")).intValue()).put("amount",amount.getText()));status.setText(r.message);view();}catch(Exception e){Ui.error(e.getMessage());}
    }
    @FXML void back() throws Exception {SceneNav.open((Stage)friendsList.getScene().getWindow(),"home.fxml","i-Wish — Home");}
}
