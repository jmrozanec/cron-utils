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

import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.value.IntegerFieldValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class EveryFieldValueGeneratorTest {
    private FieldConstraints constraints;
    private EveryFieldValueGenerator fieldValueGenerator;

    private static final int TIME = 7;

    @Before
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

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNotMatchesEvery() {
        new EveryFieldValueGenerator(new CronField(CronFieldName.HOUR, mock(FieldExpression.class), constraints));
    }
}
