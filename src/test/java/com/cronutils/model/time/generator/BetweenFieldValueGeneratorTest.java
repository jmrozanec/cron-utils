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
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.value.IntegerFieldValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class BetweenFieldValueGeneratorTest {
    private BetweenFieldValueGenerator fieldValueGenerator;
    private FieldConstraints constraints;
    private static final int FROM = 0;
    private static final int TO = 2;
    private static final int OUT_OF_RANGE = 7;

    @BeforeEach
    public void setUp() {
        constraints = FieldConstraintsBuilder.instance().createConstraintsInstance();
        fieldValueGenerator = new BetweenFieldValueGenerator(
                new CronField(CronFieldName.HOUR, new Between(new IntegerFieldValue(FROM), new IntegerFieldValue(TO)), constraints));
    }

    @Test
    public void testGenerateNextValue() throws NoSuchValueException {
        for (int j = FROM - 1; j < TO; j++) {
            assertEquals(j + 1L, fieldValueGenerator.generateNextValue(j));
        }
        assertThrows(NoSuchValueException.class, () -> fieldValueGenerator.generateNextValue(TO));
    }

    @Test
    public void testGeneratePreviousValue() throws NoSuchValueException {
        for (int j = TO + 1; j > FROM; j--) {
            assertEquals(j - 1L, fieldValueGenerator.generatePreviousValue(j));
        }
        assertThrows(NoSuchValueException.class, () -> fieldValueGenerator.generatePreviousValue(FROM));
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesFullInterval() {
        final List<Integer> candidates = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(FROM, TO);
        assertEquals(1, candidates.size());
        assertEquals(1, candidates.get(0), 0);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalRangesNotIntersectInterval() {
        assertTrue(fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(TO + 1, TO + 10).isEmpty());
    }

    @Test
    public void testIsMatch() {
        assertTrue(fieldValueGenerator.isMatch(FROM));
        assertTrue(fieldValueGenerator.isMatch(TO));
        assertFalse(fieldValueGenerator.isMatch(OUT_OF_RANGE));
    }

    @Test
    public void testMatchesFieldExpressionClass() {
        assertTrue(fieldValueGenerator.matchesFieldExpressionClass(mock(Between.class)));
        assertFalse(fieldValueGenerator.matchesFieldExpressionClass(mock(FieldExpression.class)));
    }

    @Test
    public void testConstructorNotMatchesBetween() {
        assertThrows(IllegalArgumentException.class, () -> new BetweenFieldValueGenerator(new CronField(CronFieldName.HOUR, mock(FieldExpression.class), constraints)));
    }

    @ParameterizedTest
    @CsvSource({"HOUR, 22, 2, 0, 23", "MONTH, 11, 2, 1, 12"})
    public void testGenerateValuesForOverflowingRange(CronFieldName field, int from, int to, int minimum, int maximum)
            throws NoSuchValueException {
        final BetweenFieldValueGenerator generator = rangeGenerator(field, from, to);

        assertEquals(minimum, generator.generateNextValue(minimum - 1));
        assertEquals(to, generator.generateNextValue(to - 1));
        assertEquals(from, generator.generateNextValue(to));
        assertEquals(maximum, generator.generateNextValue(maximum - 1));
        assertThrows(NoSuchValueException.class, () -> generator.generateNextValue(maximum));

        assertEquals(maximum, generator.generatePreviousValue(maximum + 1));
        assertEquals(from, generator.generatePreviousValue(from + 1));
        assertEquals(to, generator.generatePreviousValue(from));
        assertEquals(minimum, generator.generatePreviousValue(minimum + 1));
        assertThrows(NoSuchValueException.class, () -> generator.generatePreviousValue(minimum));
    }

    @ParameterizedTest
    @CsvSource({"HOUR, 22, 2, 0, 23", "MONTH, 11, 2, 1, 12"})
    public void testIsMatchForOverflowingRange(CronFieldName field, int from, int to, int minimum, int maximum) {
        final BetweenFieldValueGenerator generator = rangeGenerator(field, from, to);

        assertTrue(generator.isMatch(minimum));
        assertTrue(generator.isMatch(maximum));
        assertTrue(generator.isMatch(from));
        assertTrue(generator.isMatch(to));
        assertFalse(generator.isMatch(from - 1));
        assertFalse(generator.isMatch(to + 1));
        assertFalse(generator.isMatch(minimum - 1));
        assertFalse(generator.isMatch(maximum + 1));
    }

    @Test
    public void testGenerateCandidatesForOverflowingHourRange() {
        final BetweenFieldValueGenerator generator = rangeGenerator(CronFieldName.HOUR, 22, 2);

        assertEquals(List.of(0, 1, 2, 22, 23), generator.generateCandidates(0, 23));
        assertEquals(List.of(1, 2, 22), generator.generateCandidates(1, 22));
        assertTrue(generator.generateCandidates(3, 21).isEmpty());
    }

    @Test
    public void testGenerateCandidatesForOverflowingMonthRange() {
        final BetweenFieldValueGenerator generator = rangeGenerator(CronFieldName.MONTH, 11, 2);

        assertEquals(List.of(1, 2, 11, 12), generator.generateCandidates(0, 13));
    }

    private BetweenFieldValueGenerator rangeGenerator(CronFieldName field, int from, int to) {
        return new BetweenFieldValueGenerator(new CronField(field,
                new Between(new IntegerFieldValue(from), new IntegerFieldValue(to)),
                FieldConstraintsBuilder.instance().forField(field).createConstraintsInstance()));
    }
}
