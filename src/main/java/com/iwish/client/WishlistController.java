package com.iwish.client;

import com.iwish.common.Request;
import com.iwish.common.Response;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.util.*;

public class WishlistController {
    @FXML ListView<String> list;
    @FXML TextField name,price,description;
    @FXML Label status;
    private final List<Map<String,Object>> wishes=new ArrayList<>();
    @FXML public void initialize(){load();}

    @FXML void load(){
        try{Response r=ClientApp.api().send(new Request("MY_WISHLIST").put("userId",Session.id()));wishes.clear();wishes.addAll(r.rows);
            List<String> x=new ArrayList<>();for(var w:wishes)x.add(w.get("custom_name")+" | "+w.get("collected_amount")+"/"+w.get("price")+" EGP | "+w.get("status"));
            list.setItems(FXCollections.observableArrayList(x));
        }catch(Exception e){Ui.error(e.getMessage());}
    }
    @FXML void add(){
        try{new BigDecimal(price.getText());}catch(Exception e){status.setText("Enter a valid price.");return;}
        try{Response r=ClientApp.api().send(new Request("ADD_WISH").put("userId",Session.id()).put("name",name.getText()).put("description",description.getText()).put("price",price.getText()));status.setText(r.message);clear();load();}catch(Exception e){Ui.error(e.getMessage());}
    }
    @FXML void update(){
        int i=list.getSelectionModel().getSelectedIndex();if(i<0){status.setText("Select an item.");return;}
        try{Response r=ClientApp.api().send(new Request("UPDATE_WISH").put("userId",Session.id()).put("wishId",((Number)wishes.get(i).get("id")).intValue()).put("name",name.getText()).put("description",description.getText()).put("price",price.getText()));status.setText(r.message);load();}catch(Exception e){Ui.error(e.getMessage());}
    }
    @FXML void delete(){
        int i=list.getSelectionModel().getSelectedIndex();if(i<0)return;
        try{Response r=ClientApp.api().send(new Request("DELETE_WISH").put("userId",Session.id()).put("wishId",((Number)wishes.get(i).get("id")).intValue()));status.setText(r.message);load();}catch(Exception e){Ui.error(e.getMessage());}
    }
    @FXML void select(){int i=list.getSelectionModel().getSelectedIndex();if(i>=0){var w=wishes.get(i);name.setText(String.valueOf(w.get("custom_name")));price.setText(String.valueOf(w.get("price")));description.setText(String.valueOf(w.get("description")));}}
    private void clear(){name.clear();price.clear();description.clear();}
    @FXML void back() throws Exception {SceneNav.open((Stage)list.getScene().getWindow(),"home.fxml","i-Wish — Home");}
}
