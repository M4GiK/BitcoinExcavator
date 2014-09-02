package com.bitcoin.util;

import org.junit.Test;

import java.io.IOException;

public class ObjectJsonDeserializerTests {
    private final ObjectDeserializer<BitcoinOptions> deserializer = new ObjectJsonDeserializer<>();

    @Test (expected = IllegalArgumentException.class)
    public void throwsIfInputStringIsNull() throws IOException {
        deserializer.loadFromFile(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void throwsIfInputStreamIsNull() throws IOException {
        deserializer.loadFromStream(null);
    }
}
