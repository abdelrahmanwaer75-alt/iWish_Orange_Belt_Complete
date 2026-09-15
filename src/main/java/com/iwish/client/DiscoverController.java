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

public class DiscoverController {
    private ListView<String> friendsList;
    private ListView<String> wishesList;
    private TextField amount;
    private Label status;
    private final List<Map<String,Object>> friends = new ArrayList<>();
    private final List<Map<String,Object>> wishes = new ArrayList<>();

    public Node view(javafx.stage.Stage stage) {
        friendsList = new ListView<>();
        wishesList = new ListView<>();
        amount = new TextField();
        amount.setPromptText("Contribution amount");
        status = new Label();

        Button view = new Button("View Wishlist");
        Button contribute = new Button("Contribute");
        Button refresh = new Button("Refresh");
        Button back = new Button("Back");

        view.setOnAction(e -> viewWishlist());
        contribute.setOnAction(e -> contribute());
        refresh.setOnAction(e -> loadFriends());
        back.setOnAction(e -> SceneNav.open(stage, "home", "i-Wish — Home"));

        HBox actions = new HBox(8, view, amount, contribute, refresh);
        VBox center = new VBox(12,
                new Label("Friends"),
                friendsList,
                actions,
                new Label("Selected Friend's Wishlist"),
                wishesList,
                status);
        VBox.setVgrow(friendsList, Priority.ALWAYS);
        VBox.setVgrow(wishesList, Priority.ALWAYS);
        center.setPadding(new Insets(20));

        BorderPane root = new BorderPane(center);
        root.setTop(back);
        BorderPane.setMargin(back, new Insets(10));
        loadFriends();
        return root;
    }

    private void loadFriends() {
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
            friendsList.getItems().setAll(x);
            wishesList.getItems().clear();
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }

    private void viewWishlist() {
        int i = friendsList.getSelectionModel().getSelectedIndex();
        if (i < 0 || i >= friends.size()) {
            status.setText("Select a friend.");
            return;
        }
        try {
            int ownerId = ((Number) friends.get(i).get("id")).intValue();
            Response r = ClientApp.api().send(new Request("FRIEND_WISHLIST")
                    .put("userId", Session.id())
                    .put("ownerId", ownerId));
            if (!r.success) {
                status.setText(r.message);
                return;
            }
            wishes.clear();
            wishes.addAll(r.rows);
            List<String> x = new ArrayList<>();
            for (Map<String,Object> w : wishes)
                x.add(w.get("custom_name") + " | Remaining: " + w.get("remaining") + " EGP | " + w.get("status"));
            wishesList.getItems().setAll(x);
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }

    private void contribute() {
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
            Response r = ClientApp.api().send(new Request("CONTRIBUTE")
                    .put("userId", Session.id())
                    .put("wishId", ((Number) wishes.get(i).get("id")).intValue())
                    .put("amount", amount.getText()));
            status.setText(r.message);
            if (r.success) {
                amount.clear();
                viewWishlist();
            }
        } catch (Exception e) {
            status.setText(e.getMessage());
        }
    }
}
