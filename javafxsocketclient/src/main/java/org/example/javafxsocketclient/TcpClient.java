package org.example.javafxsocketclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;

public class TcpClient extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader =
                new FXMLLoader(TcpClient.class.getResource("tcpclient-view.fxml"));

        Scene scene = new Scene(loader.load());
        Controller controller = loader.getController();

        stage.setTitle("TCP Chat Client");
        stage.setScene(scene);
        stage.show();

        // Ask username once (NO loop)
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Username");
        dialog.setHeaderText("Enter your username");
        dialog.setContentText("Username:");

        Optional<String> result = dialog.showAndWait();

        // Cancel / close => exit app
        if (result.isEmpty()) {
            Platform.exit();
            return;
        }

        String raw = result.get();                // OK clicked
        boolean readOnly = (raw == null || raw.isBlank());

        String username = readOnly ? "READ-ONLY MODE" : raw.trim();

        // Load HOST + PORT from app.properties at runtime
        ClientConfig cfg = ClientConfig.load();

        ClientListener listener =
                new ClientListener(cfg.host(), cfg.port(), controller, username, readOnly);

        controller.setClientListener(listener);
        controller.setReadOnly(readOnly);

        listener.setDaemon(true);
        listener.start();
    }

    public static void main(String[] args) {
        launch();
    }
}