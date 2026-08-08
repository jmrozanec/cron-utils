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
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.utils.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

class EveryFieldValueGenerator extends FieldValueGenerator {

    protected final int from;
    protected final int to;
    /**
     * When true, {@code start/period} on the month field wraps within the year
     * (e.g. {@code 10/3} → Oct, Jan, Apr, Jul). Matches continuous "every N months
     * starting at month M" semantics; see issue #692 and {@link Every} javadoc.
     */
    private final boolean wrapMonths;

    public EveryFieldValueGenerator(final CronField cronField) {
        super(cronField);

        final Every every = (Every) cronField.getExpression();
        final FieldExpression everyExpression = every.getExpression();
        if (everyExpression instanceof Between) {
            final Between between = (Between) everyExpression;

            from = Math.max(cronField.getConstraints().getStartRange(), BetweenFieldValueGenerator.map(between.getFrom()));
            to = Math.min(cronField.getConstraints().getEndRange(), BetweenFieldValueGenerator.map(between.getTo()));
            wrapMonths = false;
        } else if (everyExpression instanceof On) {
            final On on = (On) everyExpression;
            // Month intervals wrap the calendar year so "10/3" is every 3 months
            // from October (1,4,7,10), not only month 10.
            if (CronFieldName.MONTH.equals(cronField.getField())) {
                from = cronField.getConstraints().getStartRange();
                to = cronField.getConstraints().getEndRange();
                wrapMonths = true;
            } else {
                from = on.getTime().getValue();
                to = cronField.getConstraints().getEndRange();
                wrapMonths = false;
            }
        } else {
            from = cronField.getConstraints().getStartRange();
            to = cronField.getConstraints().getEndRange();
            wrapMonths = false;
        }
    }

    @Override
    public int generateNextValue(final int reference) throws NoSuchValueException {
        if (wrapMonths) {
            return generateNextValueWrapping(reference);
        }
        //intuition: for valid values, we have: offset+period*i
        if (reference >= to) {
            throw new NoSuchValueException();
        }
        final Every every = (Every) cronField.getExpression();

        final int next = getNext(reference, every);
        if (next < from) {
            return from;
        }
        if (next > to) {
            throw new NoSuchValueException();
        }

        return next;
    }

    private int generateNextValueWrapping(final int reference) throws NoSuchValueException {
        final int period = ((Every) cronField.getExpression()).getPeriod().getValue();
        final int offset = offset();
        for (int candidate = reference + 1; candidate <= to; candidate++) {
            if (matchesWrapped(candidate, offset, period)) {
                return candidate;
            }
        }
        throw new NoSuchValueException();
    }

    private int getNext(int reference, Every every) {
        final int offset = offset();

        if (reference >= offset) {
            final int referenceWithoutOffset = reference - offset;
            final int period = every.getPeriod().getValue();
            final int remainder = referenceWithoutOffset % period;

            return reference + (period - remainder);
        } else {
            return offset;
        }
    }

    @Override
    public int generatePreviousValue(final int reference) throws NoSuchValueException {
        if (wrapMonths) {
            return generatePreviousValueWrapping(reference);
        }
        final Every every = (Every) cronField.getExpression();
        if (reference < from) {
            throw new NoSuchValueException();
        }
        if (reference > to) {
            return to;
        }
        final int period = every.getPeriod().getValue();
        final int remainder = (reference - from) % period;
        if (remainder == 0) {
            return reference - period;
        } else {
            return reference - remainder;
        }
    }

    private int generatePreviousValueWrapping(final int reference) throws NoSuchValueException {
        final int period = ((Every) cronField.getExpression()).getPeriod().getValue();
        final int offset = offset();
        for (int candidate = reference - 1; candidate >= from; candidate--) {
            if (matchesWrapped(candidate, offset, period)) {
                return candidate;
            }
        }
        throw new NoSuchValueException();
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(final int start, final int end) {
        final List<Integer> values = new ArrayList<>();
        try {
            if (wrapMonths) {
                int reference = generateNextValue(start);
                while (reference < end) {
                    values.add(reference);
                    reference = generateNextValue(reference);
                }
                return values;
            }
            final int offset = offset();
            if (start < offset && offset < end) {
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
        final int period = every.getPeriod().getValue();
        if (wrapMonths) {
            return value >= from && value <= to && matchesWrapped(value, offset(), period);
        }
        final int start = offset();
        return value >= start && ((value - start) % period) == 0 && value >= from && value <= to;
    }

    /**
     * Month M matches start S with period P when M is on the arithmetic progression
     * S, S±P, S±2P, … projected onto the calendar year (1–12).
     */
    private static boolean matchesWrapped(final int value, final int offset, final int period) {
        if (period <= 0) {
            return false;
        }
        return Math.floorMod(value - offset, period) == 0;
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
