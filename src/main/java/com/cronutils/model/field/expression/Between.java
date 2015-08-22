package com.cronutils.model.field.expression;

/*
 * Copyright 2014 jmrozanec
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;

/**
 * Represents a range in a cron expression.
 */
public class Between extends FieldExpression {
    private FieldValue from;
    private FieldValue to;
    private Every every;

    public Between(FieldConstraints constraints, FieldValue from, FieldValue to) {
        this(constraints, from, to, new IntegerFieldValue(1));
    }

    public Between(FieldConstraints constraints, FieldValue from, FieldValue to, IntegerFieldValue every) {
        super(constraints);
        this.from = validate(from);
        this.to = validate(to);
        this.every = new Every(getConstraints(), every);
        validate();
    }

    public Between(Between between) {
        this(between.getConstraints(), between.getFrom(), between.getTo(), between.getEvery().getTime());
    }

    public FieldValue getFrom() {
        return from;
    }

    public FieldValue getTo() {
        return to;
    }

    public Every getEvery() {
        return every;
    }

    private void validate() {
        if(from instanceof IntegerFieldValue && to instanceof IntegerFieldValue){
            int fromValue = ((IntegerFieldValue)from).getValue();
            int toValue = ((IntegerFieldValue)to).getValue();
            if (fromValue >= toValue) {
                throw new IllegalArgumentException(String.format("Bad range defined! Defined range should satisfy from <= to, but was [%s, %s]", fromValue, toValue));
            }
            if (every.getTime().getValue() > (toValue - fromValue)) {
                throw new IllegalArgumentException("Every x time cannot exceed range length");
            }
        }
        validateSpecialCharValue(from);
        validateSpecialCharValue(to);
    }

    private void validateSpecialCharValue(FieldValue fieldValue){
        if(fieldValue instanceof SpecialCharFieldValue){
            if(!SpecialChar.L.equals(fieldValue.getValue())){
                throw new IllegalArgumentException(String.format("%s value not supported in ranges", fieldValue));
            }
        }
    }

    @Override
    public String asString() {
        return String.format("%s-%s%s", from, to, every.asString());
    }
}
