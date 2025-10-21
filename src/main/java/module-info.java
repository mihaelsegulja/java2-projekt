module hr.algebra.uno {
    requires javafx.controls;
    requires javafx.fxml;


    opens hr.algebra.uno to javafx.fxml;
    exports hr.algebra.uno;
}