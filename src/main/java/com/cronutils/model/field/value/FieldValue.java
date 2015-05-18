package com.cronutils.model.field.value;

public abstract class FieldValue<T> {
    public abstract T getValue();

    public final String toString(){
        return String.format("%s", getValue());
    }
}
