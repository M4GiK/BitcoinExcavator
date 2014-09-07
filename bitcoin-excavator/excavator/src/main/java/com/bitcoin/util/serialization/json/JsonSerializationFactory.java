package com.bitcoin.util.serialization.json;

import com.bitcoin.util.serialization.ObjectSerializationFactory;
import com.bitcoin.util.serialization.SerializationFactory;

public class JsonSerializationFactory implements SerializationFactory {
    public <T> ObjectSerializationFactory<T> createObjectSerializationFactory() {
        return new ObjectJsonSerializationFactory<>();
    }
}
