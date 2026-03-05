package org.example.javafxsocketserver;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class ServerConfig {

    private static final String FILE_NAME = "app.properties";
    private static final String DEFAULT_BIND = "0.0.0.0";
    private static final int DEFAULT_PORT = 3333;

    private final String bindAddress;
    private final int port;
    private final Path loadedFrom; // for debug

    private ServerConfig(String bindAddress, int port, Path loadedFrom) {
        this.bindAddress = bindAddress;
        this.port = port;
        this.loadedFrom = loadedFrom;
    }

    public String bindAddress() { return bindAddress; }
    public int port() { return port; }
    public Path loadedFrom() { return loadedFrom; }

    public static ServerConfig load() {
        Properties props = new Properties();
        Path used = null;

        // 0) Optional override: java -Dapp.config=/path/to/app.properties ...
        String override = System.getProperty("app.config");
        if (override != null && !override.isBlank()) {
            Path p = Paths.get(override).toAbsolutePath();
            if (Files.exists(p)) {
                used = p;
                loadInto(props, p);
            }
        }

        // 1) Working directory: ./app.properties
        if (used == null) {
            Path p = Paths.get(FILE_NAME).toAbsolutePath();
            if (Files.exists(p)) {
                used = p;
                loadInto(props, p);
            }
        }

        // 2) Next to the running code (works for jlink + jar)
        if (used == null) {
            Path p = guessAppRoot().resolve(FILE_NAME).toAbsolutePath();
            if (Files.exists(p)) {
                used = p;
                loadInto(props, p);
            }
        }

        // Parse values
        String bind = props.getProperty("BIND_ADDRESS", DEFAULT_BIND).trim();
        String portStr = props.getProperty("PORT", String.valueOf(DEFAULT_PORT)).trim();

        int port = DEFAULT_PORT;
        try { port = Integer.parseInt(portStr); } catch (Exception ignored) {}
        if (port < 1 || port > 65535) port = DEFAULT_PORT;

        try { InetAddress.getByName(bind); } catch (Exception e) { bind = DEFAULT_BIND; }

        // Debug print so you KNOW what happened
        System.out.println("[CONFIG] user.dir = " + Paths.get("").toAbsolutePath());
        System.out.println("[CONFIG] Loaded app.properties from: " + (used == null ? "(NOT FOUND - using defaults)" : used));

        return new ServerConfig(bind, port, used);
    }

    private static void loadInto(Properties props, Path file) {
        try (InputStream in = new FileInputStream(file.toFile())) {
            props.load(in);
        } catch (Exception ignored) {}
    }

    /**
     * Tries to locate the app root folder:
     * - If running from jlink: .../tcp-server/lib/app.jar -> returns .../tcp-server
     * - If running from jar: .../target/tcp-server.jar -> returns .../target
     */
    private static Path guessAppRoot() {
        try {
            Path code = Paths.get(ServerConfig.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).toAbsolutePath();

            // If code is a JAR file inside lib/, go up to the app root.
            // jlink typical: <appRoot>/lib/app.jar
            if (Files.isRegularFile(code) && code.getFileName().toString().endsWith(".jar")) {
                Path parent = code.getParent();              // maybe lib/ or target/
                if (parent != null && parent.getFileName().toString().equalsIgnoreCase("lib")) {
                    return parent.getParent();               // appRoot
                }
                return parent;                               // jar folder
            }

            // If code is a directory (rare), use it
            if (Files.isDirectory(code)) return code;

        } catch (Exception ignored) {}

        // fallback to working dir
        return Paths.get("").toAbsolutePath();
    }
}