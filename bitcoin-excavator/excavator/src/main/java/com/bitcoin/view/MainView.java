/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at May 21, 2014.
 */
package com.bitcoin.view;

import java.util.Locale;
import java.util.ResourceBundle;

import com.bitcoin.controller.MainViewController;
import com.bitcoin.util.GuiUtils;

import com.bitcoin.util.TextFieldValidator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.SceneBuilder;
import javafx.stage.Stage;
import com.aquafx_project.AquaFx;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO COMMENTS MISSING!
 * 
 * @author m4gik <michal.szczygiel@wp.pl>
 * 
 */
public class MainView extends Application {

    /** **/
    public static String APP_NAME = "bitcoin-excavator";

    /** **/
    public static MainView instance;

    /** **/
    private StackPane uiStack;

    /** **/
    private Pane mainUI;

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory.getLogger(MainView.class);

    /**
     * 
     * TODO Comments missing. This method overrides an existing method.
     * 
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage mainWindow) throws Exception {
        instance = this;

        // Show the crash dialog for any exceptions that we don't handle and that hit the main loop.
        GuiUtils.handleCrashesOnThisThread();

        try {
            init(mainWindow);
        } catch (Throwable t) {
            // Nicer message for the case where the block store file is locked.
                GuiUtils.informationalAlert("Already running", "This application is already running and cannot be started twice."
                        + "\nOr something goes wrong: \n" + t.toString());
                throw t;
            }
        }

    /**
     *
     * @param mainWindow
     * @throws IOException
     */
    private void init(Stage mainWindow) throws IOException {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            AquaFx.style();
        }

        String fxmlFile = "main-view.fxml";
        Locale locale = Locale.getDefault();

        // Load the GUI. The Controller class will be automagically created and wired up.
        URL location = getClass().getResource("/fxml/" + fxmlFile);
        FXMLLoader loader = FXMLLoader.load(location, ResourceBundle.getBundle("/fxml/main-view", locale));
        mainUI = loader.load();

        MainViewController controller = loader.getController();

        // Configure the window with a StackPane so we can overlay things on top of the main UI.
        uiStack = new StackPane(mainUI);
        mainWindow.setTitle(APP_NAME);
        mainWindow.setWidth(650);
        mainWindow.setHeight(500);
        final Scene scene = new Scene(uiStack);
        TextFieldValidator.configureScene(scene);   // Add CSS that we need.
        mainWindow.setScene(scene);

        mainWindow.show();
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }

}
