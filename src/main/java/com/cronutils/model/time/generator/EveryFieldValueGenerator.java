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

import java.util.ArrayList;
import java.util.List;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.utils.VisibleForTesting;

class EveryFieldValueGenerator extends FieldValueGenerator {

    private final int from;
    private final int to;

    public EveryFieldValueGenerator(final CronField cronField) {
        super(cronField);

        final Every every = (Every) cronField.getExpression();
        final FieldExpression everyExpression = every.getExpression();
        if (everyExpression instanceof Between) {
            final Between between = (Between) everyExpression;

            from = Math.max(cronField.getConstraints().getStartRange(), BetweenFieldValueGenerator.map(between.getFrom()));
            to = Math.min(cronField.getConstraints().getEndRange(), BetweenFieldValueGenerator.map(between.getTo()));
        } else {
            from = cronField.getConstraints().getStartRange();
            to = cronField.getConstraints().getEndRange();
        }
    }

    @Override
    public int generateNextValue(final int reference) throws NoSuchValueException {
        //intuition: for valid values, we have: offset+period*i
        if (reference >= to) {
            throw new NoSuchValueException();
        }
        final Every every = (Every) cronField.getExpression();

        final int referenceWithoutOffset = reference - offset();
        final int period = every.getPeriod().getValue();
        final int remainder = referenceWithoutOffset % period;

        final int next = reference + (period - remainder);
        if (next < from) {
            return from;
        }
        if (next > to) {
            throw new NoSuchValueException();
        }

        return next;
    }

    @Override
    public int generatePreviousValue(final int reference) throws NoSuchValueException {
        final Every every = (Every) cronField.getExpression();
        final int period = every.getPeriod().getValue();
        final int remainder = reference % period;
        if (remainder == 0) {
            return reference - period;
        } else {
            return reference - remainder;
        }
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(final int start, final int end) {
        final List<Integer> values = new ArrayList<>();
        try {
            final int offset = offset();
            if (start != offset) {
                values.add(offset);
            }
            int reference = generateNextValue(start);
            while (reference < end) {
                if (reference != offset) {
                    values.add(reference);
                }
                reference = generateNextValue(reference);
            }
        } catch (final NoSuchValueException ignored) {
            // We just skip, since we generate values until we get the exception
        }
        return values;
    }

    @Override
    public boolean isMatch(final int value) {
        final Every every = (Every) cronField.getExpression();
        final int start = offset();
        return ((value - start) % every.getPeriod().getValue()) == 0 && value >= from && value <= to;
    }

    @Override
    protected boolean matchesFieldExpressionClass(final FieldExpression fieldExpression) {
        return fieldExpression instanceof Every;
    }

    @VisibleForTesting
    int offset() {
        final FieldExpression expression = ((Every) cronField.getExpression()).getExpression();
        if (expression instanceof On) {
            return ((On) expression).getTime().getValue();
        }
        return from;
    }
}
