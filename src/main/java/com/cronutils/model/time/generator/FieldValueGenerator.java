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

import java.util.Collections;
import java.util.List;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.utils.Preconditions;

/**
 * Provides a strategy to generate values. Strategy is valid for 0+ numbers
 */
public abstract class FieldValueGenerator {

    protected static final int NO_VALUE = Integer.MIN_VALUE;
    protected CronField cronField;

    public FieldValueGenerator(final CronField cronField) {
        this.cronField = Preconditions.checkNotNull(cronField, "CronField must not be null");
        Preconditions.checkArgument(matchesFieldExpressionClass(cronField.getExpression()), "FieldExpression does not match required class");
    }

    /**
     * Generates next valid value from reference.
     *
     * @param reference - reference value
     * @return generated value - Integer
     * @throws NoSuchValueException - if there is no next value
     */
    public abstract int generateNextValue(int reference) throws NoSuchValueException;

    /**
     * Generates previous valid value from reference.
     *
     * @param reference - reference value
     * @return generated value - Integer
     * @throws NoSuchValueException - if there is no previous value
     */
    public abstract int generatePreviousValue(int reference) throws NoSuchValueException;

    protected abstract List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end);

    public abstract boolean isMatch(int value);

    public final List<Integer> generateCandidates(final int start, final int end) {
        final List<Integer> candidates = generateCandidatesNotIncludingIntervalExtremes(start, end);
        if (isMatch(start)) {
            candidates.add(start);
        }
        if (isMatch(end)) {
            candidates.add(end);
        }
        Collections.sort(candidates);
        return candidates;
    }

    protected abstract boolean matchesFieldExpressionClass(FieldExpression fieldExpression);
}
