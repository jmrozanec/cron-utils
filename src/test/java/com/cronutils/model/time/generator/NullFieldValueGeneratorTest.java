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

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.expression.FieldExpression;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class NullFieldValueGeneratorTest {
    private NullFieldValueGenerator fieldValueGenerator;

    @Before
    public void setUp() {
        fieldValueGenerator = new NullFieldValueGenerator(mock(CronField.class));
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValue() throws NoSuchValueException {
        final Random random = new Random();
        fieldValueGenerator.generateNextValue(random.nextInt(10));
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValue() throws NoSuchValueException {
        final Random random = new Random();
        fieldValueGenerator.generatePreviousValue(random.nextInt(10));
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremes() {
        assertTrue(fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(0, Integer.MAX_VALUE).isEmpty());
    }

    @Test
    public void testIsMatch() {
        final Random random = new Random();
        assertFalse(fieldValueGenerator.isMatch(random.nextInt(10)));
    }

    @Test
    public void testMatchesFieldExpressionClass() {
        assertTrue(fieldValueGenerator.matchesFieldExpressionClass(mock(FieldExpression.class)));
    }

    @Test
    public void testConstructorNotMatchesNull() {
        assertNotNull(new NullFieldValueGenerator(mock(CronField.class)));
    }
}
