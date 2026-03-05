package org.example.javafxsocketserver;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // Change HelloApplication to TcpServer
        Application.launch(TcpServer.class, args);
    }
}