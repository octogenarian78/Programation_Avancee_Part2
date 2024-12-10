module com.example.pi_assignment102 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.pi_assignment102 to javafx.fxml;
    exports com.example.pi_assignment102;
}