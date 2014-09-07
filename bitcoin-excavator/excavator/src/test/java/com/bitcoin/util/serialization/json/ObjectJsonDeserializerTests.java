package com.bitcoin.util.serialization.json;

import com.bitcoin.util.BitCoinOptions;
import com.bitcoin.util.serialization.ObjectDeserializer;
import org.junit.Test;

import java.io.IOException;

public class ObjectJsonDeserializerTests {
    private final ObjectDeserializer<BitCoinOptions> deserializer = new ObjectJsonDeserializer<>();

    @Test (expected = IllegalArgumentException.class)
    public void throwsIfInputStringIsNull() throws IOException {
        deserializer.loadFromFile(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void throwsIfInputStreamIsNull() throws IOException {
        deserializer.loadFromStream(null);
    }
}
