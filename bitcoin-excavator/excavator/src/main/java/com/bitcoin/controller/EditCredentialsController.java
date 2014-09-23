/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 23, 2014.
 */
package com.bitcoin.controller;

import com.bitcoin.util.Credential;
import com.bitcoin.util.serialization.ObjectSerializer;
import com.bitcoin.util.serialization.json.JsonSerializationFactory;
import com.bitcoin.view.MainView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * This class control operation between {@link com.bitcoin.util.Credential} and setup view.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class EditCredentialsController implements Initializable {

    public TextField host;
    public TextField worker;
    public TextField password;
    public TextField protocol;
    public TextField path;
    public TextField port;

    public MainView.OverlayUI overlayUi;

    private ResourceBundle resources = null;
    private Credential credential;
    private ListView credentialsView;
    private Integer id;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
    }

    public void initData(Credential credentials, ListView credentialsView, Integer id) {
        this.credential = credentials;
        this.credentialsView = credentialsView;
        this.id = id;
        populateCredentialsItems();
    }

    private void populateCredentialsItems() {
        host.setText(credential.getHost());
        worker.setText(credential.getLogin());
        password.setText(credential.getPassword());
        protocol.setText(credential.getProtocol());
        path.setText(credential.getPath());
        port.setText(Integer.toString(credential.getPort()));
    }

    public void cancel(ActionEvent actionEvent) {
        overlayUi.done();
    }

    public void accept(ActionEvent actionEvent) {
        credential.setHost(host.getText());
        credential.setLogin(worker.getText());
        credential.setPassword(password.getText());
        credential.setProtocol(protocol.getText());
        credential.setPath(path.getText());
        credential.setPort(Integer.parseInt(port.getText()));
        credentialsView.getItems().remove(id);
        credentialsView.getItems().add(id, credential.toString());
        Platform.runLater(overlayUi::done);
    }
}
