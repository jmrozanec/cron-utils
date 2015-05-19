package com.cronutils.model.time.generator;

import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.value.IntegerFieldValue;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
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
public class AndFieldValueGeneratorTest {
    private AndFieldValueGenerator fieldValueGenerator;

    private int value0 = 0;
    private int value1 = 1;
    private int value2 = 2;

    private int notConsideredValue = 7;

    @Before
    public void setUp(){
        FieldConstraints constraints = FieldConstraintsBuilder.instance().createConstraintsInstance();
        fieldValueGenerator =
                new AndFieldValueGenerator(
                        new And()
                                .and(new On(constraints, new IntegerFieldValue(value0)))
                                .and(new On(constraints, new IntegerFieldValue(value1)))
                                .and(new On(constraints, new IntegerFieldValue(value2)))
                );
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValue() throws Exception {
        assertEquals(value0, fieldValueGenerator.generateNextValue(value0-1));
        assertEquals(value1, fieldValueGenerator.generateNextValue(value1-1));
        assertEquals(value2, fieldValueGenerator.generateNextValue(value2-1));
        fieldValueGenerator.generateNextValue(value2);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValue() throws Exception {
        assertEquals(value2, fieldValueGenerator.generatePreviousValue(value2+1));
        assertEquals(value1, fieldValueGenerator.generatePreviousValue(value1 + 1));
        assertEquals(value0, fieldValueGenerator.generatePreviousValue(value0 + 1));
        fieldValueGenerator.generatePreviousValue(value0);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremes() throws Exception {
        List<Integer> candidates = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(value0, value2);
        assertEquals(1, candidates.size());
        assertEquals(value1, candidates.get(0), 0);
    }

    @Test
    public void testIsMatch() throws Exception {
        assertTrue(fieldValueGenerator.isMatch(value0));
        assertTrue(fieldValueGenerator.isMatch(value1));
        assertTrue(fieldValueGenerator.isMatch(value2));
        assertFalse(fieldValueGenerator.isMatch(notConsideredValue));
    }

    @Test
    public void testMatchesFieldExpressionClass() throws Exception {
        assertTrue(fieldValueGenerator.matchesFieldExpressionClass(mock(And.class)));
        assertFalse(fieldValueGenerator.matchesFieldExpressionClass(mock(FieldExpression.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNotMatchesAnd() throws Exception {
        new AndFieldValueGenerator(mock(FieldExpression.class));
    }
}