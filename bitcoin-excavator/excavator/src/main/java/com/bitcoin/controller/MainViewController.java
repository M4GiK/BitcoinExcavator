/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at May 21, 2014.
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
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
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
    public TabPane container;
    public AnchorPane mainPage;
    public GridPane setupPage;
    public AnchorPane aboutPage;
    public Tab mainTab;
    public Tab setupTab;
    public Tab aboutTab;

    @FXML
    private OptionsController setupPageController;

    @FXML
    private MainTabController mainPageController;

    private ResourceBundle resources = null;

    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        progress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        progress.setVisible(true);
        container.setOpacity(0.0);
        setupPage.setOpacity(0.0);
        aboutPage.setOpacity(0.0);

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

        //setupController.initialize(location, resources);
        readOptionsFromFile();

        // Initialize something ..
        Platform.runLater(MainViewController.this::readyToGoAnimation);
    }

    private void readOptionsFromFile() {
        BitcoinOptions options = retrieveOptions();
        setupPageController.setModel(options);
        mainPageController.setOptions(options);

    }

    private BitcoinOptions retrieveOptions() {
        BitcoinOptionsBuilder builder = createBitcoinOptionsBuilder();
        BitcoinOptions options;
        try {
            options = builder.fromFile(FileOperations.APP_PATH + FileOperations.BITCOIN_OPTIONS);
        } catch (Exception e) {
            options = new BitcoinOptions();
        }
        return options;
    }

    private BitcoinOptionsBuilder createBitcoinOptionsBuilder() {
        SerializationFactory serializationFactory = new JsonSerializationFactory();
        ObjectSerializationFactory<BitcoinOptions> bitcoinOptionsFactory = serializationFactory.createObjectSerializationFactory();
        bitcoinOptionsFactory.createDeserializer();
        return new BitcoinOptionsBuilder(bitcoinOptionsFactory.createDeserializer());
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


}
