/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at May 21, 2014.
 */
package com.bitcoin.view;

import com.aquafx_project.AquaFx;
import com.bitcoin.controller.MainViewController;
import com.bitcoin.core.BitcoinExcavator;
import com.bitcoin.util.BitcoinOptionsBuilder;
import com.bitcoin.util.GuiUtils;
import com.bitcoin.util.TextFieldValidator;
import com.bitcoin.util.UTF8Control;
import com.google.common.base.Throwables;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wallet.utils.FileOperations;

import java.io.IOException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * This class represents the main view for setting all properties to mining bitcoins.
 * Also this class initialize view for proper display.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class MainView extends Application {

    /**
     * Application name
     */
    public static String APP_NAME = "bitcoin-excavator";

    /** Instance of wallet view. **/
    public  static wallet.view.MainView walletView = new wallet.view.MainView();

    /** Instance of main view what mean the bitcoin-excavator view **/
    public static MainView instance;

    /** Instance of Bitcoin excavator **/
    public static BitcoinExcavator excavator;

    /** **/
    private StackPane uiStack;

    /** **/
    private Pane mainUI;

    /** The main stage of application **/
    private Stage mainWindow;

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory.getLogger(MainView.class);

    /**
     * This method starts and initialize instance of the window which present excavator view.
     *
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage mainWindow) throws Exception {
        instance = this;

        // Show the crash dialog for any exceptions that we don't handle and that hit the main loop.
        GuiUtils.handleCrashesOnThisThread();
        setMainWindow(mainWindow);
        FileOperations.initializeFolders();

        try {
            init(mainWindow);
        } catch (Throwable t) {
            if (Throwables.getRootCause(t) instanceof MissingResourceException) {
                // Nicer message for the case where the block store file is locked.
                GuiUtils.informationalAlert("Wrong properties",
                        "Something goes wrong: \n" + t.toString());
                throw t;
            } else {
                GuiUtils.informationalAlert("Already running",
                        "This application is already running and cannot be started twice."
                                + "\nOr something goes wrong: \n" + t
                                .toString());
                throw t;
            }
        }
    }

    /**
     * THis method stops working wallet and excavator if are running.
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        if(walletView.isRunning()) {
            walletView.stop();
        }

        if(excavator.getRunning()) {
            excavator.stop();
        }

        // Forcibly terminate the JVM because Orchid likes to spew non-daemon threads everywhere.
        Runtime.getRuntime().exit(0);
    }

    /**
     * Inits stage for application.
     *
     * @param mainWindow
     * @throws IOException
     */
    private void init(Stage mainWindow) throws IOException {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            AquaFx.style();
        }

        String fxmlFile = "excavator-view.fxml";
        UTF8Control locale = new UTF8Control(); //Locale.getDefault();

        // Load the GUI. The Controller class will be automagically created and wired up.
        URL location = getClass().getResource("/fxml/" + fxmlFile);
        FXMLLoader loader = new FXMLLoader(location);//,
        loader.setResources(ResourceBundle.getBundle("fxml/excavator", locale));
        mainUI = loader.load();

        MainViewController controller = loader.getController();

        // Configure the window with a StackPane so we can overlay things on top of the main UI.
        uiStack = new StackPane(mainUI);
        mainWindow.setResizable(false);
        mainWindow.setTitle(APP_NAME);
        final Scene scene = new Scene(uiStack);

        // Add CSS that we need.
        TextFieldValidator.configureScene(scene);
        mainWindow.setScene(scene);

        mainWindow.show();
    }

    /**
     * Main method for all application.
     * If args are putted terminal mode starts, if doesn't GUI mode.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length > 0) { // TODO  don't forget replace "==" to ">"
            log.info("Terminal mode is running.");
            excavator = new BitcoinExcavator(BitcoinOptionsBuilder.terminalOptions(args));
            excavator.execute();
        } else {
            launch(args);
        }

    }

    public Stage getMainWindow() {
        return mainWindow;
    }

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }
}
