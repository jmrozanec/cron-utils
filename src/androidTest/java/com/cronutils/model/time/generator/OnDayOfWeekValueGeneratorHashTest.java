package com.cronutils.model.time.generator;

import android.support.test.runner.AndroidJUnit4;

import com.cronutils.BaseAndroidTest;
import com.cronutils.mapper.ConstantsMapper;
import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
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
public class OnDayOfWeekValueGeneratorHashTest {
    FieldConstraints constraints = FieldConstraintsBuilder.instance().addHashSupport().createConstraintsInstance();
    private OnDayOfWeekValueGenerator fieldValueGenerator;
    private WeekDay mondayDoWValue = ConstantsMapper.QUARTZ_WEEK_DAY;
    private int year = 2015;

    private int firstDayDoWGreaterThanRequestedDoW_Month = 5;
    private int firstDayDoWGreaterThanRequestedDoW_TimeValue = 2;
    private int firstDayDoWGreaterThanRequestedDoW_HashValue = 4;
    private int firstDayDoWGreaterThanRequestedDoW_Day = 25;//4th Monday of month (2#4) is 25

    private int firstDayDoWLessThanRequestedDoW_Month = 2;
    private int firstDayDoWLessThanRequestedDoW_TimeValue = 6;
    private int firstDayDoWLessThanRequestedDoW_HashValue = 4;
    private int firstDayDoWLessThanRequestedDoW_Day = 27;//4th Friday of month (6#4) is 27

    private int firstDayDoWEqualToRequestedDoW_Month = 2;
    private int firstDayDoWEqualToRequestedDoW_TimeValue = 1;
    private int firstDayDoWEqualToRequestedDoW_HashValue = 3;
    private int firstDayDoWEqualToRequestedDoW_Day = 15;//3rd Sunday of month (1#3) is 15

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueLastDayDoWGreaterThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceFirstDayDoWGreaterThanRequestedDoW();
        assertEquals(firstDayDoWGreaterThanRequestedDoW_Day, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(firstDayDoWGreaterThanRequestedDoW_Day);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueLastDayDoWLessThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        assertEquals(firstDayDoWLessThanRequestedDoW_Day, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(firstDayDoWLessThanRequestedDoW_Day);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueLastDayDoWEqualToRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        assertEquals(firstDayDoWEqualToRequestedDoW_Day, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(firstDayDoWEqualToRequestedDoW_Day);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueLastDayDoWGreaterThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceFirstDayDoWGreaterThanRequestedDoW();
        assertEquals(firstDayDoWGreaterThanRequestedDoW_Day, fieldValueGenerator.generatePreviousValue(firstDayDoWGreaterThanRequestedDoW_Day + 1));
        fieldValueGenerator.generatePreviousValue(firstDayDoWGreaterThanRequestedDoW_Day);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueLastDayDoWLessThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        assertEquals(firstDayDoWLessThanRequestedDoW_Day, fieldValueGenerator.generatePreviousValue(firstDayDoWLessThanRequestedDoW_Day + 1));
        fieldValueGenerator.generatePreviousValue(firstDayDoWLessThanRequestedDoW_Day);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueLastDayDoWEqualToRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        assertEquals(firstDayDoWEqualToRequestedDoW_Day, fieldValueGenerator.generatePreviousValue(firstDayDoWEqualToRequestedDoW_Day + 1));
        fieldValueGenerator.generatePreviousValue(firstDayDoWEqualToRequestedDoW_Day);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesLastDayDoWGreaterThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceFirstDayDoWGreaterThanRequestedDoW();
        List<Integer> values = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, firstDayDoWGreaterThanRequestedDoW_Day +1);
        assertFalse(values.isEmpty());
        assertEquals(firstDayDoWGreaterThanRequestedDoW_Day, values.get(0), 0);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesLastDayDoWLessThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        List<Integer> values = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, firstDayDoWLessThanRequestedDoW_Day +1);
        assertFalse(values.isEmpty());
        assertEquals(firstDayDoWLessThanRequestedDoW_Day, values.get(0), 0);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesLastDayDoWEqualToRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        List<Integer> values = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, firstDayDoWEqualToRequestedDoW_Day +1);
        assertFalse(values.isEmpty());
        assertEquals(firstDayDoWEqualToRequestedDoW_Day, values.get(0), 0);
    }

    @Test
    public void testIsMatchLastDayDoWGreaterThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceFirstDayDoWGreaterThanRequestedDoW();
        assertTrue(fieldValueGenerator.isMatch(firstDayDoWGreaterThanRequestedDoW_Day));
        assertFalse(fieldValueGenerator.isMatch(firstDayDoWGreaterThanRequestedDoW_Day +1));
    }

    @Test
    public void testIsMatchLastDayDoWLessThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        assertTrue(fieldValueGenerator.isMatch(firstDayDoWLessThanRequestedDoW_Day));
        assertFalse(fieldValueGenerator.isMatch(firstDayDoWLessThanRequestedDoW_Day +1));
    }

    @Test
    public void testIsMatchLastDayDoWEqualToRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        assertTrue(fieldValueGenerator.isMatch(firstDayDoWEqualToRequestedDoW_Day));
        assertFalse(fieldValueGenerator.isMatch(firstDayDoWEqualToRequestedDoW_Day +1));
    }

    private OnDayOfWeekValueGenerator createFieldValueGeneratorInstanceFirstDayDoWGreaterThanRequestedDoW() {
        return new OnDayOfWeekValueGenerator(
                new CronField(
                        CronFieldName.DAY_OF_WEEK,
                        new On(new IntegerFieldValue(firstDayDoWGreaterThanRequestedDoW_TimeValue),
                                new SpecialCharFieldValue(SpecialChar.HASH), new IntegerFieldValue(firstDayDoWGreaterThanRequestedDoW_HashValue)
                        ),
                        constraints),
                year, firstDayDoWGreaterThanRequestedDoW_Month, mondayDoWValue
        );
    }

    private OnDayOfWeekValueGenerator createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW() {
        return new OnDayOfWeekValueGenerator(
                new CronField(
                        CronFieldName.DAY_OF_WEEK,
                        new On(new IntegerFieldValue(firstDayDoWLessThanRequestedDoW_TimeValue),
                                new SpecialCharFieldValue(SpecialChar.HASH), new IntegerFieldValue(firstDayDoWLessThanRequestedDoW_HashValue)),
                        constraints),
                year, firstDayDoWLessThanRequestedDoW_Month, mondayDoWValue
        );
    }

    private OnDayOfWeekValueGenerator createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW() {
        return new OnDayOfWeekValueGenerator(
                new CronField(
                        CronFieldName.DAY_OF_WEEK,
                        new On(
                                new IntegerFieldValue(firstDayDoWEqualToRequestedDoW_TimeValue),
                                new SpecialCharFieldValue(SpecialChar.HASH),
                                new IntegerFieldValue(firstDayDoWEqualToRequestedDoW_HashValue)
                        ), constraints),
                year, firstDayDoWEqualToRequestedDoW_Month, mondayDoWValue
        );
    }
}
