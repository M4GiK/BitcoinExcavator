/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at May 21, 2014.
 */
package com.bitcoin.controller;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import com.bitcoin.core.BitcoinExcavator;
import com.bitcoin.core.BitcoinExcavatorFatalException;
import com.bitcoin.util.BitcoinOptionsBuilder;
import com.bitcoin.util.GuiUtils;
import com.bitcoin.util.ObjectJsonDeserializer;
import com.bitcoin.view.MainView;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wallet.utils.FileOperations;

import static com.bitcoin.view.MainView.excavator;
import static com.bitcoin.view.MainView.instance;

/**
 * Main view controller for excavator.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class MainViewController implements Initializable {

    /**
     * Logger for monitoring runtime.
     */
    private static final Logger log = LoggerFactory.getLogger(MainViewController.class);

    public HBox controlsBox;
    public VBox progressBox;
    public ProgressIndicator progress;
    public Label loadingLabel;
    public ImageView bitcoinExcavator;
    public ImageView bitcoinWallet;
    public TabPane container;
    public AnchorPane mainPage;
    public AnchorPane setupPage;
    public AnchorPane aboutPage;
    public Tab mainTab;
    public Tab setupTab;
    public Tab aboutTab;

    private ResourceBundle resources = null;
    private Stage walletStage;

    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        progress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        progress.setVisible(true);
        container.setOpacity(0.0);
        setupPage.setOpacity(0.0);
        aboutPage.setOpacity(0.0);
        bitcoinExcavator.setOnMouseEntered(mouseOverForExcavator);
        bitcoinExcavator.setOnMouseExited(mouseExitFromExcavator);
        bitcoinExcavator.setOnMouseClicked(mouseClickedOnExcavator);
        bitcoinWallet.setOnMouseEntered(mouseOverForBitcoinWallet);
        bitcoinWallet.setOnMouseExited(mouseExitFromBitcoinWallet);
        bitcoinWallet.setOnMouseClicked(mouseClickedOnBitcoinWallet);
        container.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, recenltySelectedTab) -> {
            if (recenltySelectedTab.equals(mainTab)) {
                setupPage.setOpacity(0.0);
                aboutPage.setOpacity(0.0);
                FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), mainPage);
                fadeTransition.setToValue(1.0);
                fadeTransition.play();
            }
            if (recenltySelectedTab.equals(setupTab)) {
                mainPage.setOpacity(0.0);
                aboutPage.setOpacity(0.0);
                FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), setupPage);
                fadeTransition.setToValue(1.0);
                fadeTransition.play();
            }
            if (recenltySelectedTab.equals(aboutTab)) {
                setupPage.setOpacity(0.0);
                mainPage.setOpacity(0.0);
                FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), aboutPage);
                fadeTransition.setToValue(1.0);
                fadeTransition.play();
            }
        });
        Tooltip.install(bitcoinExcavator, new Tooltip(resources.getString("toolTipExcavator")));
        Tooltip.install(bitcoinWallet, new Tooltip(resources.getString("toolTipWallet")));
        // Initialize something ..
        Platform.runLater(MainViewController.this::readyToGoAnimation);
    }

    public void readyToGoAnimation() {
        // Sync progress bar slides out ...
        FadeTransition reveal = new FadeTransition(Duration.millis(500), progressBox);
        reveal.setToValue(0.0);
        // Buttons slide in a appears simultaneously.
        FadeTransition arrive = new FadeTransition(Duration.millis(600), container);
        arrive.setToValue(1.0);
        // Buttons slide in a appears simultaneously.
        TranslateTransition transit = new TranslateTransition(Duration.millis(600), controlsBox);
        transit.setToY(0.0);
        // Slide out happens then slide in/fade happens.
        SequentialTransition sequentialTransition = new SequentialTransition(reveal, arrive, transit);
        sequentialTransition.setCycleCount(1);
        sequentialTransition.setInterpolator(Interpolator.EASE_BOTH);
        sequentialTransition.play();
        progressBox.setDisable(true);
    }

    private EventHandler<MouseEvent> mouseOverForExcavator =  mouseEvent -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(600), bitcoinExcavator);
            scale.setToX(.85f);
            scale.setToY(.85f);
            scale.setAutoReverse(true);
            scale.play();
    };

    private EventHandler<MouseEvent> mouseExitFromExcavator =  mouseEvent -> {
            ScaleTransition rescale = new ScaleTransition(Duration.millis(600), bitcoinExcavator);
            rescale.setToX(1.0f);
            rescale.setToY(1.0f);
            rescale.play();
    };

    private EventHandler<MouseEvent> mouseClickedOnExcavator = mouseEvent -> {
//        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(600), instance.getMainWindow());
//        scaleTransition.setToX(1.0f);
//        scaleTransition.setToY(1.2f);
//        scaleTransition.play();


//        if(excavator == null) {
//            Platform.runLater(MainViewController.this::startExcavator);
//        } else {
//            System.out.println(excavator.getHashCount() + " " + excavator.getCurrentTime());
//        }
    };

    private void startExcavator() {
        try {
            BitcoinOptionsBuilder builder = new BitcoinOptionsBuilder(new ObjectJsonDeserializer<>());
            excavator = new BitcoinExcavator(builder.fromFile(FileOperations.APP_PATH
                    + FileOperations.BITCOIN_OPTIONS));
            Thread excavatorThread = new Thread(excavator);
            excavatorThread.start();
        } catch (BitcoinExcavatorFatalException e) {
            GuiUtils.crashAlert(e);
        } catch (IOException e) {
            GuiUtils.crashAlert(e);
        }
    }

    private EventHandler<MouseEvent> mouseOverForBitcoinWallet =  mouseEvent -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(600), bitcoinWallet);
            scale.setToX(.85f);
            scale.setToY(.85f);
            scale.setAutoReverse(true);
            scale.play();
    };

    private EventHandler<MouseEvent> mouseExitFromBitcoinWallet =  mouseEvent -> {
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
}
