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

public class NotificationsController {
    private ListView<String> list;
    private Label status;
    private final List<Map<String,Object>> data = new ArrayList<>();

    public Node view(javafx.stage.Stage stage) {
        list = new ListView<>();
        status = new Label();

        Button markRead = new Button("Mark as read");
        Button refresh = new Button("Refresh");
        Button back = new Button("Back");

        markRead.setOnAction(e -> markRead());
        refresh.setOnAction(e -> load());
        back.setOnAction(e -> SceneNav.open(stage, "home", "i-Wish — Home"));

        HBox actions = new HBox(8, markRead, refresh);
        VBox center = new VBox(12, list, actions, status);
        VBox.setVgrow(list, Priority.ALWAYS);
        center.setPadding(new Insets(20));

        BorderPane root = new BorderPane(center);
        root.setTop(back);
        BorderPane.setMargin(back, new Insets(10));
        load();
        return root;
    }

    private void load() {
        try {
            Response r = ClientApp.api().send(new Request("NOTIFICATIONS").put("userId", Session.id()));
            data.clear();
            data.addAll(r.rows);
            List<String> x = new ArrayList<>();
            for (Map<String,Object> n : data)
                x.add((Boolean.TRUE.equals(n.get("is_read")) ? "✓ " : "● ") + n.get("message") + " [" + n.get("type") + "]");
            list.getItems().setAll(x);
        } catch (Exception e) {
            Ui.error(e.getMessage());
        }
    }

    private void markRead() {
        int i = list.getSelectionModel().getSelectedIndex();
        if (i < 0) return;
        try {
            Response r = ClientApp.api().send(new Request("MARK_READ")
                    .put("userId", Session.id())
                    .put("notificationId", ((Number) data.get(i).get("id")).intValue()));
            status.setText(r.message);
            load();
        } catch (Exception e) {
            Ui.error(e.getMessage());
        }
    }
}
