package org.example.javafxsocketclient;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Controller {

    @FXML private TextField serverText;
    @FXML private ListView<String> myListView;
    @FXML private Button sendButton;

    // Status indicator
    @FXML private Label statusLabel;
    @FXML private Circle statusCircle;

    private ClientListener clientListener;
    private boolean readOnly = false;

    private boolean online = true;

    // username -> assigned color (consistent)
    private final Map<String, Color> userColors = new HashMap<>();
    private final Random rng = new Random();

    @FXML
    public void initialize() {
        // Default: online
        setOnline(true);

        myListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item);

                if (item.startsWith("[SERVER]") || item.startsWith("[CLIENT]")) {
                    setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
                    return;
                }

                String user = extractUsername(item);
                if (user == null) {
                    setStyle("-fx-background-color: #666666; -fx-text-fill: white;");
                    return;
                }

                Color c = userColors.computeIfAbsent(user, u -> randomSoftColor());

                int r = (int) Math.round(c.getRed() * 255);
                int g = (int) Math.round(c.getGreen() * 255);
                int b = (int) Math.round(c.getBlue() * 255);

                String textColor = (luminance(c) > 0.6) ? "#111111" : "white";

                setStyle(String.format(
                        "-fx-background-color: rgb(%d,%d,%d); -fx-text-fill: %s;",
                        r, g, b, textColor
                ));
            }
        });
    }

    public void addMessage(String msg) {
        Platform.runLater(() -> {
            myListView.getItems().add(msg);
            myListView.scrollTo(myListView.getItems().size() - 1);
        });
    }

    public void setClientListener(ClientListener listener) {
        this.clientListener = listener;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        refreshInputState();

        if (readOnly) {
            addMessage("[CLIENT] READ-ONLY MODE enabled (you cannot send messages).");
        }
    }

    // Called by ClientListener when connection state changes
    public void setOnline(boolean isOnline) {
        this.online = isOnline;

        Platform.runLater(() -> {
            if (statusLabel != null) statusLabel.setText(isOnline ? "Online" : "Offline");
            if (statusCircle != null) statusCircle.setFill(isOnline ? Color.web("#2ecc71") : Color.web("#e74c3c"));
        });

        // If offline => disable input & send
        refreshInputState();
    }

    private void refreshInputState() {
        Platform.runLater(() -> {
            boolean disable = !online || readOnly;
            if (serverText != null) serverText.setDisable(disable);
            if (sendButton != null) sendButton.setDisable(disable);
        });
    }

    @FXML
    public void onSend(ActionEvent event) {
        if (!online) return;
        if (readOnly) return;

        String msg = serverText.getText();
        if (msg == null || msg.isBlank()) return;

        if (clientListener != null) {
            clientListener.sendMessage(msg);
        }

        serverText.clear();
    }

    // -------- helpers --------

    private String extractUsername(String line) {
        String s = line;
        if (s.startsWith("[") && s.contains("]")) {
            int idx = s.indexOf("]");
            if (idx + 1 < s.length()) s = s.substring(idx + 1).trim();
        }

        int colon = s.indexOf(":");
        if (colon <= 0) return null;

        String left = s.substring(0, colon).trim();
        return left.isEmpty() ? null : left;
    }

    private Color randomSoftColor() {
        double r = 0.5 + rng.nextDouble() * 0.4;
        double g = 0.5 + rng.nextDouble() * 0.4;
        double b = 0.5 + rng.nextDouble() * 0.4;
        return new Color(clamp(r), clamp(g), clamp(b), 1.0);
    }

    private double luminance(Color c) {
        return 0.2126 * c.getRed() + 0.7152 * c.getGreen() + 0.0722 * c.getBlue();
    }

    private double clamp(double x) {
        return Math.max(0.0, Math.min(1.0, x));
    }
}