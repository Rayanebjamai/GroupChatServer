package org.example.javafxsocketclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientListener extends Thread {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dout;

    private final Controller controller;
    private final String username;
    private final boolean readOnly;

    public ClientListener(String host, int port, Controller controller, String username, boolean readOnly) {
        this.controller = controller;
        this.username = username;
        this.readOnly = readOnly;

        try {
            socket = new Socket(host, port);
            dis = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());

            //  connected at TCP level
            controller.setOnline(true);

        } catch (Exception e) {
            e.printStackTrace();
            controller.addMessage("[CLIENT] Failed to connect to server.");
            controller.setOnline(false);
        }
    }

    public void sendMessage(String msg) {
        if (readOnly) return;

        try {
            if (dout != null) {
                dout.writeUTF(msg);
                dout.flush();
            }
        } catch (Exception e) {
            // If sending fails, connection is probably gone
            controller.addMessage("[CLIENT] Connection lost while sending.");
            controller.setOnline(false);
        }
    }

    @Override
    public void run() {
        try {
            if (dis == null || dout == null) return;

            // Server prompt
            String prompt = dis.readUTF();
            controller.addMessage("[SERVER] " + prompt);

            // Send username
            dout.writeUTF(username);
            dout.flush();

            // fully online (handshake done)
            controller.setOnline(true);

            // Listen for chat messages
            while (true) {
                String msg = dis.readUTF();
                controller.addMessage(msg);

                if (msg.equals("[SERVER] DISCONNECT")) {
                    break;
                }
            }

        } catch (Exception e) {
            controller.addMessage("[CLIENT] Connection to server lost.");
            controller.setOnline(false);

        } finally {
            try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        }
    }
}