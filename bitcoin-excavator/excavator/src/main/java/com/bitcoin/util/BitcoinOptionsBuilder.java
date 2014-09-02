package com.bitcoin.util;

import java.io.IOException;

public class BitcoinOptionsBuilder {
    ObjectDeserializer<BitcoinOptions> deserializer;

    public BitcoinOptionsBuilder(ObjectDeserializer<BitcoinOptions> deserializer) {
        this.deserializer = deserializer;
    }

    public BitcoinOptions fromFile(String path) throws IOException {
        return deserializer.loadFromFile(path);
    }
}
