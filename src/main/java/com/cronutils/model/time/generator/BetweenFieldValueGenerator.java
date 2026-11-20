/*
 * Copyright 2015 jmrozanec
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

package com.cronutils.model.time.generator;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;

import java.util.ArrayList;
import java.util.List;

class BetweenFieldValueGenerator extends FieldValueGenerator {

    public BetweenFieldValueGenerator(final CronField cronField) {
        super(cronField);
    }

    @Override
    public int generateNextValue(final int reference) throws NoSuchValueException {
        final Between between = (Between) cronField.getExpression();
        final int from = map(between.getFrom());
        final int to = map(between.getTo());
        final int rangeStart = from > to ? cronField.getConstraints().getStartRange() : from;
        final int rangeEnd = from > to ? cronField.getConstraints().getEndRange() : to;
        if (reference >= rangeEnd) {
            throw new NoSuchValueException();
        }
        final int candidate = Math.max(reference + 1, rangeStart);
        return from > to && candidate > to && candidate < from ? from : candidate;
    }

    @Override
    public int generatePreviousValue(final int reference) throws NoSuchValueException {
        final Between between = (Between) cronField.getExpression();
        final int from = map(between.getFrom());
        final int to = map(between.getTo());
        final int rangeStart = from > to ? cronField.getConstraints().getStartRange() : from;
        final int rangeEnd = from > to ? cronField.getConstraints().getEndRange() : to;
        if (reference <= rangeStart) {
            throw new NoSuchValueException();
        }
        final int candidate = Math.min(reference - 1, rangeEnd);
        return from > to && candidate > to && candidate < from ? to : candidate;
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(final int start, final int end) {
        final List<Integer> values = new ArrayList<>();
        try {
            int reference = generateNextValue(start);
            while (reference < end) {
                values.add(reference);
                reference = generateNextValue(reference);
            }
        } catch (final NoSuchValueException ignored) {
            // No more values remain in this field's range.
        }
        return values;
    }

    @Override
    public boolean isMatch(final int value) {
        final Between between = (Between) cronField.getExpression();
        final int from = map(between.getFrom());
        final int to = map(between.getTo());
        if (from > to) {
            return value >= cronField.getConstraints().getStartRange()
                    && value <= cronField.getConstraints().getEndRange()
                    && (value >= from || value <= to);
        }
        return value >= from && value <= to;
    }

    @Override
    protected boolean matchesFieldExpressionClass(final FieldExpression fieldExpression) {
        return fieldExpression instanceof Between;
    }

    static int map(final FieldValue<?> fieldValue) {
        if (fieldValue instanceof IntegerFieldValue) {
            return ((IntegerFieldValue) fieldValue).getValue();
        }
        throw new IllegalArgumentException("Non integer values at intervals are not fully supported yet.");
    }
}
