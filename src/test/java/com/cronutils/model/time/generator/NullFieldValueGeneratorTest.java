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
import com.cronutils.model.field.expression.FieldExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class NullFieldValueGeneratorTest {
    private NullFieldValueGenerator fieldValueGenerator;

    @BeforeEach
    public void setUp() {
        fieldValueGenerator = new NullFieldValueGenerator(mock(CronField.class));
    }

    @Test
    public void testGenerateNextValue() {
        final Random random = new Random();
        assertThrows(NoSuchValueException.class, () -> fieldValueGenerator.generateNextValue(random.nextInt(10)));
    }

    @Test
    public void testGeneratePreviousValue() {
        final Random random = new Random();
        assertThrows(NoSuchValueException.class, () -> fieldValueGenerator.generatePreviousValue(random.nextInt(10)));
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
