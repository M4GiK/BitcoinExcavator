package com.bitcoin.util.serialization.json;

import com.bitcoin.util.serialization.ObjectDeserializer;
import com.bitcoin.util.serialization.ObjectSerializationFactory;
import com.bitcoin.util.serialization.ObjectSerializer;

class ObjectJsonSerializationFactory<T> implements ObjectSerializationFactory<T> {
    public ObjectSerializer<T> createSerializer() {
        return new ObjectJsonSerializer<>();
    }

    public ObjectDeserializer<T> createDeserializer() {
        return new ObjectJsonDeserializer<>();
    }
}
