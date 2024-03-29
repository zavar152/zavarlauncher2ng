module com.zavar.zavarlauncher {
    requires com.zavar.launcher.common;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires javafx.graphics;
    requires org.update4j;
    requires org.apache.commons.net;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.apache.logging.log4j.core;
    requires semver4j;
    requires minecraft.authenticator;
    requires jdk.httpserver;
    requires java.desktop;
    requires java.net.http;
    requires com.google.gson;
    requires org.json;

    opens com.zavar.zavarlauncher to javafx.fxml;
    exports com.zavar.zavarlauncher;
    exports com.zavar.zavarlauncher.update.handler;
    opens com.zavar.zavarlauncher.update.handler to javafx.fxml;
    opens com.zavar.zavarlauncher.lang;
    opens com.zavar.zavarlauncher.css;
    opens com.zavar.zavarlauncher.fxml;
    opens com.zavar.zavarlauncher.img.graphics;
    opens com.zavar.zavarlauncher.img.background;
    opens com.zavar.zavarlauncher.img.icons;
    exports com.zavar.zavarlauncher.auth;
    opens com.zavar.zavarlauncher.auth to javafx.fxml;
}