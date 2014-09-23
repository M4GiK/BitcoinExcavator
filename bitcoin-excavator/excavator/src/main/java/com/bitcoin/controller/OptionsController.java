/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 7, 2014.
 */
package com.bitcoin.controller;

import com.bitcoin.util.BitcoinOptions;
import com.bitcoin.util.Credential;
import com.bitcoin.util.CredentialsList;
import com.bitcoin.util.GuiUtils;

import com.bitcoin.view.MainView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.util.converter.DefaultStringConverter;

import java.net.URL;
import java.util.*;

public class OptionsController implements Initializable {
    public GridPane setupPage;

    public ListView credentialsView;
    public TextField proxyField;
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
    private ResourceBundle resources = null;
    private ObservableList<String> list;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resources = resourceBundle;
        credentialsView.setCellFactory(param -> new CredentialsList<>(new DefaultStringConverter()));
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
        populateCredentialsView();
    }

    private String integersToString(Integer[] integers) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < integers.length; ++i) {
            builder.append(integers[i].toString());
        }
        return builder.toString();
    }

    private void populateCredentialsView() {
        List<String> items = new ArrayList<>();
        for (Credential credential : model.getCredentials()) {
            items.add(credential.toString());
        }
        list = FXCollections.observableList(items);
        credentialsView.setItems(list);
        createEmptyPosition(credentialsView);
    }

    private void createEmptyPosition(ListView credentialsView) {
        Integer id = credentialsView.getItems().size();
        credentialsView.getItems().add(id, resources.getString("pressToAddCredentials"));
    }

    public void keyTypedInProxyField() {

    }

    public void keyTypedInWorkLifetimeField(KeyEvent event) {
        if (event.getText() == null) return;
        try {
            int workLifetime = Integer.parseInt(event.getText());
            model.setWorklifetime(workLifetime);
        } catch (NumberFormatException e) {
            GuiUtils.informationalAlert("Invalid characters found", "New value will be ignored.");
        }
    }

    public void actionInDebuggingModeTick() {
        boolean debuggingMode = debuggingModeTick.isSelected();
        model.setDebug(debuggingMode);
    }

    public void actionInDebuggingTimerTick() {
        boolean debuggingTimer = debuggingTimerTick.isSelected();
        model.setDebugtimer(debuggingTimer);
    }

    public void keyTypedInEnabledDevicesField(KeyEvent event) {
        if (event.getText() == null) return;
        Set<String> enabledDevices = new HashSet<>(Arrays.asList(event.getText().split(",")));
        model.setEnabledDevices(enabledDevices);
    }

    public void keyTypedInGpuTargetFpsField(KeyEvent event) {
        if (event.getText() == null) return;
        try {
            double gpuTargetFps = Double.parseDouble(event.getText());
            model.setGPUTargetFPS(gpuTargetFps);
        } catch (NumberFormatException e) {
            GuiUtils.informationalAlert("Invalid characters found", "New value will be ignored.");
        }
    }

    public void keyTypedInGpuTargetFpsBaseField(KeyEvent event) {
        if (event.getText() == null) return;
        try {
            double gpuTargetFpsBase = Double.parseDouble(event.getText());
            model.setGPUTargetFPSBasis(gpuTargetFpsBase);
        } catch (NumberFormatException e) {
            GuiUtils.informationalAlert("Invalid characters found", "New value will be ignored.");
        }
    }

    public void keyTypedInGpuForceWorkSizeField(KeyEvent event) {
        if (event.getText() == null) return;
        try {
            int gpuForceWorkSize = Integer.parseInt(event.getText());
            model.setGPUForceWorkSize(gpuForceWorkSize);
        } catch (NumberFormatException e) {
            GuiUtils.informationalAlert("Invalid characters found", "New value will be ignored.");
        }
    }

    public void keyTypedInGpuVectorsField(KeyEvent event) {
        if (event.getText() == null) return;
        String[] gpuVectorStrings = event.getText().split(",");
        Integer[] gpuVectors = new Integer[gpuVectorStrings.length];
        try {
            for (int i = 0; i < gpuVectorStrings.length; ++i) {
                gpuVectors[i] = Integer.parseInt(gpuVectorStrings[i]);
            }
            model.setGPUVectors(gpuVectors);
        } catch (NumberFormatException e) {
            GuiUtils.informationalAlert("Invalid characters found", "New value will be ignored.");
        }
    }

    public void actionInGpuNoArrayTick() {
        boolean gpuNoArray = gpuNoArrayTick.isSelected();
        model.setGPUNoArray(gpuNoArray);
    }

    public void actionInGpuDebugSourceTick() {
        boolean gpuDebugSource = gpuDebugSourceTick.isSelected();
        model.setGPUDebugSource(gpuDebugSource);
    }


    public void editCommitInCredentialsView(ListView.EditEvent<String> event) {
        credentialsView.getItems().set(event.getIndex(), event.getNewValue());
        Credential oldValue = model.getCredential(event.getIndex());
        String text = event.getNewValue();
        String[] parts = text.split("://|:|@");

        if (parts.length != 5) {
            throw new IllegalArgumentException("Could not parse credentials: " + text);
        }

        // "PROTOCOL://LOGIN:PASSWORD@HOST:PORT"
        String protocol = parts[0];
        String login = parts[1];
        String password = parts[2];
        String host = parts[3];
        Integer port = Integer.parseInt(parts[4]);
        Credential newValue = new Credential(login, password, host, protocol, "", port);

        model.removeCredential(oldValue);
        model.addCredential(newValue);
    }

    public void openEditCredentialsView(ListView.EditEvent<String> editEvent) {
        MainView.instance.overlayUIEditCredentials("/fxml/edit-credentials.fxml",
                model.getCredential(editEvent.getIndex()), resources, credentialsView, editEvent.getIndex());
    }
}
