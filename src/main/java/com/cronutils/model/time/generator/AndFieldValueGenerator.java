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
import com.cronutils.model.field.expression.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

class AndFieldValueGenerator extends FieldValueGenerator {
    private static final Logger log = LoggerFactory.getLogger(AndFieldValueGenerator.class);

    public AndFieldValueGenerator(final CronField cronField) {
        super(cronField);
    }

    @Override
    public int generateNextValue(final int reference) throws NoSuchValueException {
        final List<Integer> candidates = computeCandidates(
                fieldValueGenerator -> {
                    try {
                        return fieldValueGenerator.generateNextValue(reference);
                    } catch (final NoSuchValueException e) {
                        return NO_VALUE;
                    }
                }

        );
        if (candidates.isEmpty()) {
            throw new NoSuchValueException();
        } else {
            return candidates.get(0);
        }
    }

    @Override
    public int generatePreviousValue(final int reference) throws NoSuchValueException {
        final List<Integer> candidates = computeCandidates(
                candidateGenerator -> {
                    try {
                        return candidateGenerator.generatePreviousValue(reference);
                    } catch (final NoSuchValueException e) {
                        return NO_VALUE;
                    }
                }

        );
        if (candidates.isEmpty()) {
            throw new NoSuchValueException();
        } else {
            return candidates.get(candidates.size() - 1);
        }
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
        } catch (final NoSuchValueException e) {
            log.debug("Catched expected exception while generating candidates", e);
        }
        return values;
    }

    @Override
    public boolean isMatch(final int value) {
        final And and = (And) cronField.getExpression();
        boolean match = false;
        for (final FieldExpression expression : and.getExpressions()) {
            match = match || createCandidateGeneratorInstance(new CronField(cronField.getField(), expression, cronField.getConstraints())).isMatch(value);
        }
        return match;
    }

    @Override
    protected boolean matchesFieldExpressionClass(final FieldExpression fieldExpression) {
        return fieldExpression instanceof And;
    }

    private List<Integer> computeCandidates(final Function<FieldValueGenerator, Integer> function) {
        final And and = (And) cronField.getExpression();
        final List<Integer> candidates = new ArrayList<>();
        for (final FieldExpression expression : and.getExpressions()) {
            candidates.add(
                    function.apply(
                                createCandidateGeneratorInstance(new CronField(cronField.getField(), expression, cronField.getConstraints()))
                    )
            );
        }
        final List<Integer> filteredCandidates = new ArrayList<>();
        for (final Integer candidate : candidates) {
            if (candidate >= 0) {
                filteredCandidates.add(candidate);
            }
        }
        Collections.sort(filteredCandidates);
        return filteredCandidates;
    }

    private FieldValueGenerator createCandidateGeneratorInstance(final CronField cronField) {
        final FieldExpression expression = cronField.getExpression();
        if (expression instanceof Always) {
            return new AlwaysFieldValueGenerator(cronField);
        }
        if (expression instanceof Between) {
            return new BetweenFieldValueGenerator(cronField);
        }
        if (expression instanceof Every) {
            return new EveryFieldValueGenerator(cronField);
        }
        if (expression instanceof On) {
            return new OnFieldValueGenerator(cronField);
        }
        throw new IllegalArgumentException(String.format("FieldExpression %s not supported!", expression.getClass()));
    }
}
