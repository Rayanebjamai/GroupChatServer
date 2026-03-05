package org.example.javafxsocketserver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                TcpServer.class.getResource("tcpserver-view.fxml")
        );

        Scene scene = new Scene(loader.load());
        Controller controller = loader.getController();

        stage.setTitle("TCP Chat Server");
        stage.setScene(scene);
        stage.show();

        Thread serverThread = new Thread(() -> {

            ServerConfig cfg = ServerConfig.load();
            String bindAddr = cfg.bindAddress();
            int port = cfg.port();

            try (ServerSocket serverSocket = new ServerSocket()) {

                // Bind using config (IP + port)
                serverSocket.bind(new InetSocketAddress(InetAddress.getByName(bindAddr), port));

                controller.log("Server started on " + bindAddr + ":" + port);
                controller.log("Waiting for clients...");

                while (true) {

                    Socket clientSocket = serverSocket.accept();

                    ClientHandler handler =
                            new ClientHandler(clientSocket, controller);

                    controller.addClient(handler);

                    handler.start();
                }

            } catch (Exception e) {
                controller.log("Server failed to start: " + e.getMessage());
                e.printStackTrace();
            }

        });

        serverThread.setDaemon(true);
        serverThread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}