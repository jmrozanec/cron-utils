package com.cronutils.model.time.generator;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.expression.FieldExpression;
import org.junit.Before;
import org.junit.Test;

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
public class NullFieldValueGeneratorTest {
    private NullFieldValueGenerator fieldValueGenerator;

    @Before
    public void setUp(){
        fieldValueGenerator = new NullFieldValueGenerator(mock(CronField.class));
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValue() throws Exception {
        fieldValueGenerator.generateNextValue((int)(10*Math.random()));
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValue() throws Exception {
        fieldValueGenerator.generatePreviousValue((int) (10 * Math.random()));
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremes() throws Exception {
        assertTrue(fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(0, Integer.MAX_VALUE).isEmpty());
    }

    @Test
    public void testIsMatch() throws Exception {
        assertFalse(fieldValueGenerator.isMatch((int)(10*Math.random())));
    }

    @Test
    public void testMatchesFieldExpressionClass() throws Exception {
        assertTrue(fieldValueGenerator.matchesFieldExpressionClass(mock(FieldExpression.class)));
    }

    @Test
    public void testConstructorNotMatchesNull() throws Exception {
        assertNotNull(new NullFieldValueGenerator(mock(CronField.class)));
    }
}