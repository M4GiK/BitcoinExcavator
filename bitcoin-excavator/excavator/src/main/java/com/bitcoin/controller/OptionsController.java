package com.bitcoin.controller;

import com.bitcoin.util.BitcoinOptions;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class OptionsController implements Initializable {
    public GridPane setupPage;

    public TextField workLifetimeField;
    public CheckBox debuggingModeTick;
    public CheckBox debuggingTimerTick;
    public TextField enabledDevicesField;
    public TextField gpuTargetFpsField;
    public TextField gpuTargetFpsBaseField;
    public TextField gpuForceWorkSizeField;
    public TextField gpuVectorsField;
    public CheckBox gpuNoArrayTick;
    public CheckBox gpuDebugSourceTick;

    private BitcoinOptions model;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    private void setSetupPageFields() {
        if (model.getWorklifetime() != null) {
            workLifetimeField.setText(model.getWorklifetime().toString());
        }
        debuggingModeTick.setSelected(model.getDebug());
        debuggingTimerTick.setSelected(model.getDebugtimer());
        if (model.getEnabledDevices() != null) {
            enabledDevicesField.setText(model.getEnabledDevices().toString());
        }
        if (model.getGPUTargetFPS() != null) {
            gpuTargetFpsField.setText(model.getGPUTargetFPS().toString());
        }
        if (model.getGPUTargetFPSBasis() != null) {
            gpuTargetFpsBaseField.setText(model.getGPUTargetFPSBasis().toString());
        }
        if (model.getGPUForceWorkSize() != null) {
            gpuForceWorkSizeField.setText(model.getGPUForceWorkSize().toString());
        }
        if (model.getGPUVectors() != null) {
            gpuVectorsField.setText(integersToString(model.getGPUVectors()));
        }
        gpuNoArrayTick.setSelected(model.getGPUNoArray());
        gpuDebugSourceTick.setSelected(model.getGPUDebugSource());
    }

    public BitcoinOptions getModel() {
        return model;
    }

    public void setModel(BitcoinOptions model) {
        this.model = model;
        setSetupPageFields();
    }

    private String integersToString(Integer[] integers) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < integers.length; ++i) {
            builder.append(integers[i].toString());
        }
        return builder.toString();
    }

    public void keyTypedInWorkLifetimeField() {
        int workLifetime = Integer.parseInt(workLifetimeField.getText());
        model.setWorklifetime(workLifetime);
    }

    public void actionInDebuggingModeTick() {
        boolean debuggingMode = debuggingModeTick.isSelected();
        model.setDebug(debuggingMode);
    }

    public void actionInDebuggingTimerTick() {
        boolean debuggingTimer = debuggingTimerTick.isSelected();
        model.setDebugtimer(debuggingTimer);
    }

    public void keyTypedInEnabledDevicesField() {
        Set<String> enabledDevices = new HashSet<>(Arrays.asList(enabledDevicesField.getText().split(",")));
        model.setEnabledDevices(enabledDevices);
    }

    public void keyTypedInGpuTargetFpsField() {
        double gpuTargetFps = Double.parseDouble(gpuTargetFpsField.getText());
        model.setGPUTargetFPS(gpuTargetFps);
    }

    public void keyTypedInGpuTargetFpsBaseField() {
        double gpuTargetFpsBase = Double.parseDouble(gpuTargetFpsBaseField.getText());
        model.setGPUTargetFPSBasis(gpuTargetFpsBase);
    }

    public void keyTypedInGpuForceWorkSizeField() {
        int gpuForceWorkSize = Integer.getInteger(gpuForceWorkSizeField.getText());
        model.setGPUForceWorkSize(gpuForceWorkSize);
    }

    public void keyTypedInGpuVectorsField() {
        String[] gpuVectorStrings = gpuVectorsField.getText().split(",");
        Integer[] gpuVectors = new Integer[gpuVectorStrings.length];
        for (int i = 0; i < gpuVectorStrings.length; ++i) {
            gpuVectors[i] = Integer.parseInt(gpuVectorStrings[i]);
        }
        model.setGPUVectors(gpuVectors);
    }

    public void actionInGpuNoArrayTick() {
        boolean gpuNoArray = gpuNoArrayTick.isSelected();
        model.setGPUNoArray(gpuNoArray);
    }

    public void actionInGpuDebugSourceTick() {
        boolean gpuDebugSource = gpuDebugSourceTick.isSelected();
        model.setGPUDebugSource(gpuDebugSource);
    }
}
