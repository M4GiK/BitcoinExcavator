/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 23, 2014.
 */

package com.bitcoin.controller;

import com.bitcoin.core.BitcoinExcavator;
import com.bitcoin.core.BitcoinExcavatorFatalException;
import com.bitcoin.util.BitcoinOptions;
import com.bitcoin.util.BitcoinOptionsBuilder;
import com.bitcoin.util.GuiUtils;
import com.bitcoin.util.serialization.ObjectSerializationFactory;
import com.bitcoin.util.serialization.SerializationFactory;
import com.bitcoin.util.serialization.json.JsonSerializationFactory;
import com.bitcoin.view.MainView;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wallet.utils.FileOperations;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import static com.bitcoin.view.MainView.excavator;

/**
 * View controller for main tab of excavator application.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class MainTabController implements Initializable {

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory.getLogger(MainTabController.class);

    public ImageView bitcoinExcavator;
    public ImageView bitcoinExcavatorStop;
    public ImageView bitcoinWallet;
    public Label excavatorSpeedLabel;
    public Label excavatorBlocksLabel;
    public Label excavatorErrorsLabel;
    public Label excavatorHashesLabel;
    public Label excavatorSpeed;
    public Label excavatorAvgBasis;
    public Label excavatorBlocks;
    public Label excavatorErrors;
    public Label excavatorHashes;
    public Pane excavatorPane;

    private BitcoinOptions options;
    private Boolean excavatorStopClicked = false;
    private ResourceBundle resources = null;
    private Stage walletStage;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

        bitcoinExcavator.setOnMouseEntered(mouseOverForExcavator);
        bitcoinExcavator.setOnMouseExited(mouseExitFromExcavator);
        bitcoinExcavator.setOnMouseClicked(mouseClickedOnExcavator);
        bitcoinExcavatorStop.setOnMouseEntered(mouseOverForExcavatorStop);
        bitcoinExcavatorStop.setOnMouseExited(mouseExitFromExcavatorStop);
        bitcoinExcavatorStop.setOnMouseClicked(mouseClickedOnExcavatorStop);
        bitcoinWallet.setOnMouseEntered(mouseOverForBitcoinWallet);
        bitcoinWallet.setOnMouseExited(mouseExitFromBitcoinWallet);
        bitcoinWallet.setOnMouseClicked(mouseClickedOnBitcoinWallet);

        Tooltip.install(bitcoinExcavator, new Tooltip(resources.getString("toolTipExcavator")));
        Tooltip.install(bitcoinWallet, new Tooltip(resources.getString("toolTipWallet")));

        //setupPageController.getModel();
    }

    private EventHandler<MouseEvent> mouseOverForExcavator = mouseEvent -> {
        ScaleTransition scale = new ScaleTransition(Duration.millis(600), bitcoinExcavator);
        scale.setToX(.85f);
        scale.setToY(.85f);
        scale.setAutoReverse(true);
        scale.play();
    };

    private EventHandler<MouseEvent> mouseExitFromExcavator = mouseEvent -> {
        ScaleTransition rescale = new ScaleTransition(Duration.millis(600), bitcoinExcavator);
        rescale.setToX(1.0f);
        rescale.setToY(1.0f);
        rescale.play();
    };

    private EventHandler<MouseEvent> mouseClickedOnExcavator = mouseEvent -> {
        bitcoinExcavator.setDisable(true);
        FadeTransition fadeTransitionOff = new FadeTransition(Duration.millis(600), bitcoinExcavator);
        fadeTransitionOff.setToValue(0.0f);
        fadeTransitionOff.play();

        excavatorPane.setDisable(false);
        FadeTransition fadeTransitionOn = new FadeTransition(Duration.millis(600), excavatorPane);
        fadeTransitionOn.setToValue(1.0f);
        fadeTransitionOn.play();

        if (excavator == null || excavatorStopClicked) {
            Platform.runLater(MainTabController.this::startExcavator);
        }
    };

    private void startExcavator() {
        try {
            excavatorStopClicked = false;
            excavator = new BitcoinExcavator(options);
            Thread excavatorThread = new Thread(excavator);
            excavatorThread.start();
            setExcavatorValues(excavator);
        } catch (BitcoinExcavatorFatalException e) {
            GuiUtils.crashAlert(e);
        }
    }

    private void setExcavatorValues(BitcoinExcavator excavator) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(MainTabController.this::refreshExcavatorValues);
                if (excavatorStopClicked == true) {
                    timer.cancel();
                    timer.purge();
                }
            }
        }, 0, 5000);
    }

    private void refreshExcavatorValues() {
        excavatorSpeed.setText(String.format("%.1f Mhash", excavator.getSpeed()));
        excavatorAvgBasis.setText(String.format("%.1f fps", excavator.getAvgBasis()));
        excavatorBlocks.setText(excavator.getBlocks().toString());
        excavatorErrors.setText(excavator.getHwErrors().toString());
        excavatorHashes.setText(excavator.getHashCount().toString());
    }

    private EventHandler<MouseEvent> mouseOverForBitcoinWallet = mouseEvent -> {
        ScaleTransition scale = new ScaleTransition(Duration.millis(600), bitcoinWallet);
        scale.setToX(.85f);
        scale.setToY(.85f);
        scale.setAutoReverse(true);
        scale.play();
    };

    private EventHandler<MouseEvent> mouseExitFromBitcoinWallet = mouseEvent -> {
        ScaleTransition rescale = new ScaleTransition(Duration.millis(600), bitcoinWallet);
        rescale.setToX(1.0f);
        rescale.setToY(1.0f);
        rescale.play();
    };

    private EventHandler<MouseEvent> mouseClickedOnBitcoinWallet = mouseEvent -> {
        if (walletStage == null) {
            walletStage = new Stage();
            walletStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    walletStage.hide();
                }
            });
            try {
                MainView.walletView.start(walletStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            walletStage.show();
        }
    };

    private EventHandler<MouseEvent> mouseOverForExcavatorStop = mouseEvent -> {
        ScaleTransition scale = new ScaleTransition(Duration.millis(600), bitcoinExcavatorStop);
        scale.setToX(.85f);
        scale.setToY(.85f);
        scale.setAutoReverse(true);
        scale.play();
    };

    private EventHandler<MouseEvent> mouseExitFromExcavatorStop = mouseEvent -> {
        ScaleTransition rescale = new ScaleTransition(Duration.millis(600), bitcoinExcavatorStop);
        rescale.setToX(1.0f);
        rescale.setToY(1.0f);
        rescale.play();
    };

    private EventHandler<MouseEvent> mouseClickedOnExcavatorStop = mouseEvent -> {
        excavatorStopClicked = true;

        excavatorPane.setDisable(true);
        FadeTransition fadeTransitionOff = new FadeTransition(Duration.millis(600), excavatorPane);
        fadeTransitionOff.setToValue(0.0f);
        fadeTransitionOff.play();

        bitcoinExcavator.setDisable(false);
        FadeTransition fadeTransitionOn = new FadeTransition(Duration.millis(600), bitcoinExcavator);
        fadeTransitionOn.setToValue(1.0f);
        fadeTransitionOn.play();

        if (excavator != null) {
            excavator.stop();
        }
    };

    public void setOptions(BitcoinOptions options) {
        this.options = options;
    }
}
