package com.cronutils.model.time.generator;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
public class OnDayOfMonthValueGeneratorWTest {
    private FieldConstraints constraints;
    private OnDayOfMonthValueGenerator fieldValueGenerator;
    private static final int YEAR = 2015;
    private static final int SUNDAY_VALUE_MONTH = 2;
    private int sundayValueDay = 15;
    private int sundayValueWeekday = 16;

    private int saturdayValueMonth = 2;
    private int saturdayValueDay = 7;
    private int saturdayValueWeekday = 6;

    private int firstDaySaturdayValueMonth = 8;
    private int firstDaySaturdayValueDay = 1;
    private int firstDaySaturdayValueWeekday = 3;

    private int outOfScopeValue = 18;

    @Before
    public void setUp() {
        constraints = FieldConstraintsBuilder.instance().addWSupport().createConstraintsInstance();
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueSundayValue() throws Exception {
        testGenerateNextValue(SUNDAY_VALUE_MONTH, sundayValueDay, sundayValueWeekday);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueSaturdayValue() throws Exception {
        testGenerateNextValue(saturdayValueMonth, saturdayValueDay, saturdayValueWeekday);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueFirstDaySaturdayValue() throws Exception {
        testGenerateNextValue(firstDaySaturdayValueMonth, firstDaySaturdayValueDay, firstDaySaturdayValueWeekday);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueSundayValue() throws Exception {
        testGeneratePreviousValue(SUNDAY_VALUE_MONTH, sundayValueDay, sundayValueWeekday);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueSaturdayValue() throws Exception {
        testGeneratePreviousValue(saturdayValueMonth, saturdayValueDay, saturdayValueWeekday);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueFirstDaySaturdayValue() throws Exception {
        testGeneratePreviousValue(firstDaySaturdayValueMonth, firstDaySaturdayValueDay, firstDaySaturdayValueWeekday);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesSundayValue() throws Exception {
        testGenerateCandidatesNotIncludingIntervalExtremes(SUNDAY_VALUE_MONTH, sundayValueDay, sundayValueWeekday);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesSaturdayValue() throws Exception {
        testGenerateCandidatesNotIncludingIntervalExtremes(saturdayValueMonth, saturdayValueDay, saturdayValueWeekday);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesFirstDaySaturdayValue() throws Exception {
        testGenerateCandidatesNotIncludingIntervalExtremes(firstDaySaturdayValueMonth, firstDaySaturdayValueDay, firstDaySaturdayValueWeekday);
    }

    @Test
    public void testIsMatchSundayValue() throws Exception {
        testIsMatch(SUNDAY_VALUE_MONTH, sundayValueDay, sundayValueWeekday);
    }

    @Test
    public void testIsMatchSaturdayValue() throws Exception {
        testIsMatch(saturdayValueMonth, saturdayValueDay, saturdayValueWeekday);
    }

    @Test
    public void testIsMatchFirstDaySaturdayValue() throws Exception {
        testIsMatch(firstDaySaturdayValueMonth, firstDaySaturdayValueDay, firstDaySaturdayValueWeekday);
    }

    private void testGenerateNextValue(int month, int day, int weekday) throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstance(month, day);
        assertEquals(weekday, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(outOfScopeValue);
    }

    public void testGeneratePreviousValue(int month, int day, int weekday) throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstance(month, day);
        assertEquals(weekday, fieldValueGenerator.generatePreviousValue(outOfScopeValue));
        fieldValueGenerator.generatePreviousValue(1);
    }

    public void testGenerateCandidatesNotIncludingIntervalExtremes(int month, int day, int weekday) throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstance(month, day);
        List<Integer> candidates = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, 32);
        assertEquals(1, candidates.size());
        assertEquals(weekday, candidates.get(0), 0);
    }

    public void testIsMatch(int month, int day, int weekday) throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstance(month, day);
        assertTrue(fieldValueGenerator.isMatch(weekday));
        assertFalse(fieldValueGenerator.isMatch(weekday - 1));
    }

    private OnDayOfMonthValueGenerator createFieldValueGeneratorInstance(int month, int day) {
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
