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
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class AndFieldValueGeneratorTest {
    private AndFieldValueGenerator fieldValueGenerator;
    private FieldConstraints constraints;

    private static final int VALUE0 = 0;
    private static final int VALUE1 = 1;
    private static final int VALUE2 = 2;

    private static final int NOT_CONSIDERED_VALUE = 7;

    @Before
    public void setUp() {
        constraints = FieldConstraintsBuilder.instance().createConstraintsInstance();
        fieldValueGenerator =
                new AndFieldValueGenerator(
                        new CronField(
                                CronFieldName.MONTH,
                                new And()
                                        .and(new On(new IntegerFieldValue(VALUE0)))
                                        .and(new On(new IntegerFieldValue(VALUE1)))
                                        .and(new On(new IntegerFieldValue(VALUE2))),
                                constraints)
                );
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValue() throws NoSuchValueException {
        assertEquals(VALUE0, fieldValueGenerator.generateNextValue(VALUE0 - 1));
        assertEquals(VALUE1, fieldValueGenerator.generateNextValue(VALUE1 - 1));
        assertEquals(VALUE2, fieldValueGenerator.generateNextValue(VALUE2 - 1));
        fieldValueGenerator.generateNextValue(VALUE2);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValue() throws NoSuchValueException {
        assertEquals(VALUE2, fieldValueGenerator.generatePreviousValue(VALUE2 + 1));
        assertEquals(VALUE1, fieldValueGenerator.generatePreviousValue(VALUE1 + 1));
        assertEquals(VALUE0, fieldValueGenerator.generatePreviousValue(VALUE0 + 1));
        fieldValueGenerator.generatePreviousValue(VALUE0);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremes() {
        final List<Integer> candidates = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(VALUE0, VALUE2);
        assertEquals(1, candidates.size());
        assertEquals(VALUE1, candidates.get(0), 0);
    }

    @Test
    public void testIsMatch() {
        assertTrue(fieldValueGenerator.isMatch(VALUE0));
        assertTrue(fieldValueGenerator.isMatch(VALUE1));
        assertTrue(fieldValueGenerator.isMatch(VALUE2));
        assertFalse(fieldValueGenerator.isMatch(NOT_CONSIDERED_VALUE));
    }

    @Test
    public void testMatchesFieldExpressionClass() {
        assertTrue(fieldValueGenerator.matchesFieldExpressionClass(mock(And.class)));
        assertFalse(fieldValueGenerator.matchesFieldExpressionClass(mock(FieldExpression.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNotMatchesAnd() {
        new AndFieldValueGenerator(new CronField(CronFieldName.HOUR, mock(FieldExpression.class), constraints));
    }
}
