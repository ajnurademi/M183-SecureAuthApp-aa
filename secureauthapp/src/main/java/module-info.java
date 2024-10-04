module ost {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.codec;
    requires java.sql;

    opens ost to javafx.fxml;
    exports ost;
}