/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 2, 2014.
 */
package com.bitcoin.util.serialization;

import java.io.IOException;
import java.io.OutputStream;

public interface ObjectSerializer<T> {
    void saveToFile(T object, String path) throws IOException;

    void saveToStream(T object, OutputStream stream) throws IOException;

    String serialize(T object) throws IOException;
}
