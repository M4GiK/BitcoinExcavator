/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Aug 29, 2014.
 */
package wallet.controller;

import com.google.bitcoin.core.*;
import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.params.RegTestParams;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wallet.controls.BitcoinNameWalletValidator;
import wallet.controls.ClickableBitcoinAddress;
import wallet.utils.FileOperations;
import wallet.view.MainView;

import java.io.File;
import java.util.Date;

import static wallet.view.MainView.bitcoinWallets;


/**
 * This class represents operations for file.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class AddWalletController {
    public Button addBtn;
    public Button cancelBtn;
    public TextField name;
    public Label titleLabel;
    public ProgressIndicator progress;

    public MainView.OverlayUI overlayUi;

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory.getLogger(MainView.class);

    // Called by FXMLLoader
    public void initialize() {
        progress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        progress.setVisible(false);
        new BitcoinNameWalletValidator(name, addBtn);
    }

    public void cancel(ActionEvent event) {
        overlayUi.done();
    }

    public void add(ActionEvent event) {
        hideItems();
        progress.setVisible(true);

        WalletAppKit bitcoinWallet = new WalletAppKit(MainView.params, new File(FileOperations.APP_PATH + "/wallets/."), FileOperations.removeFileExtension(name.getText()));
        bitcoinWallets.add(bitcoinWallet);

        if (MainView.params == RegTestParams.get()) {
            // You should run a regtest mode bitcoind locally.
            bitcoinWallet.connectToLocalHost();
        } else if (MainView.params == MainNetParams.get()) {
            // Checkpoints are block headers that ship inside our app: for a new user,
            // we pick the last header
            // in the checkpoints file and then download the rest from the network.
            // It makes things much faster. Checkpoint files are made using the BuildCheckpoints
            // tool and usually we have to download the last months worth or more (takes a few seconds).
            bitcoinWallet.setCheckpoints(getClass().getResourceAsStream("/wallet/checkpoints"));
        }
        //Platform.runLater(overlayUi::done);
        bitcoinWallet.setDownloadListener(progressBarUpdater())
                .setBlockingStartup(false)
                .setUserAgent(MainView.APP_NAME, "1.0");
        bitcoinWallet.startAsync();
        bitcoinWallet.awaitRunning();

        // Don't make the user wait for confirmations for now, as the intention
        // is they're sending it their own money!
        bitcoinWallet.wallet().allowSpendingUnconfirmedTransactions();
        bitcoinWallet.peerGroup().setMaxConnections(11);
        log.info("Address wallet: " + bitcoinWallet.wallet().currentReceiveAddress().toString());
        FileOperations.updateProperty(name.getText());
    }

    private void hideItems() {
        addBtn.setDisable(true);
        name.setDisable(true);
        addBtn.setVisible(false);
        cancelBtn.setVisible(false);
        name.setVisible(false);
    }

    public ProgressBarUpdater progressBarUpdater() {
        return new ProgressBarUpdater();
    }

    public class ProgressBarUpdater extends DownloadListener {
        @Override
        protected void progress(double pct, int blocksSoFar, Date date) {
            super.progress(pct, blocksSoFar, date);
            Platform.runLater(() -> progress.setProgress(pct / 100.0));
        }

        @Override
        protected void doneDownload() {
            super.doneDownload();
            Platform.runLater(AddWalletController.this::readyToGoAnimation);
        }
    }

    private void readyToGoAnimation() {
        overlayUi.done();
    }
}
