package com.cronutils.builder.model.expression;

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

import com.cronutils.model.field.value.IntegerFieldValue;
import org.apache.commons.lang3.Validate;

/**
 * Represents a star (*) value on cron expression field
 */
public class Always extends FieldExpression {
    private Every every;

    private Always(Always always) {
        this(always.getEvery().getTime());
    }

    public Always() {
        this.every = new Every(new IntegerFieldValue(1));
    }

    public Always(IntegerFieldValue every) {
        Validate.notNull(every, "'Every' value cannot be null.");
        this.every = new Every(every);
    }

    @Override
    public String asString() {
        return String.format("*%s", every.asString());
    }

    public Every getEvery() {
        return every;
    }
}
