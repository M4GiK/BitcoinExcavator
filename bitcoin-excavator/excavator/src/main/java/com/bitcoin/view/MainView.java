/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at May 21, 2014.
 */
package com.bitcoin.view;

import com.aquafx_project.AquaFx;
import com.bitcoin.controller.EditCredentialsController;
import com.bitcoin.controller.MainViewController;
import com.bitcoin.core.BitcoinExcavator;
import com.bitcoin.util.*;
import com.google.common.base.Throwables;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wallet.controller.SendMoneyController;
import wallet.utils.BitcoinWallet;
import wallet.utils.FileOperations;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static wallet.utils.GuiUtils.*;
import static wallet.utils.GuiUtils.blurIn;


/**
 * This class represents the main view for setting all properties to mining bitcoins.
 * Also this class initialize view for proper display.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class MainView extends Application {

    public class OverlayUI<T> {
        public Node ui;
        public T controller;

        public OverlayUI(Node ui, T controller) {
            this.ui = ui;
            this.controller = controller;
        }

        public void show() {
            blurOut(mainUI);
            uiStack.getChildren().add(ui);
            fadeIn(ui);
        }

        public void done() {
            checkGuiThread();
            fadeOutAndRemove(ui, uiStack);
            blurIn(mainUI);
            this.ui = null;
            this.controller = null;
        }

    }

    /**
     * Application name
     */
    public static String APP_NAME = "bitcoin-excavator";

    /**
     * Instance of wallet view. *
     */
    public static wallet.view.MainView walletView = new wallet.view.MainView();

    /**
     * Instance of main view what mean the bitcoin-excavator view *
     */
    public static MainView instance;

    /**
     * Instance of Bitcoin excavator *
     */
    public static BitcoinExcavator excavator;

    /** **/
    private StackPane uiStack;

    /** **/
    private Pane mainUI;

    /**
     * The main stage of application *
     */
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
     *
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        if (walletView != null && walletView.isRunning()) {
            walletView.stop();
        }

        if (excavator != null && excavator.getRunning()) {
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
        if (args.length == 0) { // TODO  don't forget replace "==" to ">"
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

    /**
     * Loads the FXML file with the given name, blurs out the main UI and puts this one on top.
     *
     * @param name            Controler name.
     * @param credentials     The credentials to manipulate.
     * @param resources       The resource bundle to set.
     * @param credentialsView The list view to update.
     * @param id              The id of current credentials position.
     * @return pair.
     */
    public <T> OverlayUI<T> overlayUIEditCredentials(String name, Credential credentials, ResourceBundle resources,
                                                     ListView credentialsView, Integer id) {
        try {
            checkGuiThread();
            // Load the UI from disk.
            URL location = getClass().getResource(name);
            FXMLLoader loader = new FXMLLoader(location);
            loader.setResources(resources);
            Pane ui = loader.load();
            EditCredentialsController controller = loader.getController();
            controller.initData(credentials, credentialsView, id);
            OverlayUI<T> pair = new OverlayUI<T>(ui, (T) controller);
            // Auto-magically set the overlayUi member, if it's there.
            try {
                controller.getClass().getDeclaredField("overlayUi").set(controller, pair);
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
            }
            pair.show();
            return pair;
        } catch (IOException e) {
            throw new RuntimeException(e);  // Can't happen.
        }
    }
}
