package com.bitcoin.controller;

/*
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 */

import java.util.Map;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

/**
 * Result cell value factory. Retrieves cell values from a map.
 */
public class ResultCellValueFactory
        implements
        Callback<CellDataFeatures<Map<String, Object>, Object>, ObservableValue<Object>> {
    public ObservableValue<Object> call(
            CellDataFeatures<Map<String, Object>, Object> param) {
        ResultTableColumn resultTableColumn = (ResultTableColumn) param
                .getTableColumn();
        Map<String, Object> row = param.getValue();

        return new ReadOnlyObjectWrapper<Object>(row.get(resultTableColumn
                .getKey()));
    }
}
