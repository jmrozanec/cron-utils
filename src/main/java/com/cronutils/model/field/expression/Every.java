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

import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import org.apache.commons.lang3.Validate;

/**
 * Represents every x time on a cron field.
 */
public class Every extends FieldExpression {
    private FieldExpression expression;
    private IntegerFieldValue period;

    public Every(IntegerFieldValue time) {
        this(new Always(), time);
    }

    public Every(FieldExpression expression, IntegerFieldValue period) {
        this.expression = Validate.notNull(expression, "Expression must not be null");
        if (period == null) {
            period = new IntegerFieldValue(1);
        }
        this.period = period;
    }

    private Every(Every every){
        this(every.getExpression(), every.getPeriod());
    }

    public IntegerFieldValue getPeriod() {
        return period;
    }

    public FieldExpression getExpression(){
        return expression;
    }

    @Override
    public String asString() {
        if(period.getValue()==1){
            return expression.asString()!=null?"*":"";
        }
        return String.format("%s/%s", this.expression.asString(), getPeriod());
    }
}
