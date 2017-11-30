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

package com.cronutils.model.time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.cronutils.utils.Preconditions;
import com.cronutils.utils.VisibleForTesting;

class TimeNode {
    protected List<Integer> values;

    public TimeNode(final List<Integer> values) {
        this.values = Preconditions.checkNotNullNorEmpty(values, "Values must not be empty");
        Collections.sort(this.values);
    }

    public NearestValue getNextValue(final int reference, final int shifts) {
        return getNearestForwardValue(reference, shifts);
    }

    public List<Integer> getValues() {
        return Collections.unmodifiableList(values);
    }

    public NearestValue getPreviousValue(final int reference, final int shifts) {
        return getNearestBackwardValue(reference, shifts);
    }

    /**
     * We return same reference value if matches or next one if does not match.
     * Then we start applying shifts.
     * This way we ensure same value is returned if no shift is requested.
     *
     * @param reference     - reference value
     * @param shiftsToApply - shifts to apply
     * @return NearestValue instance, never null. Holds information on nearest (forward) value and shifts performed.
     */
    @VisibleForTesting
    NearestValue getNearestForwardValue(final int reference, int shiftsToApply) {
        final List<Integer> temporaryValues = new ArrayList<>(this.values);
        int index = 0;
        boolean foundGreater = false;
        final AtomicInteger shift = new AtomicInteger(0);
        if (!temporaryValues.contains(reference)) {
            for (final Integer value : temporaryValues) {
                if (value > reference) {
                    index = temporaryValues.indexOf(value);
                    shiftsToApply--;//we just moved a position!
                    foundGreater = true;
                    break;
                }
            }
            if (!foundGreater) {
                shift.incrementAndGet();
            }
        } else {
            index = temporaryValues.indexOf(reference);
        }
        int value = temporaryValues.get(index);
        for (int j = 0; j < shiftsToApply; j++) {
            value = getValueFromList(temporaryValues, index + 1, shift);
            index = temporaryValues.indexOf(value);
        }
        return new NearestValue(value, shift.get());
    }

    /**
     * We return same reference value if matches or previous one if does not match.
     * Then we start applying shifts.
     * This way we ensure same value is returned if no shift is requested.
     *
     * @param reference     - reference value
     * @param shiftsToApply - shifts to apply
     * @return NearestValue instance, never null. Holds information on nearest (backward) value and shifts performed.
     */
    @VisibleForTesting
    NearestValue getNearestBackwardValue(final int reference, int shiftsToApply) {
        final List<Integer> temporaryValues = new ArrayList<>(this.values);
        Collections.reverse(temporaryValues);
        int index = 0;
        boolean foundSmaller = false;
        final AtomicInteger shift = new AtomicInteger(0);
        if (!temporaryValues.contains(reference)) {
            for (final Integer value : temporaryValues) {
                if (value < reference) {
                    index = temporaryValues.indexOf(value);
                    shiftsToApply--;//we just moved a position!
                    foundSmaller = true;
                    break;
                }
            }
            if (!foundSmaller) {
                shift.incrementAndGet();
            }
        } else {
            index = temporaryValues.indexOf(reference);
        }
        int value = temporaryValues.get(index);
        for (int j = 0; j < shiftsToApply; j++) {
            value = getValueFromList(temporaryValues, index + 1, shift);
            index = temporaryValues.indexOf(value);
        }
        return new NearestValue(value, shift.get());
    }

    /**
     * Obtain value from list considering specified index and required shifts.
     *
     * @param values - possible values
     * @param index  - index to be considered
     * @param shift  - shifts that should be applied
     * @return int - required value from values list
     */
    @VisibleForTesting
    int getValueFromList(final List<Integer> values, int index, final AtomicInteger shift) {
        Preconditions.checkNotNullNorEmpty(values, "List must not be empty");
        if (index < 0) {
            index = index + values.size();
            shift.incrementAndGet();
            return getValueFromList(values, index, shift);
        }
        if (index >= values.size()) {
            index = index - values.size();
            shift.incrementAndGet();
            return getValueFromList(values, index, shift);
        }
        return values.get(index);
    }
}
