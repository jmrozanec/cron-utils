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
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OnDayOfMonthValueGeneratorWTest {
    private FieldConstraints constraints;
    private OnDayOfMonthValueGenerator fieldValueGenerator;
    private static final int YEAR = 2015;
    private static final int SUNDAY_VALUE_MONTH = 2;
    private static final int SUNDAY_VALUE_DAY = 15;
    private static final int SUNDAY_VALUE_WEEKDAY = 16;

    private static final int SATURDAY_VALUE_MONTH = 2;
    private static final int SATURDAY_VALUE_DAY = 7;
    private static final int SATURDAY_VALUE_WEEKDAY = 6;

    private static final int FIRST_DAY_SATURDAY_VALUE_MONTH = 8;
    private static final int FIRST_DAY_SATURDAY_VALUE_DAY = 1;
    private static final int FIRST_DAY_SATURDAY_VALUE_WEEKDAY = 3;

    private static final int OUT_OF_SCOPE_VALUE = 18;

    @BeforeEach
    public void setUp() {
        constraints = FieldConstraintsBuilder.instance().addWSupport().createConstraintsInstance();
    }

    @Test
    public void testGenerateNextValueSundayValue() {
        assertThrows(NoSuchValueException.class, () -> testGenerateNextValue(SUNDAY_VALUE_MONTH, SUNDAY_VALUE_DAY, SUNDAY_VALUE_WEEKDAY));
    }

    @Test
    public void testGenerateNextValueSaturdayValue() {
        assertThrows(NoSuchValueException.class, () -> testGenerateNextValue(SATURDAY_VALUE_MONTH, SATURDAY_VALUE_DAY, SATURDAY_VALUE_WEEKDAY));
    }

    @Test
    public void testGenerateNextValueFirstDaySaturdayValue() {
        assertThrows(NoSuchValueException.class, () -> testGenerateNextValue(FIRST_DAY_SATURDAY_VALUE_MONTH, FIRST_DAY_SATURDAY_VALUE_DAY, FIRST_DAY_SATURDAY_VALUE_WEEKDAY));
    }

    @Test
    public void testGeneratePreviousValueSundayValue() {
        assertThrows(NoSuchValueException.class, () -> testGeneratePreviousValue(SUNDAY_VALUE_MONTH, SUNDAY_VALUE_DAY, SUNDAY_VALUE_WEEKDAY));
    }

    @Test
    public void testGeneratePreviousValueSaturdayValue() {
        assertThrows(NoSuchValueException.class, () -> testGeneratePreviousValue(SATURDAY_VALUE_MONTH, SATURDAY_VALUE_DAY, SATURDAY_VALUE_WEEKDAY));
    }

    @Test
    public void testGeneratePreviousValueFirstDaySaturdayValue() {
        assertThrows(NoSuchValueException.class, () -> testGeneratePreviousValue(FIRST_DAY_SATURDAY_VALUE_MONTH, FIRST_DAY_SATURDAY_VALUE_DAY, FIRST_DAY_SATURDAY_VALUE_WEEKDAY));
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesSundayValue() {
        testGenerateCandidatesNotIncludingIntervalExtremes(SUNDAY_VALUE_MONTH, SUNDAY_VALUE_DAY, SUNDAY_VALUE_WEEKDAY);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesSaturdayValue() {
        testGenerateCandidatesNotIncludingIntervalExtremes(SATURDAY_VALUE_MONTH, SATURDAY_VALUE_DAY, SATURDAY_VALUE_WEEKDAY);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesFirstDaySaturdayValue() {
        testGenerateCandidatesNotIncludingIntervalExtremes(FIRST_DAY_SATURDAY_VALUE_MONTH, FIRST_DAY_SATURDAY_VALUE_DAY, FIRST_DAY_SATURDAY_VALUE_WEEKDAY);
    }

    @Test
    public void testIsMatchSundayValue() {
        testIsMatch(SUNDAY_VALUE_MONTH, SUNDAY_VALUE_DAY, SUNDAY_VALUE_WEEKDAY);
    }

    @Test
    public void testIsMatchSaturdayValue() {
        testIsMatch(SATURDAY_VALUE_MONTH, SATURDAY_VALUE_DAY, SATURDAY_VALUE_WEEKDAY);
    }

    @Test
    public void testIsMatchFirstDaySaturdayValue() {
        testIsMatch(FIRST_DAY_SATURDAY_VALUE_MONTH, FIRST_DAY_SATURDAY_VALUE_DAY, FIRST_DAY_SATURDAY_VALUE_WEEKDAY);
    }

    private void testGenerateNextValue(final int month, final int day, final int weekday) throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstance(month, day);
        assertEquals(weekday, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(OUT_OF_SCOPE_VALUE);
    }

    private void testGeneratePreviousValue(final int month, final int day, final int weekday) throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstance(month, day);
        assertEquals(weekday, fieldValueGenerator.generatePreviousValue(OUT_OF_SCOPE_VALUE));
        fieldValueGenerator.generatePreviousValue(1);
    }

    private void testGenerateCandidatesNotIncludingIntervalExtremes(final int month, final int day, final int weekday) {
        fieldValueGenerator = createFieldValueGeneratorInstance(month, day);
        final List<Integer> candidates = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, 32);
        assertEquals(1, candidates.size());
        assertEquals(weekday, candidates.get(0), 0);
    }

    private void testIsMatch(final int month, final int day, final int weekday) {
        fieldValueGenerator = createFieldValueGeneratorInstance(month, day);
        assertTrue(fieldValueGenerator.isMatch(weekday));
        assertFalse(fieldValueGenerator.isMatch(weekday - 1));
    }

    private OnDayOfMonthValueGenerator createFieldValueGeneratorInstance(final int month, final int day) {
        return new OnDayOfMonthValueGenerator(
                new CronField(
                        CronFieldName.DAY_OF_MONTH,
                        new On(
                                new IntegerFieldValue(day),
                                new SpecialCharFieldValue(SpecialChar.W), new IntegerFieldValue(-1)
                        ),
                        constraints
                ), YEAR, month);
    }
}
