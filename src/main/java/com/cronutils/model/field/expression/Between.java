package com.cronutils.model.field.expression;

/*
 * Copyright 2014 jmrozanec
 * 
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

/**
 * Represents a range in a cron expression.
 */
public class Between extends FieldExpression {
    private FieldValue<?> from;
    private FieldValue<?> to;

    public Between(Between between) {
        this(between.getFrom(), between.getTo());
    }

    public Between(FieldValue<?> from, FieldValue<?> to) {
        this.from = from;
        this.to = to;
    }

    public FieldValue<?> getFrom() {
        return from;
    }

    public FieldValue<?> getTo() {
        return to;
    }

    @Override
    public String asString() {
        return String.format("%s-%s", from, to);
    }
}
