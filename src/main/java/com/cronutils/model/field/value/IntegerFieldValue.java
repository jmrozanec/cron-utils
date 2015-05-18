package com.cronutils.model.field.value;

public class IntegerFieldValue extends FieldValue<Integer>{
    private int value;

    public IntegerFieldValue(int value){
        this.value=value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
