package com.cronutils.model.time.generator;

import com.cronutils.model.field.FieldExpression;
import org.apache.commons.lang3.Validate;

import java.util.List;
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

/**
 * Provides a strategy to generate values.
 * Strategy is valid for 0+ numbers
 */
public abstract class FieldValueGenerator {
    protected static int NO_VALUE = Integer.MIN_VALUE;
    protected FieldExpression expression;

    public FieldValueGenerator(FieldExpression expression){
        Validate.notNull(expression);
        Validate.isTrue(matchesFieldExpressionClass(expression), "FieldExpression does not match required class");
        this.expression = expression;
    }

    /**
     * Generates next valid value from reference
     * @param reference - reference value
     * @return generated value - Integer
     * @throws NoSuchValueException - if there is no next value
     */
    public abstract int generateNextValue(int reference) throws NoSuchValueException;

    /**
     * Generates previous valid value from reference
     * @param reference - reference value
     * @return generated value - Integer
     * @throws NoSuchValueException - if there is no previous value
     */
    public abstract int generatePreviousValue(int reference) throws NoSuchValueException;
    protected abstract List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end);
    public abstract boolean isMatch(int value);

    public final List<Integer> generateCandidates(int start, int end){
        List<Integer> candidates = generateCandidatesNotIncludingIntervalExtremes(start, end);
        if(isMatch(start)){
            candidates.add(start);
        }
        if(isMatch(end)){
            candidates.add(end);
        }
        return candidates;
    }

    protected abstract boolean matchesFieldExpressionClass(FieldExpression fieldExpression);
}