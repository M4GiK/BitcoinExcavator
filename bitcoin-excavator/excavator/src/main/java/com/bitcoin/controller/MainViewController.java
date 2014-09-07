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
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wallet.utils.FileOperations;

import java.io.IOException;
import java.net.URL;
import java.util.*;

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
    public ImageView bitcoinExcavator;
    public ImageView bitcoinExcavatorStop;
    public ImageView bitcoinWallet;
    public TabPane container;
    public AnchorPane mainPage;
    public ScrollPane setupPage;
    public AnchorPane aboutPage;
    public Tab mainTab;
    public Tab setupTab;
    public Tab aboutTab;
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
    public TextField WorkLifetimeField;
    public TextField NetworkStatesAmountField;
    public CheckBox DebuggingModeTick;
    public CheckBox DebuggingTimerTick;
    public TextField EnabledDevicesField;
    public TextField GpuTargetFpsField;
    public TextField GpuTargetFpsBaseField;
    public TextField GpuForceWorkSizeField;
    public TextField GpuVectorsField;
    public CheckBox GpuNoArrayTick;
    public CheckBox GpuDebugSourceTick;

    private ResourceBundle resources = null;
    private Stage walletStage;
    private Boolean excavatorStopClicked = false;
    private BitcoinOptions bitcoinOptions;

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
        bitcoinExcavatorStop.setOnMouseEntered(mouseOverForExcavatorStop);
        bitcoinExcavatorStop.setOnMouseExited(mouseExitFromExcavatorStop);
        bitcoinExcavatorStop.setOnMouseClicked(mouseClickedOnExcavatorStop);
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

        readOptionsFromFile();

        // Initialize something ..
        Platform.runLater(MainViewController.this::readyToGoAnimation);
    }

    private void readOptionsFromFile() {
        SerializationFactory serializationFactory = new JsonSerializationFactory();
        ObjectSerializationFactory<BitcoinOptions> bitcoinOptionsFactory = serializationFactory.createObjectSerializationFactory();
        bitcoinOptionsFactory.createDeserializer();
        BitcoinOptionsBuilder builder = new BitcoinOptionsBuilder(bitcoinOptionsFactory.createDeserializer());
        try {
            bitcoinOptions = builder.fromFile(FileOperations.APP_PATH + FileOperations.BITCOIN_OPTIONS);
        } catch (IOException e) {
            GuiUtils.crashAlert(e);
        }

        setSetupPageFields();
    }

    private void setSetupPageFields() {
        if (bitcoinOptions.getWorklifetime() != null) {
            WorkLifetimeField.setText(bitcoinOptions.getWorklifetime().toString());
        }
        DebuggingModeTick.setSelected(bitcoinOptions.getDebug());
        DebuggingTimerTick.setSelected(bitcoinOptions.getDebugtimer());
        if (bitcoinOptions.getEnabledDevices() != null) {
            EnabledDevicesField.setText(bitcoinOptions.getEnabledDevices().toString());
        }
        if (bitcoinOptions.getGPUTargetFPS() != null) {
            GpuTargetFpsField.setText(bitcoinOptions.getGPUTargetFPS().toString());
        }
        if (bitcoinOptions.getGPUTargetFPSBasis() != null) {
            GpuTargetFpsBaseField.setText(bitcoinOptions.getGPUTargetFPSBasis().toString());
        }
        if (bitcoinOptions.getGPUForceWorkSize() != null) {
            GpuForceWorkSizeField.setText(bitcoinOptions.getGPUForceWorkSize().toString());
        }
        if (bitcoinOptions.getGPUVectors() != null) {
            GpuVectorsField.setText(integersToString(bitcoinOptions.getGPUVectors()));
        }
        GpuNoArrayTick.setSelected(bitcoinOptions.getGPUNoArray());
        GpuDebugSourceTick.setSelected(bitcoinOptions.getGPUDebugSource());
    }

    private <T> String integersToString(Integer[] integers) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < integers.length; ++i) {
            builder.append(integers[i].toString());
        }
        return builder.toString();
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
            Platform.runLater(MainViewController.this::startExcavator);
        }
    };

    private void startExcavator() {
        try {
            excavatorStopClicked = false;
            excavator = new BitcoinExcavator(bitcoinOptions);
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
                Platform.runLater(MainViewController.this::refreshExcavatorValues);
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

    public void keyTypedInWorkLifetimeField() {
        int workLifetime = Integer.parseInt(WorkLifetimeField.getText());
        bitcoinOptions.setWorklifetime(workLifetime);
    }

    public void actionInDebuggingModeTick() {
        boolean debuggingMode = DebuggingModeTick.isSelected();
        bitcoinOptions.setDebug(debuggingMode);
    }

    public void actionInDebuggingTimerTick() {
        boolean debuggingTimer = DebuggingTimerTick.isSelected();
        bitcoinOptions.setDebugtimer(debuggingTimer);
    }

    public void keyTypedInEnabledDevicesField() {
        Set<String> enabledDevices = new HashSet<>(Arrays.asList(EnabledDevicesField.getText().split(",")));
        bitcoinOptions.setEnabledDevices(enabledDevices);
    }

    public void keyTypedInGpuTargetFpsField() {
        double gpuTargetFps = Double.parseDouble(GpuTargetFpsField.getText());
        bitcoinOptions.setGPUTargetFPS(gpuTargetFps);
    }

    public void keyTypedInGpuTargetFpsBaseField() {
        double gpuTargetFpsBase = Double.parseDouble(GpuTargetFpsBaseField.getText());
        bitcoinOptions.setGPUTargetFPSBasis(gpuTargetFpsBase);
    }

    public void keyTypedInGpuForceWorkSizeField() {
        int gpuForceWorkSize = Integer.getInteger(GpuForceWorkSizeField.getText());
        bitcoinOptions.setGPUForceWorkSize(gpuForceWorkSize);
    }

    public void keyTypedInGpuVectorsField() {
        String[] gpuVectorStrings = GpuVectorsField.getText().split(",");
        Integer[] gpuVectors = new Integer[gpuVectorStrings.length];
        for (int i = 0; i < gpuVectorStrings.length; ++i) {
            gpuVectors[i] = Integer.parseInt(gpuVectorStrings[i]);
        }
        bitcoinOptions.setGPUVectors(gpuVectors);
    }

    public void actionInGpuNoArrayTick() {
        boolean gpuNoArray = GpuNoArrayTick.isSelected();
        bitcoinOptions.setGPUNoArray(gpuNoArray);
    }

    public void actionInGpuDebugSourceTick() {
        boolean gpuDebugSource = GpuDebugSourceTick.isSelected();
        bitcoinOptions.setGPUDebugSource(gpuDebugSource);
    }
}
