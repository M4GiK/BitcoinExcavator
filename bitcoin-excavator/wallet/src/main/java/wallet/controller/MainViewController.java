/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Aug 29, 2014.
 */
package wallet.controller;

import com.google.bitcoin.core.*;
import com.google.bitcoin.kits.WalletAppKit;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import wallet.controls.ClickableBitcoinAddress;
import wallet.view.MainView;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static wallet.view.MainView.bitcoinWallets;
import static wallet.utils.GuiUtils.checkGuiThread;

/**
 * Gets created auto-magically by FXMLLoader via reflection. The widget fields are set to the GUI controls they're named
 * after. This class handles all the updates and event handling for the main UI.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class MainViewController implements Initializable {
    public ProgressBar syncProgress;
    public VBox syncBox;
    public HBox controlsBox;
    public Label balance;
    public Button addWallet;
    public VBox connectionsListView;
    private List<ClickableBitcoinAddress> clickableBitcoinAddressList;

    // Called by FXMLLoader.
    public void initialize(URL location, ResourceBundle resources) {
        syncProgress.setProgress(-1);
        connectionsListView.setOpacity(0.0);
    }

    public void onBitcoinSetup() {
        clickableBitcoinAddressList = new ArrayList<ClickableBitcoinAddress>();

        for (int i = 0; i < bitcoinWallets.size(); i++) {
            bitcoinWallets.get(i).wallet().addEventListener(new BalanceUpdater());
            ClickableBitcoinAddress node = new ClickableBitcoinAddress();
            node.setAddress(bitcoinWallets.get(i).wallet().currentReceiveKey().toAddress(MainView.params).toString(),
                    bitcoinWallets.get(i), connectionsListView);
            connectionsListView.getChildren().add(node);
            clickableBitcoinAddressList.add(node);
        }

        refreshBalanceLabel();
    }

    public void newWallet(MouseEvent event) {
        // Hide this UI and show the add wallet UI.
        MainView.instance.overlayUIAddWallet("/wallet/add-wallet.fxml", connectionsListView);
    }

    public class ProgressBarUpdater extends DownloadListener {
        @Override
        protected void progress(double pct, int blocksSoFar, Date date) {
            super.progress(pct, blocksSoFar, date);
            Platform.runLater(() -> syncProgress.setProgress(pct / 100.0));
        }

        @Override
        protected void doneDownload() {
            super.doneDownload();
            Platform.runLater(MainViewController.this::readyToGoAnimation);
        }
    }

    public void readyToGoAnimation() {
        // Sync progress bar slides out ...
        TranslateTransition leave = new TranslateTransition(
                Duration.millis(600), syncBox);
        leave.setByY(80.0);
        // Buttons slide in and clickable address appears simultaneously.
        TranslateTransition arrive = new TranslateTransition(
                Duration.millis(600), controlsBox);
        arrive.setToY(0.0);
        FadeTransition reveal = new FadeTransition(Duration.millis(500),
                connectionsListView);
        reveal.setToValue(1.0);
        ParallelTransition group = new ParallelTransition(arrive, reveal);
        // Slide out happens then slide in/fade happens.
        SequentialTransition both = new SequentialTransition(leave, group);
        both.setCycleCount(1);
        both.setInterpolator(Interpolator.EASE_BOTH);
        both.play();
    }

    public ProgressBarUpdater progressBarUpdater() {
        return new ProgressBarUpdater();
    }

    public class BalanceUpdater extends AbstractWalletEventListener {
        @Override
        public void onWalletChanged(Wallet wallet) {
            checkGuiThread();
            refreshBalanceLabel();
        }
    }

    public void refreshBalanceLabel() {
        Long amount = 0L;
        for (WalletAppKit bitcoinWallet : bitcoinWallets) {
            amount += bitcoinWallet.wallet().getBalance(Wallet.BalanceType.ESTIMATED).longValue();
        }
        balance.setText(Double.toString(amount / 100000000.0));

        for (ClickableBitcoinAddress node : clickableBitcoinAddressList) {
            node.refreshBalance();
        }
    }
}
