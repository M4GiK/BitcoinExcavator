package com.bitcoin.util;

import com.cedarsoftware.util.io.JsonReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ObjectJsonDeserializer<T> implements ObjectDeserializer<T> {
    @Override
    public T loadFromFile(String path) throws IOException {
        if (path == null) throw new IllegalArgumentException("path cannot be null");

        FileInputStream stream = new FileInputStream(path);
        return loadFromStream(stream);
    }

    @Override
    public T loadFromStream(InputStream stream) throws IOException {
        if (stream == null) throw new IllegalArgumentException("stream cannot be null");

        JsonReader reader = new JsonReader(stream);
        return (T)reader.readObject();
    }

    @Override
    public T deserialize(String string) throws IOException {
        if (string == null) throw new IllegalArgumentException("string cannot be null");

        return (T)JsonReader.jsonToJava(string);
    }
}
