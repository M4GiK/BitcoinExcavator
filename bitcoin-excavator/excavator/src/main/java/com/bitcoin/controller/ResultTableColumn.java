package com.bitcoin.controller;

/*
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 */

import java.util.Map;

import javafx.scene.control.TableColumn;

/**
 * Class representing a column in the result table. Adds a "key" property to the
 * standard {@link TableColumn} class that is used to identify the map value
 * that is used to populate the cell.
 */
public class ResultTableColumn extends TableColumn<Map<String, Object>, Object> {
    private String key = null;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
