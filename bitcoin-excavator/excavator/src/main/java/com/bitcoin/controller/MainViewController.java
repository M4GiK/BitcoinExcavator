/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at May 21, 2014.
 */
package com.bitcoin.controller;


import java.net.URL;
import java.util.ResourceBundle;

import com.bitcoin.view.MainView;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

    private ResourceBundle resources = null;
    private Stage walletStage;

    private EventHandler<MouseEvent> mouseOverForExcavator = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            ScaleTransition scale = new ScaleTransition(Duration.millis(600), bitcoinExcavator);
            scale.setToX(.75f);
            scale.setToY(.75f);
            scale.setAutoReverse(true);
            scale.play();
        }
    };

    private EventHandler<MouseEvent> mouseExitFromExcavator = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            ScaleTransition rescale = new ScaleTransition(Duration.millis(600), bitcoinExcavator);
            rescale.setToX(1.0f);
            rescale.setToY(1.0f);
            rescale.play();
        }
    };

    private EventHandler<MouseEvent> mouseClickedOnExcavator = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            System.out.println("lol");
        }
    };

    private EventHandler<MouseEvent> mouseOverForBitcoinWallet = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            ScaleTransition scale = new ScaleTransition(Duration.millis(600), bitcoinWallet);
            scale.setToX(.75f);
            scale.setToY(.75f);
            scale.setAutoReverse(true);
            scale.play();
        }
    };

    private EventHandler<MouseEvent> mouseExitFromBitcoinWallet = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            ScaleTransition rescale = new ScaleTransition(Duration.millis(600), bitcoinWallet);
            rescale.setToX(1.0f);
            rescale.setToY(1.0f);
            rescale.play();
        }
    };

    private EventHandler<MouseEvent> mouseClickedOnBitcoinWallet = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {

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
        }
    };

    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        log.debug(resources.toString());
        progress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        progress.setVisible(true);
        bitcoinExcavator.setOnMouseEntered(mouseOverForExcavator);
        bitcoinExcavator.setOnMouseExited(mouseExitFromExcavator);
        bitcoinExcavator.setOnMouseClicked(mouseClickedOnExcavator);
        bitcoinWallet.setOnMouseEntered(mouseOverForBitcoinWallet);
        bitcoinWallet.setOnMouseExited(mouseExitFromBitcoinWallet);
        bitcoinWallet.setOnMouseClicked(mouseClickedOnBitcoinWallet);

        // Initialize something ..
        Platform.runLater(MainViewController.this::readyToGoAnimation);
    }

    public void readyToGoAnimation() {

        // Sync progress bar slides out ...
        FadeTransition reveal = new FadeTransition(Duration.millis(500), progressBox);
        reveal.setToValue(0.0);

        // Buttons slide in a appears simultaneously.
        TranslateTransition arrive = new TranslateTransition(Duration.millis(600), controlsBox);
        arrive.setToY(0.0);

        // Slide out happens then slide in/fade happens.
        SequentialTransition both = new SequentialTransition(reveal, arrive);
        both.setCycleCount(1);
        both.setInterpolator(Interpolator.EASE_BOTH);
        both.play();
        progressBox.setDisable(true);
    }
}
