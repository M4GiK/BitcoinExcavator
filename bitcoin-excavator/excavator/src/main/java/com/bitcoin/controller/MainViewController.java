/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at May 21, 2014.
 */
package com.bitcoin.controller;


import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO COMMENTS MISSING!
 * 
 * @author m4gik <michal.szczygiel@wp.pl>
 * 
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

    private ResourceBundle resources = null;

    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        log.debug(resources.toString());
        progress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        progress.setVisible(true);

        // Initialize something ..
        Platform.runLater(MainViewController.this::readyToGoAnimation);
    }

//    public ProgressBarUpdater progressBarUpdater() {
//        return new ProgressBarUpdater();
//    }

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
    }
}
