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
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class EveryFieldValueGeneratorTest {
    private FieldConstraints constraints;
    private EveryFieldValueGenerator fieldValueGenerator;

    private static final int TIME = 7;

    @BeforeEach
    public void setUp() {
        constraints = FieldConstraintsBuilder.instance().createConstraintsInstance();
        fieldValueGenerator = new EveryFieldValueGenerator(new CronField(CronFieldName.HOUR, new Every(new IntegerFieldValue(TIME)), constraints));
    }

    @Test
    public void testGenerateNextValue() throws NoSuchValueException {
        final Random random = new Random();
        for (int j = 1; j <= 10; j++) {
            final int value = TIME * j - (1 + (random.nextInt(3)));
            assertEquals(j * (long)TIME, fieldValueGenerator.generateNextValue(value));
        }
    }

    @Test
    public void testGeneratePreviousValue() throws NoSuchValueException {
        final Random random = new Random();
        for (int j = 0; j < 10; j++) {
            final int value = TIME * j + 1 + random.nextInt(3);
            assertEquals(j * (long)TIME, fieldValueGenerator.generatePreviousValue(value));
        }
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremes() {
        final int candidatesQty = 7;
        final List<Integer> candidates = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(0, TIME * candidatesQty);
        assertEquals(candidatesQty - 1L, candidates.size());
    }

    @Test
    public void testIsMatch() {
        assertTrue(fieldValueGenerator.isMatch(TIME));
        assertFalse(fieldValueGenerator.isMatch(TIME + 1));
    }

    @Test
    public void testMatchesFieldExpressionClass() {
        assertTrue(fieldValueGenerator.matchesFieldExpressionClass(mock(Every.class)));
        assertFalse(fieldValueGenerator.matchesFieldExpressionClass(mock(FieldExpression.class)));
    }

    @Test
    public void testConstructorNotMatchesEvery() {
        assertThrows(IllegalArgumentException.class, () -> new EveryFieldValueGenerator(new CronField(CronFieldName.HOUR, mock(FieldExpression.class), constraints)));
    }

    @Test
    public void testMonthIntervalWrapsYearForStartSlashPeriod() {
        // 10/3 → months Oct, Jan, Apr, Jul (issue #692)
        final FieldConstraints monthConstraints = FieldConstraintsBuilder.instance()
                .forField(CronFieldName.MONTH)
                .createConstraintsInstance();
        final Every every = new Every(new On(new IntegerFieldValue(10)), new IntegerFieldValue(3));
        final EveryFieldValueGenerator months = new EveryFieldValueGenerator(
                new CronField(CronFieldName.MONTH, every, monthConstraints));

        assertEquals(Arrays.asList(1, 4, 7, 10), months.generateCandidates(1, 12));
        assertTrue(months.isMatch(1));
        assertTrue(months.isMatch(4));
        assertTrue(months.isMatch(7));
        assertTrue(months.isMatch(10));
        assertFalse(months.isMatch(2));
        assertFalse(months.isMatch(11));
    }

    @Test
    public void testMonthIntervalNextAndPreviousWrap() throws NoSuchValueException {
        final FieldConstraints monthConstraints = FieldConstraintsBuilder.instance()
                .forField(CronFieldName.MONTH)
                .createConstraintsInstance();
        final Every every = new Every(new On(new IntegerFieldValue(10)), new IntegerFieldValue(3));
        final EveryFieldValueGenerator months = new EveryFieldValueGenerator(
                new CronField(CronFieldName.MONTH, every, monthConstraints));

        assertEquals(1, months.generateNextValue(0));
        assertEquals(4, months.generateNextValue(1));
        assertEquals(4, months.generateNextValue(2));
        assertEquals(10, months.generateNextValue(7));
        assertThrows(NoSuchValueException.class, () -> months.generateNextValue(10));

        assertEquals(10, months.generatePreviousValue(11));
        assertEquals(7, months.generatePreviousValue(10));
        assertEquals(1, months.generatePreviousValue(4));
        assertThrows(NoSuchValueException.class, () -> months.generatePreviousValue(1));
    }

    @Test
    public void testNonMonthOnIntervalDoesNotWrap() {
        // hours 22/3 must remain 22 only (no wrap into early hours)
        final FieldConstraints hourConstraints = FieldConstraintsBuilder.instance()
                .forField(CronFieldName.HOUR)
                .createConstraintsInstance();
        final Every every = new Every(new On(new IntegerFieldValue(22)), new IntegerFieldValue(3));
        final EveryFieldValueGenerator hours = new EveryFieldValueGenerator(
                new CronField(CronFieldName.HOUR, every, hourConstraints));

        assertEquals(Collections.singletonList(22), hours.generateCandidates(0, 23));
        assertTrue(hours.isMatch(22));
        assertFalse(hours.isMatch(1));
    }
}
