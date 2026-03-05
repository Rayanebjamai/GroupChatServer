module org.example.javafxsocketclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.javafxsocketclient to javafx.fxml;
    exports org.example.javafxsocketclient;
}