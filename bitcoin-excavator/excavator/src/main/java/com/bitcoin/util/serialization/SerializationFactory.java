package com.bitcoin.util.serialization;

public interface SerializationFactory {
    public <T> ObjectSerializationFactory<T> createObjectSerializationFactory();
}
