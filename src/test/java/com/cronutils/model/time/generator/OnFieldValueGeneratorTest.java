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
public class OnFieldValueGeneratorTest {
    private OnFieldValueGenerator fieldValueGenerator;
    private int day = 3;

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
    public void testGenerateNextValue() throws Exception {
        assertEquals(day, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(day);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValue() throws Exception {
        assertEquals(day, fieldValueGenerator.generatePreviousValue(day + 1));
        fieldValueGenerator.generatePreviousValue(day);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremes() throws Exception {
        List<Integer> candidates = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, 32);
        assertEquals(1, candidates.size());
        assertEquals(day, candidates.get(0), 0);
    }

    @Test
    public void testIsMatch() throws Exception {
        assertTrue(fieldValueGenerator.isMatch(day));
        assertFalse(fieldValueGenerator.isMatch(day - 1));
    }

    @Test
    public void testMatchesFieldExpressionClass() throws Exception {
        assertTrue(fieldValueGenerator.matchesFieldExpressionClass(mock(On.class)));
        assertFalse(fieldValueGenerator.matchesFieldExpressionClass(mock(FieldExpression.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNotMatchesOn() throws Exception {
        new OnFieldValueGenerator(mock(CronField.class));
    }
}
