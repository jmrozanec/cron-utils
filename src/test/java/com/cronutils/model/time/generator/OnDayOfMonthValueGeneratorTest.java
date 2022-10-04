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
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class OnDayOfMonthValueGeneratorTest {
    private OnDayOfMonthValueGenerator fieldValueGenerator;
    private FieldConstraints constraints;
    private static final int YEAR = 2015;
    private static final int MONTH = 2;
    private final Random random = new Random();

    @BeforeEach
    public void setUp() {
        constraints = FieldConstraintsBuilder.instance().createConstraintsInstance();
        fieldValueGenerator =
                new OnDayOfMonthValueGenerator(
                        new CronField(
                                CronFieldName.DAY_OF_MONTH,
                                new On(new IntegerFieldValue(3)), constraints),
                        YEAR, MONTH);
    }

    @Test
    public void testGenerateNextValue() {
        assertThrows(NoSuchValueException.class, () -> fieldValueGenerator.generateNextValue(randomNumber()));
    }

    @Test
    public void testGeneratePreviousValue() {
        assertThrows(NoSuchValueException.class, () -> fieldValueGenerator.generatePreviousValue(randomNumber()));
    }

    @Test
    public void testMatchesFieldExpressionClass() {
        assertTrue(fieldValueGenerator.matchesFieldExpressionClass(mock(On.class)));
        assertFalse(fieldValueGenerator.matchesFieldExpressionClass(mock(FieldExpression.class)));
    }

    @Test
    public void testConstructorNotMatchesOn() {
        assertThrows(IllegalArgumentException.class, () -> new OnDayOfMonthValueGenerator(new CronField(CronFieldName.YEAR, mock(FieldExpression.class), constraints), YEAR, MONTH));
    }

    private int randomNumber() {
        return random.nextInt(10);
    }
}
