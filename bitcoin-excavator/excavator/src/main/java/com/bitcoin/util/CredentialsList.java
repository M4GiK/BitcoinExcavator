/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 23, 2014.
 */
package com.bitcoin.util;

import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.converter.DefaultStringConverter;

/**
 * This class represents the {@link javafx.scene.control.ListView} for credentials.
 *
 * @author m4gik <michal.szczygiel@wp.pl>
 */
public class CredentialsList<T> extends TextFieldListCell<T> {

    public CredentialsList(DefaultStringConverter defaultStringConverter) {
        super((javafx.util.StringConverter<T>) defaultStringConverter);
    }

}
