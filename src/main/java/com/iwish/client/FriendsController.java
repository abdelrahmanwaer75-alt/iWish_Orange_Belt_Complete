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

public class FriendsController {
    @FXML TextField search;
    @FXML ListView<String> list;
    @FXML Label status;

    private final List<Map<String,Object>> users = new ArrayList<>();
    private final List<Map<String,Object>> friends = new ArrayList<>();

    @FXML public void initialize() {
        loadFriends();
    }

    @FXML void find() {
        try {
            Response r = ClientApp.api().send(
                    new Request("USERS")
                            .put("userId", Session.id())
                            .put("search", search.getText())
            );

            if (!r.success) {
                status.setText(r.message);
                return;
            }

            users.clear();
            users.addAll(r.rows);

            List<String> names = new ArrayList<>();
            for (Map<String,Object> u : users)
                names.add(u.get("name") + " — " + u.get("email"));

            list.setItems(FXCollections.observableArrayList(names));
            status.setText("Select a person then press Add Friend.");
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }

    @FXML void add() {
        int i = list.getSelectionModel().getSelectedIndex();

        if (i < 0 || i >= users.size()) {
            status.setText("Select a user.");
            return;
        }

        try {
            Response r = ClientApp.api().send(
                    new Request("ADD_FRIEND")
                            .put("userId", Session.id())
                            .put("otherId", ((Number) users.get(i).get("id")).intValue())
            );

            status.setText(r.message);
            loadFriends();
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }

    @FXML void loadFriends() {
        try {
            Response r = ClientApp.api().send(
                    new Request("FRIENDS").put("userId", Session.id())
            );

            if (!r.success) {
                status.setText(r.message);
                return;
            }

            friends.clear();
            friends.addAll(r.rows);

            List<String> names = new ArrayList<>();
            for (Map<String,Object> f : friends)
                names.add(f.get("name") + " — " + f.get("status") + " (" + f.get("direction") + ")");

            list.setItems(FXCollections.observableArrayList(names));
            status.setText("Your friends and requests.");
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }

    @FXML void accept() {
        act("ACCEPTED");
    }

    @FXML void decline() {
        act("DECLINED");
    }

    private void act(String decision) {
        int i = list.getSelectionModel().getSelectedIndex();

        if (i < 0 || i >= friends.size()) {
            status.setText("Select a received request.");
            return;
        }

        Map<String,Object> f = friends.get(i);

        if (!"PENDING".equals(f.get("status")) ||
            !"RECEIVED".equals(f.get("direction"))) {
            status.setText("Select a received pending request.");
            return;
        }

        try {
            Response r = ClientApp.api().send(
                    new Request("FRIEND_DECISION")
                            .put("userId", Session.id())
                            .put("otherId", ((Number) f.get("id")).intValue())
                            .put("status", decision)
            );

            status.setText(r.message);
            loadFriends();
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }

    @FXML void remove() {
        int i = list.getSelectionModel().getSelectedIndex();

        if (i < 0 || i >= friends.size()) {
            status.setText("Select a friend.");
            return;
        }

        try {
            Response r = ClientApp.api().send(
                    new Request("REMOVE_FRIEND")
                            .put("userId", Session.id())
                            .put("otherId", ((Number) friends.get(i).get("id")).intValue())
            );

            status.setText(r.message);
            loadFriends();
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }

    @FXML void back() throws Exception {
        SceneNav.open(
                (Stage) list.getScene().getWindow(),
                "home.fxml",
                "i-Wish — Home"
        );
    }
}