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
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class OnFieldValueGeneratorTest {
    private OnFieldValueGenerator fieldValueGenerator;
    private static final int DAY = 3;

    @Before
    public void setUp() {
        fieldValueGenerator =
                new OnFieldValueGenerator(
                        new CronField(
                                CronFieldName.HOUR,
                                new On(new IntegerFieldValue(3)),
                                FieldConstraintsBuilder.instance().addLSupport().createConstraintsInstance()
                        )
                );
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValue() throws NoSuchValueException {
        assertEquals(DAY, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(DAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValue() throws NoSuchValueException {
        assertEquals(DAY, fieldValueGenerator.generatePreviousValue(DAY + 1));
        fieldValueGenerator.generatePreviousValue(DAY);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremes() {
        final List<Integer> candidates = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, 32);
        assertEquals(1, candidates.size());
        assertEquals(DAY, candidates.get(0), 0);
    }

    @Test
    public void testIsMatch() {
        assertTrue(fieldValueGenerator.isMatch(DAY));
        assertFalse(fieldValueGenerator.isMatch(DAY - 1));
    }

    @Test
    public void testMatchesFieldExpressionClass() {
        assertTrue(fieldValueGenerator.matchesFieldExpressionClass(mock(On.class)));
        assertFalse(fieldValueGenerator.matchesFieldExpressionClass(mock(FieldExpression.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNotMatchesOn() {
        new OnFieldValueGenerator(mock(CronField.class));
    }
}
