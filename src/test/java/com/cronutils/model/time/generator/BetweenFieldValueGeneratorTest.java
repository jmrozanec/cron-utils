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

import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.value.IntegerFieldValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class BetweenFieldValueGeneratorTest {
    private BetweenFieldValueGenerator fieldValueGenerator;
    private FieldConstraints constraints;
    private static final int FROM = 0;
    private static final int TO = 2;
    private static final int OUT_OF_RANGE = 7;

    @Before
    public void setUp() {
        constraints = FieldConstraintsBuilder.instance().createConstraintsInstance();
        fieldValueGenerator = new BetweenFieldValueGenerator(
                new CronField(CronFieldName.HOUR, new Between(new IntegerFieldValue(FROM), new IntegerFieldValue(TO)), constraints));
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValue() throws NoSuchValueException {
        for (int j = FROM - 1; j < (TO + 1); j++) {
            assertEquals(j + 1L, fieldValueGenerator.generateNextValue(j));
        }
        fieldValueGenerator.generateNextValue(TO);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValue() throws NoSuchValueException {
        for (int j = TO + 1; j > (FROM - 1); j--) {
            assertEquals(j - 1L, fieldValueGenerator.generatePreviousValue(j));
        }
        fieldValueGenerator.generatePreviousValue(FROM);
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

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNotMatchesBetween() {
        new BetweenFieldValueGenerator(new CronField(CronFieldName.HOUR, mock(FieldExpression.class), constraints));
    }
}
