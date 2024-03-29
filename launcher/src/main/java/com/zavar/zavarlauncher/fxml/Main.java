package com.zavar.zavarlauncher.fxml;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;
import com.zavar.common.finder.JavaFinder;
import com.zavar.zavarlauncher.auth.AuthConstants;
import com.zavar.zavarlauncher.Launcher;
import com.zavar.zavarlauncher.auth.http.AuthHttpHandler;
import com.zavar.zavarlauncher.auth.pojo.AuthProfileResponse;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationException;
import net.hycrafthd.minecraft_authenticator.login.AuthenticationFile;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;
import net.hycrafthd.minecraft_authenticator.login.User;
import net.hycrafthd.minecraft_authenticator.util.AuthenticationFileUtil;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main implements Initializable {
    @FXML
    private Button playButton, settingsButton, updateButton, folderButton, consoleButton;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private AnchorPane mainPane, mainMenuControlsPane, settingsFxml, mainMenuBackgroundPane;
    @FXML
    private Settings settingsFxmlController;
    @FXML
    private WebView browser;
    @FXML
    private Circle avatar;

    private Properties settings;
    private static final Logger logger = LoggerContext.getContext().getLogger(Main.class.getName());

    private final FadeTransition fadeSettingsTransition = new FadeTransition(Duration.millis(500));
    private final FadeTransition fadeMainControlsTransition = new FadeTransition(Duration.millis(500));
    private final FadeTransition fadeMainBackgroundTransition = new FadeTransition(Duration.millis(500));
    private boolean animationEnable = false;

    private final ExecutorService serverExecutorService = Executors.newSingleThreadExecutor();
    private HttpServer httpServer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        avatar.setOnMouseClicked(mouseEvent -> auth());
        fadeMainBackgroundTransition.setNode(mainMenuBackgroundPane);
        fadeMainControlsTransition.setNode(mainMenuControlsPane);
        closeMain();
        settingsFxmlController.setLocalesList(resourceBundle.getLocale(), getAvailableLocales());
        try {
            settingsFxmlController.setAvailableJavas(JavaFinder.find());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        settingsFxmlController.setOnSettingsSaved(settings -> animationEnable = Boolean.parseBoolean(settings.getProperty("general.animation")));
        backgroundImage.fitWidthProperty().bind(mainPane.widthProperty());
        backgroundImage.fitHeightProperty().bind(mainPane.heightProperty());
        fadeSettingsTransition.setNode(settingsFxml);
        closeSettings();
        settingsButton.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (fadeSettingsTransition.getToValue() == 1.0) {
                    closeSettings();
                } else {
                    openSettings();
                }
            }
        });
        consoleButton.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (Launcher.isConsoleShowing())
                    Launcher.hideConsole();
                else
                    Launcher.showConsole(resourceBundle);
            }
        });
        ScheduledThreadPoolExecutor sch = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
        sch.scheduleWithFixedDelay(new Task<Void>() {
            @Override
            protected Void call() {
                openMain();
                return null;
            }
        }, 1, 1, TimeUnit.SECONDS);
        settingsFxmlController.setBackButtonHandler(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                closeSettings();
            }
        });
    }

    public void openSettings() {
        fadeSettingsTransition.stop();
        if (animationEnable)
            fadeSettingsTransition.setFromValue(0);
        else
            fadeSettingsTransition.setFromValue(1.0);
        fadeSettingsTransition.setToValue(1.0);
        fadeSettingsTransition.setOnFinished(actionEvent -> {
        });
        settingsFxml.setDisable(false);
        fadeSettingsTransition.play();
        playButton.setDisable(true);
        consoleButton.setDisable(true);
        folderButton.setDisable(true);
        updateButton.setDisable(true);
    }

    private void closeSettings() {
        fadeSettingsTransition.stop();
        if (animationEnable)
            fadeSettingsTransition.setFromValue(1.0);
        else
            fadeSettingsTransition.setFromValue(0);
        fadeSettingsTransition.setToValue(0);
        fadeSettingsTransition.setOnFinished(actionEvent -> {
            settingsFxml.setDisable(true);
            try {
                settingsFxmlController.resetSettingsFromFile();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        fadeSettingsTransition.play();
        playButton.setDisable(false);
        folderButton.setDisable(false);
        updateButton.setDisable(false);
        consoleButton.setDisable(false);
    }

    private void openMain() {
        fadeMainBackgroundTransition.stop();
        if (animationEnable)
            fadeMainBackgroundTransition.setFromValue(0);
        else
            fadeMainBackgroundTransition.setFromValue(0.3);
        fadeMainBackgroundTransition.setToValue(0.3);
        fadeMainControlsTransition.stop();
        if (animationEnable)
            fadeMainControlsTransition.setFromValue(0);
        else
            fadeMainControlsTransition.setFromValue(1.0);
        fadeMainControlsTransition.setToValue(1.0);
        fadeMainControlsTransition.play();
        fadeMainBackgroundTransition.play();
    }

    private void closeMain() {
        fadeMainBackgroundTransition.stop();
        if (animationEnable)
            fadeMainBackgroundTransition.setFromValue(0.3);
        else
            fadeMainBackgroundTransition.setFromValue(0);
        fadeMainBackgroundTransition.setToValue(0);
        fadeMainControlsTransition.stop();
        if (animationEnable)
            fadeMainControlsTransition.setFromValue(1.0);
        else
            fadeMainControlsTransition.setFromValue(0);
        fadeMainControlsTransition.setToValue(0);
        fadeMainControlsTransition.play();
        fadeMainBackgroundTransition.play();
    }

    private Set<ResourceBundle> getAvailableLocales() {
        Set<ResourceBundle> resourceBundles = new HashSet<>();
        for (Locale locale : Locale.getAvailableLocales())
            resourceBundles.add(ResourceBundle.getBundle("com/zavar/zavarlauncher/lang/launcher", locale));
        return Collections.unmodifiableSet(resourceBundles);
    }

    public void setupSettings(Properties settings) throws FileNotFoundException {
        this.settings = settings;
        animationEnable = Boolean.parseBoolean(settings.getProperty("general.animation"));
        settingsFxmlController.setupSettings(settings);
    }

    private void auth() {
        browser.setDisable(false);
        browser.setVisible(true);
        CookieManager manager = new CookieManager();
        CookieHandler.setDefault(manager);
        manager.getCookieStore().removeAll();

        try {
            httpServer = HttpServer.create(new InetSocketAddress(8000), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        AuthHttpHandler authHttpHandler = new AuthHttpHandler();

        WebEngine engine = browser.getEngine();
        authHttpHandler.addCodeReturnListener(evt -> {
            logger.info(evt.getNewValue());
            httpServer.stop(0);
            Authenticator authenticator;
            if(Files.exists(Launcher.getAccountFile())) {
                try {
                    AuthenticationFile authenticationFile = AuthenticationFileUtil.fromCompressedInputStream(Files.newInputStream(Launcher.getAccountFile()));
                    authenticator = Authenticator.of(authenticationFile).
                            customAzureApplication(AuthConstants.CLIENT_ID, AuthConstants.REDIRECT_URI).shouldAuthenticate().build();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                authenticator = Authenticator.ofMicrosoft((String) evt.getNewValue()).
                        customAzureApplication(AuthConstants.CLIENT_ID, AuthConstants.REDIRECT_URI).shouldAuthenticate().build();
            }

            try {
                authenticator.run(); //wait for finish
                Platform.runLater(() -> {
                    browser.setDisable(true);
                    browser.setVisible(false);
                });
                Optional<User> user = authenticator.getUser();
                System.out.println(user);

                AuthenticationFile file = authenticator.getResultFile();
                file.writeCompressed(Files.newOutputStream(Launcher.getAccountFile()));

                if(user.isPresent()) {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI(AuthConstants.PROFILE_URL))
                            .header("Authorization", "Bearer " + user.get().accessToken())
                            .GET()
                            .build();
                    HttpClient httpClient = HttpClient.newHttpClient();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if(response.statusCode() == 200) {
                        System.out.println(response.body());
                        JSONObject jsonObject = new JSONObject(response.body());
                        System.out.println(jsonObject.getJSONArray("skins").getJSONObject(0).get("url"));
                    } else {

                    }
                }
            } catch (AuthenticationException | URISyntaxException | InterruptedException | IOException e) {
                logger.error(e.getMessage());
                throw new RuntimeException(e);
            }
        });
        httpServer.createContext("/", authHttpHandler);
        httpServer.setExecutor(serverExecutorService);
        httpServer.start();
        logger.info("Auth server started");

        engine.load(String.valueOf(Authenticator.microsoftLogin(AuthConstants.CLIENT_ID, AuthConstants.REDIRECT_URI)));
        logger.info("Opening Microsoft auth page...");
    }
}
