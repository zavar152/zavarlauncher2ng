module com.zavar.bootstrapper {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires javafx.graphics;
    requires org.update4j;
    requires org.apache.commons.net;
    requires org.apache.commons.io;
    requires org.json;
    requires TrayNotification.master;
    requires com.zavar.launcher.common;
    requires zip4j;
    requires org.apache.commons.lang3;
    requires semver4j;
    requires org.jsoup;

    opens com.zavar.bootstrapper to javafx.fxml;
    opens com.zavar.bootstrapper.controller to javafx.fxml;
    exports com.zavar.bootstrapper;
    exports com.zavar.bootstrapper.util;
    opens com.zavar.bootstrapper.util to javafx.fxml;
}