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
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OnDayOfMonthValueGeneratorLWTest {
    private FieldConstraints constraints;
    private OnDayOfMonthValueGenerator fieldValueGenerator;
    private static final int YEAR = 2015;

    private static final int SUNDAY_VALUE_MONTH = 5;//last day in month is Sunday (weekend)
    private static final int SUNDAY_VALUE_WEEKDAY = 29;

    private static final int SATURDAY_VALUE_MONTH = 2;//last day in month is Saturday (weekend)
    private static final int SATURDAY_VALUE_WEEKDAY = 27;

    private static final int FRIDAY_VALUE_MONTH = 8;//last day in month is Friday (weekday)
    private static final int FRIDAY_VALUE_WEEKDAY = 31;

    private static final int OUT_OF_SCOPE_VALUE = 31;

    @Before
    public void setUp() {
        constraints = FieldConstraintsBuilder.instance().addLWSupport().createConstraintsInstance();
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueSundayValue() throws NoSuchValueException {
        testGenerateNextValue(SUNDAY_VALUE_MONTH, SUNDAY_VALUE_WEEKDAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueSaturdayValue() throws NoSuchValueException {
        testGenerateNextValue(SATURDAY_VALUE_MONTH, SATURDAY_VALUE_WEEKDAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueFridayValue() throws NoSuchValueException {
        testGenerateNextValue(FRIDAY_VALUE_MONTH, FRIDAY_VALUE_WEEKDAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueSundayValue() throws NoSuchValueException {
        testGeneratePreviousValue(SUNDAY_VALUE_MONTH, SUNDAY_VALUE_WEEKDAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueSaturdayValue() throws NoSuchValueException {
        testGeneratePreviousValue(SATURDAY_VALUE_MONTH, SATURDAY_VALUE_WEEKDAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueFridayValue() throws NoSuchValueException {
        testGeneratePreviousValue(FRIDAY_VALUE_MONTH, FRIDAY_VALUE_WEEKDAY);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesSundayValue() {
        testGenerateCandidatesNotIncludingIntervalExtremes(SUNDAY_VALUE_MONTH, SUNDAY_VALUE_WEEKDAY);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesSaturdayValue() {
        testGenerateCandidatesNotIncludingIntervalExtremes(SATURDAY_VALUE_MONTH, SATURDAY_VALUE_WEEKDAY);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesFridayValue() {
        testGenerateCandidatesNotIncludingIntervalExtremes(FRIDAY_VALUE_MONTH, FRIDAY_VALUE_WEEKDAY);
    }

    @Test
    public void testIsMatchSundayValue() {
        testIsMatch(SUNDAY_VALUE_MONTH, SUNDAY_VALUE_WEEKDAY);
    }

    @Test
    public void testIsMatchSaturdayValue() {
        testIsMatch(SATURDAY_VALUE_MONTH, SATURDAY_VALUE_WEEKDAY);
    }

    @Test
    public void testIsMatchFridayValue() {
        testIsMatch(FRIDAY_VALUE_MONTH, FRIDAY_VALUE_WEEKDAY);
    }

    private void testGenerateNextValue(final int month, final int weekday) throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstance(month);
        assertEquals(weekday, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(OUT_OF_SCOPE_VALUE);
    }

    private void testGeneratePreviousValue(final int month, final int weekday) throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstance(month);
        assertEquals(weekday, fieldValueGenerator.generatePreviousValue(OUT_OF_SCOPE_VALUE));
        fieldValueGenerator.generatePreviousValue(1);
    }

    private void testGenerateCandidatesNotIncludingIntervalExtremes(final int month, final int weekday) {
        fieldValueGenerator = createFieldValueGeneratorInstance(month);
        final List<Integer> candidates = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, 32);
        assertEquals(1, candidates.size());
        assertEquals(weekday, candidates.get(0), 0);
    }

    private void testIsMatch(final int month, final int weekday) {
        fieldValueGenerator = createFieldValueGeneratorInstance(month);
        assertTrue(fieldValueGenerator.isMatch(weekday));
        assertFalse(fieldValueGenerator.isMatch(weekday - 1));
    }

    private OnDayOfMonthValueGenerator createFieldValueGeneratorInstance(final int month) {
        return new OnDayOfMonthValueGenerator(new CronField(CronFieldName.DAY_OF_MONTH, new On(new SpecialCharFieldValue(SpecialChar.LW)), constraints), YEAR,
                month);
    }
}
