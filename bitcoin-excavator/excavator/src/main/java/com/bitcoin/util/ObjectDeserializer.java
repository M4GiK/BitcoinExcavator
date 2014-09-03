/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 2, 2014.
 */
package com.bitcoin.util;

import java.io.IOException;
import java.io.InputStream;

public interface ObjectDeserializer<T> {
    T loadFromFile(String path) throws IOException;

    T loadFromStream(InputStream stream) throws IOException;

    T deserialize(String string) throws IOException;
}
