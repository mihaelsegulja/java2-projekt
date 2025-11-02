module hr.algebra.uno {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires static lombok;
    requires java.desktop;
    requires org.slf4j;

    exports hr.algebra.uno;

    opens hr.algebra.uno.controller to javafx.fxml;

    opens hr.algebra.uno.model to javafx.base;
}