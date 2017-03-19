package com.cronutils.model.time.generator;

import android.support.test.runner.AndroidJUnit4;

import com.cronutils.BaseAndroidTest;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

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
@RunWith(AndroidJUnit4.class)
public class OnDayOfMonthValueGeneratorLWTest {
    private FieldConstraints constraints;
    private OnDayOfMonthValueGenerator fieldValueGenerator;
    private static final int YEAR = 2015;

    private static final int SUNDAY_VALUE_MONTH = 5;//last day in month is Sunday (weekend)
    private int sundayValueWeekday = 29;

    private int saturdayValueMonth = 2;//last day in month is Saturday (weekend)
    private int saturdayValueWeekday = 27;

    private int fridayValueMonth = 8;//last day in month is Friday (weekday)
    private int fridayValueWeekday = 31;


    private int outOfScopeValue = 31;

    @Before
    public void setUp() {
        constraints = FieldConstraintsBuilder.instance().addLWSupport().createConstraintsInstance();
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueSundayValue() throws Exception {
        testGenerateNextValue(SUNDAY_VALUE_MONTH, sundayValueWeekday);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueSaturdayValue() throws Exception {
        testGenerateNextValue(saturdayValueMonth, saturdayValueWeekday);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueFridayValue() throws Exception {
        testGenerateNextValue(fridayValueMonth, fridayValueWeekday);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueSundayValue() throws Exception {
        testGeneratePreviousValue(SUNDAY_VALUE_MONTH, sundayValueWeekday);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueSaturdayValue() throws Exception {
        testGeneratePreviousValue(saturdayValueMonth, saturdayValueWeekday);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueFridayValue() throws Exception {
        testGeneratePreviousValue(fridayValueMonth, fridayValueWeekday);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesSundayValue() throws Exception {
        testGenerateCandidatesNotIncludingIntervalExtremes(SUNDAY_VALUE_MONTH, sundayValueWeekday);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesSaturdayValue() throws Exception {
        testGenerateCandidatesNotIncludingIntervalExtremes(saturdayValueMonth, saturdayValueWeekday);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesFridayValue() throws Exception {
        testGenerateCandidatesNotIncludingIntervalExtremes(fridayValueMonth, fridayValueWeekday);
    }

    @Test
    public void testIsMatchSundayValue() throws Exception {
        testIsMatch(SUNDAY_VALUE_MONTH, sundayValueWeekday);
    }

    @Test
    public void testIsMatchSaturdayValue() throws Exception {
        testIsMatch(saturdayValueMonth, saturdayValueWeekday);
    }

    @Test
    public void testIsMatchFridayValue() throws Exception {
        testIsMatch(fridayValueMonth, fridayValueWeekday);
    }

    private void testGenerateNextValue(int month, int weekday) throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstance(month);
        assertEquals(weekday, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(outOfScopeValue);
    }

    public void testGeneratePreviousValue(int month, int weekday) throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstance(month);
        assertEquals(weekday, fieldValueGenerator.generatePreviousValue(outOfScopeValue));
        fieldValueGenerator.generatePreviousValue(1);
    }

    public void testGenerateCandidatesNotIncludingIntervalExtremes(int month, int weekday) throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstance(month);
        List<Integer> candidates = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, 32);
        assertEquals(1, candidates.size());
        assertEquals(weekday, candidates.get(0), 0);
    }

    public void testIsMatch(int month, int weekday) throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstance(month);
        assertTrue(fieldValueGenerator.isMatch(weekday));
        assertFalse(fieldValueGenerator.isMatch(weekday - 1));
    }

    private OnDayOfMonthValueGenerator createFieldValueGeneratorInstance(int month) {
        return new OnDayOfMonthValueGenerator(new CronField(CronFieldName.DAY_OF_MONTH, new On(new SpecialCharFieldValue(SpecialChar.LW)), constraints), YEAR, month);
    }
}
