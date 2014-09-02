package com.bitcoin.util;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ObjectJsonSerializerTests {
    private final ObjectSerializer<BitcoinOptions> serializer = new ObjectJsonSerializer<>();

    @Test(expected = IllegalArgumentException.class)
    public void saveToFileThrowsIfObjectIsNull() throws IOException {
        serializer.saveToFile(null, "Path");
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveToFileThrowsIfPathIsNull() throws IOException {
        serializer.saveToFile(new BitcoinOptions(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveToFileThrowsIfPathIsEmpty() throws IOException {
        serializer.saveToFile(new BitcoinOptions(), "");
    }

    @Test (expected = IllegalArgumentException.class)
    public void saveToStreamThrowsIfObjectIsNull() throws IOException {
        serializer.saveToStream(null, new ByteArrayOutputStream());
    }

    @Test (expected = IllegalArgumentException.class)
    public void saveToStreamThrowsIfOutputStreamIsNull() throws IOException {
        serializer.saveToStream(new BitcoinOptions(), null);
    }
}
