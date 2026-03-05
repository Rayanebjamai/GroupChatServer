package org.example.javafxsocketclient;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class ClientConfig {

    private static final String FILE_NAME = "app.properties";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 3333;

    private final String host;
    private final int port;
    private final Path loadedFrom; // debug

    private ClientConfig(String host, int port, Path loadedFrom) {
        this.host = host;
        this.port = port;
        this.loadedFrom = loadedFrom;
    }

    public String host() { return host; }
    public int port() { return port; }
    public Path loadedFrom() { return loadedFrom; }

    public static ClientConfig load() {
        Properties props = new Properties();
        Path used = null;

        // 0) Optional override: java -Dapp.config=/full/path/app.properties ...
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

        String host = props.getProperty("HOST", DEFAULT_HOST).trim();
        String portStr = props.getProperty("PORT", String.valueOf(DEFAULT_PORT)).trim();

        int port = DEFAULT_PORT;
        try { port = Integer.parseInt(portStr); } catch (Exception ignored) {}
        if (port < 1 || port > 65535) port = DEFAULT_PORT;

        if (host.isBlank()) host = DEFAULT_HOST;

        // Debug so you can SEE it works
        System.out.println("[CLIENT CONFIG] user.dir = " + Paths.get("").toAbsolutePath());
        System.out.println("[CLIENT CONFIG] Loaded app.properties from: " + (used == null ? "(NOT FOUND - using defaults)" : used));
        System.out.println("[CLIENT CONFIG] HOST=" + host + " PORT=" + port);

        return new ClientConfig(host, port, used);
    }

    private static void loadInto(Properties props, Path file) {
        try (InputStream in = new FileInputStream(file.toFile())) {
            props.load(in);
        } catch (Exception ignored) {}
    }

    /**
     * Finds the app root folder:
     * - jlink: <appRoot>/lib/app.jar  => returns <appRoot>
     * - jar:   <someFolder>/tcp-client.jar => returns <someFolder>
     */
    private static Path guessAppRoot() {
        try {
            Path code = Paths.get(ClientConfig.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).toAbsolutePath();

            if (Files.isRegularFile(code) && code.getFileName().toString().endsWith(".jar")) {
                Path parent = code.getParent(); // lib/ or target/
                if (parent != null && parent.getFileName().toString().equalsIgnoreCase("lib")) {
                    return parent.getParent();   // appRoot for jlink
                }
                return parent;                   // jar folder
            }

            if (Files.isDirectory(code)) return code;

        } catch (Exception ignored) {}

        return Paths.get("").toAbsolutePath();
    }
}