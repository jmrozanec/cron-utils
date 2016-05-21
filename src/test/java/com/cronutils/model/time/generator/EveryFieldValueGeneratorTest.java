package com.cronutils.model.time.generator;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
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
public class EveryFieldValueGeneratorTest {
    private FieldConstraints constraints;
    private EveryFieldValueGenerator fieldValueGenerator;

    private int time = 7;

    @Before
    public void setUp(){
        constraints = FieldConstraintsBuilder.instance().createConstraintsInstance();
        fieldValueGenerator = new EveryFieldValueGenerator(new CronField(CronFieldName.HOUR, new Every(new IntegerFieldValue(time)), constraints));
    }

    @Test
    public void testGenerateNextValue() throws Exception {
        for(int j=1; j<=10; j++){
            int value = time*j-(1+((int)(2*Math.random())));
            assertEquals(j*time, fieldValueGenerator.generateNextValue(value));
        }
    }

    @Test
    public void testGeneratePreviousValue() throws Exception {
        for(int j=0; j<10; j++){
            int value = time*j+1+((int)(2*Math.random()));
            assertEquals(j*time, fieldValueGenerator.generatePreviousValue(value));
        }
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremes() throws Exception {
        int candidatesQty = 7;
        List<Integer> candidates = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(0, time*candidatesQty);
        assertEquals(candidatesQty-1, candidates.size());
    }

    @Test
    public void testIsMatch() throws Exception {
        assertTrue(fieldValueGenerator.isMatch(time));
        assertFalse(fieldValueGenerator.isMatch(time + 1));
    }

    @Test
    public void testMatchesFieldExpressionClass() throws Exception {
        assertTrue(fieldValueGenerator.matchesFieldExpressionClass(mock(Every.class)));
        assertFalse(fieldValueGenerator.matchesFieldExpressionClass(mock(FieldExpression.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNotMatchesEvery() throws Exception {
        new EveryFieldValueGenerator(new CronField(CronFieldName.HOUR, mock(FieldExpression.class), constraints));
    }
}