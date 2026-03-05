package org.example.javafxsocketserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket socket;
    private final Controller controller;

    private DataInputStream dis;
    private DataOutputStream dos;

    private String username;
    private boolean readOnly = false; // enforced by server

    public ClientHandler(Socket socket, Controller controller) {
        this.socket = socket;
        this.controller = controller;
    }

    public String getUsername() {
        return username;
    }

    public void send(String msg) {
        try {
            if (dos != null) {
                dos.writeUTF(msg);
                dos.flush();
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            // 1) Ask for username
            send("Enter your username:");

            // 2) Read username
            String u = dis.readUTF();
            u = (u == null) ? "" : u.trim();

            // 3) Enforce requirements
            if (u.isEmpty()) {
                readOnly = true;
                username = "READ-ONLY MODE";   // exactly the requirement wording
                send("[SERVER] READ-ONLY MODE: you cannot send messages.");
            } else {
                username = u;
                send("[SERVER] Welcome " + username + "!");
            }

            // 4) Add user to server UI list
            controller.addUser(username);
            controller.log("Welcome " + username);

            // 5) Notify everyone
            controller.broadcast(controller.time() + " [SERVER] " + username + " joined the chat");

            // 6) Main loop
            while (true) {
                String msg = dis.readUTF();
                if (msg == null) continue;
                msg = msg.trim();

                // Disconnect commands
                if (msg.equalsIgnoreCase("bye") || msg.equalsIgnoreCase("end")) {
                    send("[SERVER] Disconnecting...");
                    break;
                }

                // allUsers allowed even in read-only
                if (msg.equalsIgnoreCase("allUsers")) {
                    send("Active users:\n" + controller.getUsersList());
                    continue;
                }

                // READ-ONLY enforced here (blocks sending)
                if (readOnly) {
                    send("[SERVER] READ-ONLY MODE: message blocked.");
                    continue; // DO NOT broadcast
                }

                // Normal user: broadcast formatted message
                String formatted = controller.time() + " " + username + " : " + msg;
                controller.broadcast(formatted);
            }

        } catch (Exception e) {
            controller.log("Client error/disconnected: " + (username == null ? "(unknown)" : username));
        } finally {
            // remove client + notify
            controller.removeClient(this);
            controller.broadcast(controller.time() + " [SERVER] " + username + " left the chat");
            controller.log(username + " disconnected");

            try { socket.close(); } catch (Exception ignored) {}
        }
    }
}