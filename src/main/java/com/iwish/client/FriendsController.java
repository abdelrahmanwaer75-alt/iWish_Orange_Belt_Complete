package com.iwish.client;

import com.iwish.common.Request;
import com.iwish.common.Response;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FriendsController {
    private TextField search;
    private ListView<String> list;
    private Label status;
    private final List<Map<String,Object>> users = new ArrayList<>();
    private final List<Map<String,Object>> friends = new ArrayList<>();

    public Node view(javafx.stage.Stage stage) {
        search = new TextField();
        search.setPromptText("Search users by name...");
        Button find = new Button("Search");
        find.setOnAction(e -> find());

        HBox searchBox = new HBox(10, search, find);
        HBox.setHgrow(search, Priority.ALWAYS);

        list = new ListView<>();
        status = new Label();

        Button add = new Button("Add Friend");
        Button accept = new Button("Accept");
        Button decline = new Button("Decline");
        Button remove = new Button("Remove");
        Button refresh = new Button("Refresh");
        Button back = new Button("Back");

        add.setOnAction(e -> add());
        accept.setOnAction(e -> accept());
        decline.setOnAction(e -> decline());
        remove.setOnAction(e -> remove());
        refresh.setOnAction(e -> loadFriends());
        back.setOnAction(e -> SceneNav.open(stage, "home", "i-Wish — Home"));

        HBox actions = new HBox(8, add, accept, decline, remove, refresh);
        VBox center = new VBox(12, searchBox, list, actions, status);
        VBox.setVgrow(list, Priority.ALWAYS);
        center.setPadding(new Insets(20));

        BorderPane root = new BorderPane(center);
        root.setTop(back);
        BorderPane.setMargin(back, new Insets(10));
        loadFriends();
        return root;
    }

    private void find() {
        try {
            Response r = ClientApp.api().send(new Request("USERS")
                    .put("userId", Session.id())
                    .put("search", search.getText()));

            if (!r.success) {
                status.setText(r.message);
                return;
            }

            users.clear();
            users.addAll(r.rows);
            List<String> names = new ArrayList<>();
            for (Map<String,Object> u : users)
                names.add(u.get("name") + " — " + u.get("email"));

            list.getItems().setAll(names);
            status.setText("Select a person then press Add Friend.");
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }

    private void add() {
        int i = list.getSelectionModel().getSelectedIndex();
        if (i < 0 || i >= users.size()) {
            status.setText("Select a user.");
            return;
        }
        try {
            Response r = ClientApp.api().send(new Request("ADD_FRIEND")
                    .put("userId", Session.id())
                    .put("otherId", ((Number) users.get(i).get("id")).intValue()));
            status.setText(r.message);
            loadFriends();
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }

    private void loadFriends() {
        if (list == null) return;
        try {
            Response r = ClientApp.api().send(new Request("FRIENDS").put("userId", Session.id()));
            if (!r.success) {
                status.setText(r.message);
                return;
            }
            friends.clear();
            friends.addAll(r.rows);
            List<String> names = new ArrayList<>();
            for (Map<String,Object> f : friends)
                names.add(f.get("name") + " — " + f.get("status") + " (" + f.get("direction") + ")");
            list.getItems().setAll(names);
            status.setText("Your friends and requests.");
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }

    private void accept() { act("ACCEPTED"); }
    private void decline() { act("DECLINED"); }

    private void act(String decision) {
        int i = list.getSelectionModel().getSelectedIndex();
        if (i < 0 || i >= friends.size()) {
            status.setText("Select a received request.");
            return;
        }
        Map<String,Object> f = friends.get(i);
        if (!"PENDING".equals(f.get("status")) || !"RECEIVED".equals(f.get("direction"))) {
            status.setText("Select a received pending request.");
            return;
        }
        try {
            Response r = ClientApp.api().send(new Request("FRIEND_DECISION")
                    .put("userId", Session.id())
                    .put("otherId", ((Number) f.get("id")).intValue())
                    .put("status", decision));
            status.setText(r.message);
            loadFriends();
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }

    private void remove() {
        int i = list.getSelectionModel().getSelectedIndex();
        if (i < 0 || i >= friends.size()) {
            status.setText("Select a friend.");
            return;
        }
        try {
            Response r = ClientApp.api().send(new Request("REMOVE_FRIEND")
                    .put("userId", Session.id())
                    .put("otherId", ((Number) friends.get(i).get("id")).intValue()));
            status.setText(r.message);
            loadFriends();
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }
}
