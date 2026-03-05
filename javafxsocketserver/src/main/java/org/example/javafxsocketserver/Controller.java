package org.example.javafxsocketserver;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private ListView<String> serverLog;

    @FXML
    private ListView<String> usersList;

    // Keep it private
    private final ArrayList<ClientHandler> clients = new ArrayList<>();

    public synchronized void addClient(ClientHandler client) {
        clients.add(client);
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);

        Platform.runLater(() ->
                usersList.getItems().remove(client.getUsername()));
    }

    // Broadcast to all connected clients
    public void broadcast(String message) {
        List<ClientHandler> snapshot = getClientsSnapshot();
        for (ClientHandler client : snapshot) {
            client.send(message);
        }
        log(message);
    }

    // Return a safe copy so ClientHandler can list users without accessing private field
    public synchronized List<ClientHandler> getClientsSnapshot() {
        return new ArrayList<>(clients);
    }

    public String getUsersList() {
        StringBuilder users = new StringBuilder();
        for (ClientHandler c : getClientsSnapshot()) {
            users.append(c.getUsername()).append("\n");
        }
        return users.toString();
    }

    public void addUser(String username) {
        Platform.runLater(() -> usersList.getItems().add(username));
    }

    public void log(String message) {
        Platform.runLater(() -> {
            serverLog.getItems().add(message);
            serverLog.scrollTo(serverLog.getItems().size() - 1);
        });
    }

    public String time() {
        return "[" + LocalTime.now().withNano(0) + "]";
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        log("Server UI initialized.");
    }
}