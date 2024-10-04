module ost {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.codec;
    requires java.sql;
    requires jbcrypt;  // Füge diese Zeile hinzu, um die BCrypt-Bibliothek zu verwenden

    opens ost to javafx.fxml;
    exports ost;
}
