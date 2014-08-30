/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Aug 30, 2014.
 */
package wallet.controller;

import com.google.bitcoin.core.*;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import wallet.controls.BitcoinAddressValidator;
import wallet.utils.BitcoinWallet;
import wallet.view.MainView;

import static wallet.utils.GuiUtils.crashAlert;
import static wallet.utils.GuiUtils.informationalAlert;

/**
 * This controller is responsible for money (bitcoin) operations.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class SendMoneyController {
    public Button sendBtn;
    public Button cancelBtn;
    public TextField address;
    public Label titleLabel;
    public Slider slider;
    public Label satoshi;

    public MainView.OverlayUI overlayUi;

    private Wallet.SendResult sendResult;

    private BitcoinWallet bitcoinWallet;

    // Called by FXMLLoader
    public void initialize() {
        new BitcoinAddressValidator(MainView.params, address, sendBtn);
    }

    /**
     * Initialize data.
     *
     * @param bitcoinWallet bitcoin wallet.
     */
    public void initData(BitcoinWallet bitcoinWallet) {
        this.bitcoinWallet = bitcoinWallet;
        Long satoshiAmount = bitcoinWallet.wallet().getBalance(Wallet.BalanceType.ESTIMATED).longValue();
        if (satoshiAmount == 0 || satoshiAmount == null) {
            satoshi.setText("0");
            slider.setDisable(true);
            slider.setBlockIncrement(10.0);
        } else {
            slider.setMax(satoshiAmount);
            slider.valueProperty().addListener((observable, oldValue, newValue)
                    -> satoshi.setText(String.format("%d", newValue.intValue())));
        }
    }

    public void cancel(ActionEvent event) {
        overlayUi.done();
    }

    public void send(ActionEvent event) {
        try {
            Address destination = new Address(MainView.params, address.getText());
            System.out.println("LLLLLLLLLLOOLLLL " + String.valueOf(Long.getLong(satoshi.getText()) / 100000000.0));
            Wallet.SendRequest req = Wallet.SendRequest.to(destination,
                    Coin.parseCoin(String.valueOf(Long.getLong(satoshi.getText()) / 100000000.0)));
            sendResult = bitcoinWallet.wallet().sendCoins(req);
            Futures.addCallback(sendResult.broadcastComplete, new FutureCallback<Transaction>() {
                @Override
                public void onSuccess(Transaction result) {
                    Platform.runLater(overlayUi::done);
                }

                @Override
                public void onFailure(Throwable t) {
                    // We died trying to empty the wallet.
                    crashAlert(t);
                }
            });
            sendResult.tx.getConfidence().addEventListener((tx, reason) -> {
                if (reason == TransactionConfidence.Listener.ChangeReason.SEEN_PEERS)
                    updateTitleForBroadcast();
            });
            sendBtn.setDisable(true);
            address.setDisable(true);
            updateTitleForBroadcast();
        } catch (AddressFormatException e) {
            // Cannot happen because we already validated it when the text field changed.
            throw new RuntimeException(e);
        } catch (InsufficientMoneyException e) {
            informationalAlert("Could not empty the wallet",
                    "You may have too little money left in the wallet to make a transaction.");
            overlayUi.done();
        }
    }

    private void updateTitleForBroadcast() {
        final int peers = sendResult.tx.getConfidence().numBroadcastPeers();
        titleLabel.setText(String.format("Broadcasting ... seen by %d peers", peers));
    }
}
