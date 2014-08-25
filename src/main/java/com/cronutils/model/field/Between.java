package com.cronutils.model.field;

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

/**
 * Represents a range in a cron expression.
 */
public class Between extends FieldExpression {
    private int from;
    private int to;
    private Every every;

    public Between(FieldConstraints constraints, String from, String to) {
        this(constraints, from, to, "1");
    }

    public Between(FieldConstraints constraints, String from, String to, String every) {
        super(constraints);
        this.from = getConstraints().validateInRange(getConstraints().intToInt(getConstraints().stringToInt(from)));
        this.to = getConstraints().validateInRange(getConstraints().intToInt(getConstraints().stringToInt(to)));
        this.every = new Every(getConstraints(), every);
        validate();
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public Every getEvery() {
        return every;
    }

    private void validate() {
        if (from >= to) {
            throw new RuntimeException("Bad range defined! Defined range should satisfy from <= to, but was [%s, %s]");
        }
        if (every.getTime() > (to - from)) {
            throw new RuntimeException("Every x time cannot exceed range length");
        }
    }

    @Override
    public String asString() {
        return String.format("%s-%s%s", from, to, every.asString());
    }
}
