module org.example.javafxsocketserver {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.javafxsocketserver to javafx.fxml;
    exports org.example.javafxsocketserver;
}