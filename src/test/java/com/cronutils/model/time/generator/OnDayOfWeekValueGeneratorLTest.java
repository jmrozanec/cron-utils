package com.cronutils.model.time.generator;

import java.util.List;

import org.junit.Test;

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
public class OnDayOfWeekValueGeneratorLTest {
    private FieldConstraints constraints = FieldConstraintsBuilder.instance().addLSupport().createConstraintsInstance();
    private OnDayOfWeekValueGenerator fieldValueGenerator;
    private WeekDay mondayDoWValue = ConstantsMapper.QUARTZ_WEEK_DAY;
    private int year = 2015;

    private int lastDayDoWGreaterThanRequestedDoW_Month = 2;
    private int lastDayDoWGreaterThanRequestedDoW_Value = 6;
    private int lastDayDoWGreaterThanRequestedDoW_Day = 27;//last Friday of month (6L) is 27

    private int lastDayDoWLessThanRequestedDoW_Month = 3;
    private int lastDayDoWLessThanRequestedDoW_Value = 6;
    private int lastDayDoWLessThanRequestedDoW_Day = 27;

    private int lastDayDoWEqualToRequestedDoW_Month = 7;
    private int lastDayDoWEqualToRequestedDoW_Value = 6;
    private int lastDayDoWEqualToRequestedDoW_Day = 31;

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueLastDayDoWGreaterThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWGreaterThanRequestedDoW();
        assertEquals(lastDayDoWGreaterThanRequestedDoW_Day, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(lastDayDoWGreaterThanRequestedDoW_Day);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueLastDayDoWLessThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        assertEquals(lastDayDoWLessThanRequestedDoW_Day, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(lastDayDoWLessThanRequestedDoW_Day);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueLastDayDoWEqualToRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        assertEquals(lastDayDoWEqualToRequestedDoW_Day, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(lastDayDoWEqualToRequestedDoW_Day);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueLastDayDoWGreaterThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWGreaterThanRequestedDoW();
        assertEquals(lastDayDoWGreaterThanRequestedDoW_Day, fieldValueGenerator.generatePreviousValue(lastDayDoWGreaterThanRequestedDoW_Day + 1));
        fieldValueGenerator.generatePreviousValue(lastDayDoWGreaterThanRequestedDoW_Day);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueLastDayDoWLessThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        assertEquals(lastDayDoWLessThanRequestedDoW_Day, fieldValueGenerator.generatePreviousValue(lastDayDoWLessThanRequestedDoW_Day + 1));
        fieldValueGenerator.generatePreviousValue(lastDayDoWLessThanRequestedDoW_Day);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueLastDayDoWEqualToRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        assertEquals(lastDayDoWEqualToRequestedDoW_Day, fieldValueGenerator.generatePreviousValue(lastDayDoWEqualToRequestedDoW_Day + 1));
        fieldValueGenerator.generatePreviousValue(lastDayDoWEqualToRequestedDoW_Day);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesLastDayDoWGreaterThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWGreaterThanRequestedDoW();
        List<Integer> values = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, lastDayDoWGreaterThanRequestedDoW_Day + 1);
        assertFalse(values.isEmpty());
        assertEquals(lastDayDoWGreaterThanRequestedDoW_Day, values.get(0), 0);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesLastDayDoWLessThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        List<Integer> values = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, lastDayDoWLessThanRequestedDoW_Day + 1);
        assertFalse(values.isEmpty());
        assertEquals(lastDayDoWLessThanRequestedDoW_Day, values.get(0), 0);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesLastDayDoWEqualToRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        List<Integer> values = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, lastDayDoWEqualToRequestedDoW_Day + 1);
        assertFalse(values.isEmpty());
        assertEquals(lastDayDoWEqualToRequestedDoW_Day, values.get(0), 0);
    }

    @Test
    public void testIsMatchLastDayDoWGreaterThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWGreaterThanRequestedDoW();
        assertTrue(fieldValueGenerator.isMatch(lastDayDoWGreaterThanRequestedDoW_Day));
        assertFalse(fieldValueGenerator.isMatch(lastDayDoWGreaterThanRequestedDoW_Day + 1));
    }

    @Test
    public void testIsMatchLastDayDoWLessThanRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        assertTrue(fieldValueGenerator.isMatch(lastDayDoWLessThanRequestedDoW_Day));
        assertFalse(fieldValueGenerator.isMatch(lastDayDoWLessThanRequestedDoW_Day + 1));
    }

    @Test
    public void testIsMatchLastDayDoWEqualToRequestedDoW() throws Exception {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        assertTrue(fieldValueGenerator.isMatch(lastDayDoWEqualToRequestedDoW_Day));
        assertFalse(fieldValueGenerator.isMatch(lastDayDoWEqualToRequestedDoW_Day + 1));
    }

    private OnDayOfWeekValueGenerator createFieldValueGeneratorInstanceLastDayDoWGreaterThanRequestedDoW() {
        return new OnDayOfWeekValueGenerator(new CronField(CronFieldName.DAY_OF_WEEK,
                new On(new IntegerFieldValue(lastDayDoWGreaterThanRequestedDoW_Value), new SpecialCharFieldValue(SpecialChar.L)), constraints), year,
                lastDayDoWGreaterThanRequestedDoW_Month, mondayDoWValue);
    }

    private OnDayOfWeekValueGenerator createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW() {
        return new OnDayOfWeekValueGenerator(new CronField(CronFieldName.DAY_OF_WEEK,
                new On(new IntegerFieldValue(lastDayDoWLessThanRequestedDoW_Value), new SpecialCharFieldValue(SpecialChar.L)), constraints), year,
                lastDayDoWLessThanRequestedDoW_Month, mondayDoWValue);
    }

    private OnDayOfWeekValueGenerator createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW() {
        return new OnDayOfWeekValueGenerator(new CronField(CronFieldName.DAY_OF_WEEK,
                new On(new IntegerFieldValue(lastDayDoWEqualToRequestedDoW_Value), new SpecialCharFieldValue(SpecialChar.L)), constraints), year,
                lastDayDoWEqualToRequestedDoW_Month, mondayDoWValue);
    }
}
