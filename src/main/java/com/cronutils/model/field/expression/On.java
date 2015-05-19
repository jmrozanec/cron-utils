package com.cronutils.model.field.expression;

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import org.apache.commons.lang3.Validate;

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
public class On extends FieldExpression {
    private static final int DEFAULT_NTH_VALUE = -1;
    private IntegerFieldValue time;
    private IntegerFieldValue nth;
    private SpecialCharFieldValue specialChar;

    private On(On on){
        this(on.constraints, on.time, on.specialChar, on.nth);
    }

    public On(FieldConstraints constraints, SpecialCharFieldValue specialChar) {
        this(constraints, new IntegerFieldValue(DEFAULT_NTH_VALUE), specialChar);
    }

    public On(FieldConstraints constraints, IntegerFieldValue time) {
        this(constraints, time, new SpecialCharFieldValue(SpecialChar.NONE));
    }

    public On(FieldConstraints constraints, IntegerFieldValue time, SpecialCharFieldValue specialChar) {
        this(constraints, time, specialChar, new IntegerFieldValue(-1));
        if(specialChar.getValue().equals(SpecialChar.HASH)){
            throw new IllegalArgumentException("value missing for a#b cron expression");
        }
    }

    public On(FieldConstraints constraints, IntegerFieldValue time, SpecialCharFieldValue specialChar, IntegerFieldValue nth) {
        super(constraints);
        Validate.notNull(time, "time must not be null");
        Validate.notNull(specialChar, "special char must not null");
        Validate.notNull(nth, "nth value must not be null");
        if(!specialChar.getValue().equals(SpecialChar.HASH) && !specialChar.getValue().equals(SpecialChar.NONE)){
            this.time = (time.getValue()!=-1)?(IntegerFieldValue)validate(time):time;
        } else {
            this.time = (IntegerFieldValue)validate(time);
        }
        this.specialChar = (SpecialCharFieldValue)validate(specialChar);
        this.nth = (nth.getValue()!=-1)?(IntegerFieldValue)validate(nth):nth;
    }

    public IntegerFieldValue getTime() {
        return time;
    }

    public IntegerFieldValue getNth() {
        return nth;
    }

    public SpecialCharFieldValue getSpecialChar() {
        return specialChar;
    }

    @Override
    public String asString() {
        switch (specialChar.getValue()){
            case NONE:
                return ""+getTime();
            case HASH:
                return String.format("%s#%s", getTime(), getNth());
            case W:
                if(isDefault(getTime())){
                    return "W";
                }else{
                    return String.format("%sW", getTime());
                }
            case L:
                if(isDefault(getTime())){
                    return "L";
                }else{
                    return String.format("%sL", getTime());
                }
            default:
                return specialChar.toString();
        }
    }

    private boolean isDefault(IntegerFieldValue fieldValue){
        return fieldValue.getValue()==DEFAULT_NTH_VALUE;
    }
}
