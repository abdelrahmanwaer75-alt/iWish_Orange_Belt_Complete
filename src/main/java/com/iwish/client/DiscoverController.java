package com.iwish.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.iwish.common.Request;
import com.iwish.common.Response;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DiscoverController {
    @FXML ListView<String> friendsList, wishesList;
    @FXML TextField amount;
    @FXML Label status;

    private final List<Map<String,Object>> friends = new ArrayList<>();
    private final List<Map<String,Object>> wishes = new ArrayList<>();

    @FXML public void initialize() {
        loadFriends();
    }

    @FXML void loadFriends() {
        try {
            Response r = ClientApp.api().send(new Request("FRIENDS").put("userId", Session.id()));
            if (!r.success) {
                status.setText(r.message);
                return;
            }

            friends.clear();
            for (Map<String,Object> f : r.rows)
                if ("ACCEPTED".equals(f.get("status"))) friends.add(f);

            List<String> x = new ArrayList<>();
            for (Map<String,Object> f : friends)
                x.add(f.get("name") + " — " + f.get("email"));

            friendsList.setItems(FXCollections.observableArrayList(x));
            wishesList.getItems().clear();
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }

    @FXML void view() {
        int i = friendsList.getSelectionModel().getSelectedIndex();
        if (i < 0 || i >= friends.size()) {
            status.setText("Select a friend.");
            return;
        }

        try {
            int ownerId = ((Number) friends.get(i).get("id")).intValue();

            Response r = ClientApp.api().send(
                    new Request("FRIEND_WISHLIST")
                            .put("userId", Session.id())
                            .put("ownerId", ownerId)
            );

            if (!r.success) {
                status.setText(r.message);
                return;
            }

            wishes.clear();
            wishes.addAll(r.rows);

            List<String> x = new ArrayList<>();
            for (Map<String,Object> w : wishes)
                x.add(w.get("custom_name") + " | Remaining: " + w.get("remaining") + " EGP | " + w.get("status"));

            wishesList.setItems(FXCollections.observableArrayList(x));
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }

    @FXML void contribute() {
        int i = wishesList.getSelectionModel().getSelectedIndex();

        if (i < 0 || i >= wishes.size()) {
            status.setText("Select an item.");
            return;
        }

        if (amount.getText().isBlank()) {
            status.setText("Enter contribution amount.");
            return;
        }

        try {
            Response r = ClientApp.api().send(
                    new Request("CONTRIBUTE")
                            .put("userId", Session.id())
                            .put("wishId", ((Number) wishes.get(i).get("id")).intValue())
                            .put("amount", amount.getText())
            );

            status.setText(r.message);

            if (r.success) {
                amount.clear();
                view();
            }
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }

    @FXML void back() throws Exception {
        SceneNav.open(
                (Stage) friendsList.getScene().getWindow(),
                "home.fxml",
                "i-Wish — Home"
        );
    }
}