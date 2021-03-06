/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Jun 02, 2014.
 */
package wallet.view;

import com.aquafx_project.AquaFx;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.params.RegTestParams;
import com.google.bitcoin.params.TestNet3Params;
import com.google.bitcoin.store.BlockStoreException;
import com.google.bitcoin.utils.BriefLogFormatter;
import com.google.bitcoin.utils.Threading;
import com.google.common.base.Throwables;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wallet.controller.AddWalletController;
import wallet.controller.DeleteWalletController;
import wallet.controller.MainViewController;
import wallet.controller.SendMoneyController;
import wallet.controls.ClickableBitcoinAddress;
import wallet.utils.BitcoinWallet;
import wallet.utils.FileOperations;
import wallet.utils.GuiUtils;
import wallet.utils.TextFieldValidator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static wallet.utils.GuiUtils.*;

/**
 * This class represents the main view for setting all properties to bitcoins wallet.
 * Also this class initialize view for proper display. This class is based on bitcoinj example.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class MainView extends Application {

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory.getLogger(MainView.class);

    /**
     * Application name *
     */
    public static String APP_NAME = "bitcoin-excavator-wallet";

    // Page for testNet3 with virtual BitCoins -> http://faucet.xeno-genesis.com
    public static NetworkParameters params = TestNet3Params.testNet3(); // MainNetParams.get();
    public static List<BitcoinWallet> bitcoinWallets;
    public static MainView instance;

    private StackPane uiStack;
    private Pane mainUI;
    private boolean isRunning = false;

    /**
     * This method starts and initialize instance of the window which present wallet view.
     *
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage mainWindow) throws Exception {
        instance = this;
        // Show the crash dialog for any exceptions that we don't handle and that hit the main loop.
        GuiUtils.handleCrashesOnThisThread();
        FileOperations.initializeFolders();
        try {
            init(mainWindow);
        } catch (Throwable t) {
            // Nicer message for the case where the block store file is locked.
            if (Throwables.getRootCause(t) instanceof BlockStoreException) {
                GuiUtils.informationalAlert("Already running", "This application is already running and cannot be started twice.");
            } else {
                throw t;
            }
        }
    }

    private void init(Stage mainWindow) throws IOException {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            AquaFx.style();
        }

        // Load the GUI. The MainViewController class will be automagically created and wired up.
        URL location = getClass().getResource("/wallet/wallet-view.fxml");
        FXMLLoader loader = new FXMLLoader(location);
        mainUI = loader.load();
        MainViewController controller = loader.getController();

        // Configure the window with a StackPane so we can overlay things on top of the main UI.
        uiStack = new StackPane(mainUI);
        mainWindow.setTitle(APP_NAME);
        final Scene scene = new Scene(uiStack);

        // Add CSS that we need.
        TextFieldValidator.configureScene(scene);
        mainWindow.setScene(scene);

        // Make log output concise.
        BriefLogFormatter.init();

        // Tell bitcoinj to run event handlers on the JavaFX UI thread. This keeps things
        // simple and means we cannot forget to switch threads when adding event handlers.
        // Unfortunately, the DownloadListener we give to the app kit is currently an
        // exception and runs on a library thread. It'll get fixed in a future version.
        Threading.USER_THREAD = Platform::runLater;

        // Create the app kit. It won't do any heavyweight initialization until after we start it.
        if (!FileOperations.isPropertiesExisting(FileOperations.PROPERTIES)) {
            FileOperations.saveProperties(FileOperations.collectWallets("/"), FileOperations.PROPERTIES);
        }

        bitcoinWallets = FileOperations.readProperties(FileOperations.PROPERTIES);//new WalletAppKit(params, new File("."), APP_NAME);

        if(bitcoinWallets.size() == 0) {
            bitcoinWallets.add(new BitcoinWallet(params, new File(FileOperations.APP_PATH + "/wallets/."), APP_NAME));
            FileOperations.updateProperty(APP_NAME);
        }

        if (params == RegTestParams.get()) {
            // You should run a regtest mode bitcoind locally.
            for (WalletAppKit wallet : bitcoinWallets) {
                wallet.connectToLocalHost();
            }
        } else if (params == MainNetParams.get()) {
            // Checkpoints are block headers that ship inside our app: for a new user,
            // we pick the last header
            // in the checkpoints file and then download the rest from the network.
            // It makes things much faster. Checkpoint files are made using the BuildCheckpoints
            // tool and usually we have to download the last months worth or more (takes a few seconds).
            for (WalletAppKit wallet : bitcoinWallets) {
                wallet.setCheckpoints(getClass().getResourceAsStream("/wallet/checkpoints"));
            }
            // As an example!
            //bitcoin.useTor();
        }

        // Now configure and start the appkit. This will take a second or two
        // - we could show a temporary splash screen
        // or progress widget to keep the user engaged whilst we initialise, but we don't.
        for (WalletAppKit wallet : bitcoinWallets) {
            wallet.setDownloadListener(controller.progressBarUpdater())
                    .setBlockingStartup(false)
                    .setUserAgent(APP_NAME, "1.0");
            wallet.startAsync();
            wallet.awaitRunning();

            // Don't make the user wait for confirmations for now, as the intention
            // is they're sending it their own money!
            wallet.wallet().allowSpendingUnconfirmedTransactions();
            wallet.peerGroup().setMaxConnections(11);
            log.info("Address wallet: " + wallet.wallet().currentReceiveAddress().toString());
        }
        controller.onBitcoinSetup();
        mainWindow.show();
        isRunning = true;
    }

    /**
     * Loads the FXML file with the given name, blurs out the main UI and puts this one on top.
     *
     * @param name Controler name.
     * @param bitcoinWallet wallet with data.
     * @param connectionsListView
     * @param clickableBitcoinAddress
     * @return pair.
     */
    public <T> OverlayUI<T> overlayUIDeleteWallet(String name, BitcoinWallet bitcoinWallet, VBox connectionsListView, ClickableBitcoinAddress clickableBitcoinAddress) {
        try {
            checkGuiThread();
            // Load the UI from disk.
            URL location = getClass().getResource(name);
            FXMLLoader loader = new FXMLLoader(location);
            Pane ui = loader.load();
            DeleteWalletController controller = loader.getController();
            controller.initData(bitcoinWallet, connectionsListView, clickableBitcoinAddress);
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

    /**
     * Loads the FXML file with the given name, blurs out the main UI and puts this one on top.
     *
     * @param name Controler name.
     * @param bitcoinWallet wallet with data.
     * @param <T>
     * @return pair.
     */
    public <T> OverlayUI<T> overlayUISendMoney(String name, BitcoinWallet bitcoinWallet) {
        try {
            checkGuiThread();
            // Load the UI from disk.
            URL location = getClass().getResource(name);
            FXMLLoader loader = new FXMLLoader(location);
            Pane ui = loader.load();
            SendMoneyController controller = loader.getController();
            controller.initData(bitcoinWallet);
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

    /**
     * Loads the FXML file with the given name, blurs out the main UI and puts this one on top.
     *
     * @param name Controler name.
     * @param connectionsListView node with list view.
     * @param <T>
     * @return pair.
     */
    public <T> OverlayUI<T> overlayUIAddWallet(String name, VBox connectionsListView) {
        try {
            checkGuiThread();
            // Load the UI from disk.
            URL location = getClass().getResource(name);
            FXMLLoader loader = new FXMLLoader(location);
            Pane ui = loader.load();
            AddWalletController controller = loader.getController();
            controller.initData(connectionsListView);
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

    /**
     * Checks if the instance of view is running
     * @return true if application is running, false if application is not.
     */
    public boolean isRunning() {
        return this.isRunning;
    }

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

    public <T> OverlayUI<T> overlayUI(Node node, T controller) {
        checkGuiThread();
        OverlayUI<T> pair = new OverlayUI<T>(node, controller);
        // Auto-magically set the overlayUi member, if it's there.
        try {
            controller.getClass().getDeclaredField("overlayUi").set(controller, pair);
        } catch (IllegalAccessException | NoSuchFieldException ignored) {
        }
        pair.show();
        return pair;
    }

    /**
     * Loads the FXML file with the given name, blurs out the main UI and puts this one on top.
     */
    public <T> OverlayUI<T> overlayUI(String name) {
        try {
            checkGuiThread();
            // Load the UI from disk.
            URL location = getClass().getResource(name);
            FXMLLoader loader = new FXMLLoader(location);
            Pane ui = loader.load();
            T controller = loader.getController();
            OverlayUI<T> pair = new OverlayUI<T>(ui, controller);
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

    @Override
    public void stop() throws Exception {
        for (WalletAppKit wallet : bitcoinWallets) {
            wallet.stopAsync();
            wallet.awaitTerminated();
        }

        isRunning = false;

        // Forcibly terminate the JVM because Orchid likes to spew non-daemon threads everywhere.
        Runtime.getRuntime().exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
