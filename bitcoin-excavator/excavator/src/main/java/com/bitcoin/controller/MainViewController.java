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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
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
        log.debug(resources.toString());
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

    private EventHandler<MouseEvent> mouseOverForExcavator = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            ScaleTransition scale = new ScaleTransition(Duration.millis(600), bitcoinExcavator);
            scale.setToX(.85f);
            scale.setToY(.85f);
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

    private EventHandler<MouseEvent> mouseClickedOnExcavator = mouseEvent -> System.out.println("lol");

    private EventHandler<MouseEvent> mouseOverForBitcoinWallet = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            ScaleTransition scale = new ScaleTransition(Duration.millis(600), bitcoinWallet);
            scale.setToX(.85f);
            scale.setToY(.85f);
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

}
