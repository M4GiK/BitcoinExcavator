/**
 * Project BitcoinExcavator.
 * Copyright Michał Szczygieł & Aleksander Śmierciak
 * Created at Sept. 2, 2014.
 */
package com.bitcoin.util.serialization.json;

import com.bitcoin.util.serialization.ObjectSerializer;
import com.cedarsoftware.util.io.JsonWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

class ObjectJsonSerializer<T> implements ObjectSerializer<T> {
    private final Map<String, Object> serializationArguments;

    public ObjectJsonSerializer() {
        serializationArguments = new HashMap<>();
        serializationArguments.put(JsonWriter.PRETTY_PRINT, true);
    }

    public void saveToFile(T object, String path) throws IOException {
        if (object == null) throw new IllegalArgumentException("object cannot be null");
        if (path == null) throw new IllegalArgumentException("path cannot be null");
        if (path == "") throw new IllegalArgumentException("path cannot be empty");

        FileOutputStream stream = new FileOutputStream(path);
        saveToStream(object, stream);
    }

    public void saveToStream(T object, OutputStream stream) throws IOException {
        if (object == null) throw new IllegalArgumentException("object cannot be null");
        if (stream == null) throw new IllegalArgumentException("stream cannot be null");

        JsonWriter writer = new JsonWriter(stream);
        writer.write(object);
    }

    public String serialize(T object) throws IOException {
        if (object == null) throw new IllegalArgumentException("object cannot be null");

        return JsonWriter.objectToJson(object, serializationArguments);
    }

    public String prettyPrint(String json) throws IOException {
        if (json == null) throw new IllegalArgumentException("json cannot be null");

        return JsonWriter.formatJson(json);
    }
}
