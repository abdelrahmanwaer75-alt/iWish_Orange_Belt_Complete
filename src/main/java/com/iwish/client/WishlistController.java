package com.iwish.client;

import com.iwish.common.Request;
import com.iwish.common.Response;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WishlistController {
    private ListView<String> list;
    private ComboBox<String> catalog;
    private TextField name, price, description;
    private Label status;
    private final List<Map<String,Object>> wishes = new ArrayList<>();
    private final List<Map<String,Object>> catalogItems = new ArrayList<>();

    public Node view(javafx.stage.Stage stage) {
        list = new ListView<>();
        catalog = new ComboBox<>();
        name = new TextField();
        price = new TextField();
        description = new TextField();
        status = new Label();

        catalog.setPromptText("Choose catalog item");
        name.setPromptText("Item name");
        price.setPromptText("Price");
        description.setPromptText("Description");

        Button add = new Button("Add Wish");
        Button update = new Button("Update");
        Button delete = new Button("Delete");
        Button refresh = new Button("Refresh");
        Button back = new Button("Back");

        catalog.setOnAction(e -> chooseCatalog());
        list.setOnMouseClicked(e -> select());
        add.setOnAction(e -> add());
        update.setOnAction(e -> update());
        delete.setOnAction(e -> delete());
        refresh.setOnAction(e -> load());
        back.setOnAction(e -> SceneNav.open(stage, "home", "i-Wish — Home"));

        VBox form = new VBox(8, catalog, name, price, description);
        HBox actions = new HBox(8, add, update, delete, refresh);

        VBox center = new VBox(12, form, list, actions, status);
        VBox.setVgrow(list, Priority.ALWAYS);
        center.setPadding(new Insets(20));

        BorderPane root = new BorderPane(center);
        root.setTop(back);
        BorderPane.setMargin(back, new Insets(10));

        loadCatalog();
        load();
        return root;
    }

    private void loadCatalog() {
        try {
            Response r = ClientApp.api().send(new Request("ITEMS"));
            catalogItems.clear();
            catalogItems.addAll(r.rows);
            List<String> labels = new ArrayList<>();
            for (Map<String,Object> item : catalogItems)
                labels.add(item.get("name") + " — " + item.get("default_price") + " EGP");
            catalog.getItems().setAll(labels);
        } catch (Exception e) {
            status.setText("Catalog unavailable; add a custom item instead.");
        }
    }

    private void chooseCatalog() {
        int i = catalog.getSelectionModel().getSelectedIndex();
        if (i >= 0) {
            Map<String,Object> item = catalogItems.get(i);
            name.setText(String.valueOf(item.get("name")));
            price.setText(String.valueOf(item.get("default_price")));
            description.setText(String.valueOf(item.get("description")));
        }
    }

    private void load() {
        try {
            Response r = ClientApp.api().send(new Request("MY_WISHLIST").put("userId", Session.id()));
            if (!r.success) {
                status.setText(r.message);
                return;
            }
            wishes.clear();
            wishes.addAll(r.rows);
            List<String> labels = new ArrayList<>();
            for (Map<String,Object> w : wishes)
                labels.add(w.get("custom_name") + " | " + w.get("collected_amount") + "/" + w.get("price") + " EGP | " + w.get("status"));
            list.getItems().setAll(labels);
        } catch (Exception e) {
            status.setText("Could not load your wish list.");
        }
    }

    private void add() {
        BigDecimal parsed;
        try {
            parsed = new BigDecimal(price.getText().trim());
        } catch (Exception e) {
            status.setText("Enter a valid price.");
            return;
        }
        if (name.getText().isBlank() || parsed.signum() <= 0) {
            status.setText("Enter a name and a positive price.");
            return;
        }
        try {
            Request req = new Request("ADD_WISH")
                    .put("userId", Session.id())
                    .put("name", name.getText())
                    .put("description", description.getText())
                    .put("price", price.getText());
            int i = catalog.getSelectionModel().getSelectedIndex();
            if (i >= 0) req.put("itemId", ((Number) catalogItems.get(i).get("id")).intValue());
            Response r = ClientApp.api().send(req);
            status.setText(r.message);
            if (r.success) {
                clear();
                catalog.getSelectionModel().clearSelection();
                load();
            }
        } catch (Exception e) {
            status.setText("Could not add the item.");
        }
    }

    private void update() {
        int i = list.getSelectionModel().getSelectedIndex();
        if (i < 0) {
            status.setText("Select an item.");
            return;
        }
        try {
            Response r = ClientApp.api().send(new Request("UPDATE_WISH")
                    .put("userId", Session.id())
                    .put("wishId", ((Number) wishes.get(i).get("id")).intValue())
                    .put("name", name.getText())
                    .put("description", description.getText())
                    .put("price", price.getText()));
            status.setText(r.message);
            if (r.success) load();
        } catch (Exception e) {
            status.setText("Could not update the item.");
        }
    }

    private void delete() {
        int i = list.getSelectionModel().getSelectedIndex();
        if (i < 0) {
            status.setText("Select an item.");
            return;
        }
        try {
            Response r = ClientApp.api().send(new Request("DELETE_WISH")
                    .put("userId", Session.id())
                    .put("wishId", ((Number) wishes.get(i).get("id")).intValue()));
            status.setText(r.message);
            if (r.success) load();
        } catch (Exception e) {
            status.setText("Could not delete the item.");
        }
    }

    private void select() {
        int i = list.getSelectionModel().getSelectedIndex();
        if (i >= 0) {
            Map<String,Object> w = wishes.get(i);
            name.setText(String.valueOf(w.get("custom_name")));
            price.setText(String.valueOf(w.get("price")));
            description.setText(String.valueOf(w.get("description")));
        }
    }

    private void clear() {
        name.clear();
        price.clear();
        description.clear();
    }
}
