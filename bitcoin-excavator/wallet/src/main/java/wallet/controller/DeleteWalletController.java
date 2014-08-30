/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Aug 30, 2014.
 */
package wallet.controller;

import com.google.bitcoin.core.DownloadListener;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.store.UnreadableWalletException;
import com.google.bitcoin.wallet.WalletFiles;
import com.google.common.util.concurrent.Service;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wallet.controls.ClickableBitcoinAddress;
import wallet.utils.BitcoinWallet;
import wallet.utils.FileOperations;
import wallet.view.MainView;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Executor;

import static wallet.view.MainView.bitcoinWallets;

/**
 * This controller represents operations for delete wallet.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class DeleteWalletController {

    public Button deleteBtn;
    public Button cancelBtn;
    public Label titleLabel;
    public ProgressIndicator progress;

    public MainView.OverlayUI overlayUi;

    private BitcoinWallet bitcoinWallet;
    private VBox connectionsListView;
    private ClickableBitcoinAddress clickableBitcoinAddress;

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory.getLogger(MainView.class);

    // Called by FXMLLoader
    public void initialize() {
        progress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        progress.setVisible(false);
    }

    public void initData(BitcoinWallet bitcoinWallet, VBox connectionsListView, ClickableBitcoinAddress clickableBitcoinAddress) {
        this.bitcoinWallet = bitcoinWallet;
        this.connectionsListView = connectionsListView;
        this.clickableBitcoinAddress = clickableBitcoinAddress;
    }

    public void cancel(ActionEvent event) {
        overlayUi.done();
    }

    public void delete(ActionEvent event) {
        hideItems();
        progress.setVisible(true);
        bitcoinWallet.stopAsync();
        bitcoinWallet.awaitTerminated();
        bitcoinWallets.remove(bitcoinWallet);
        FileOperations.deleteWallet(bitcoinWallet.getFilePrefix());
        connectionsListView.getChildren().remove(clickableBitcoinAddress);
        Platform.runLater(overlayUi::done);
    }

    private void hideItems() {
        deleteBtn.setDisable(true);
        deleteBtn.setVisible(false);
        cancelBtn.setVisible(false);
    }
}
