package com.bitcoin.util.serialization;

public interface ObjectSerializationFactory<T> {
    ObjectSerializer<T> createSerializer();

    ObjectDeserializer<T> createDeserializer();
}
