module com.example.distributedmc {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.distributedmc to javafx.fxml;
    exports com.example.distributedmc;
}