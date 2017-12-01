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

public class OnDayOfWeekValueGeneratorLTest {
    private final FieldConstraints constraints = FieldConstraintsBuilder.instance().addLSupport().createConstraintsInstance();
    private OnDayOfWeekValueGenerator fieldValueGenerator;
    private static final WeekDay mondayDoWValue = ConstantsMapper.QUARTZ_WEEK_DAY;
    private static final int YEAR = 2015;

    private static final int LAST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_MONTH = 2;
    private static final int LAST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_VALUE = 6;
    private static final int LAST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY = 27;//last Friday of month (6L) is 27

    private static final int LAST_DAY_DOW_LESS_THAN_REQUESTED_DOW_MONTH = 3;
    private static final int LAST_DAY_DOW_LESS_THAN_REQUESTED_DOW_VALUE = 6;
    private static final int LAST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY = 27;

    private static final int LAST_DAY_DOW_EQUALTO_REQUESTED_DOW_MONTH = 7;
    private static final int LAST_DAY_DOW_EQUALTO_REQUESTED_DOW_VALUE = 6;
    private static final int LAST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY = 31;

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueLastDayDoWGreaterThanRequestedDoW() throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWGreaterThanRequestedDoW();
        assertEquals(LAST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(LAST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueLastDayDoWLessThanRequestedDoW() throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        assertEquals(LAST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(LAST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueLastDayDoWEqualToRequestedDoW() throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        assertEquals(LAST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(LAST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueLastDayDoWGreaterThanRequestedDoW() throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWGreaterThanRequestedDoW();
        assertEquals(LAST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY, fieldValueGenerator.generatePreviousValue(LAST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY + 1));
        fieldValueGenerator.generatePreviousValue(LAST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueLastDayDoWLessThanRequestedDoW() throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        assertEquals(LAST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY, fieldValueGenerator.generatePreviousValue(LAST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY + 1));
        fieldValueGenerator.generatePreviousValue(LAST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueLastDayDoWEqualToRequestedDoW() throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        assertEquals(LAST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY, fieldValueGenerator.generatePreviousValue(LAST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY + 1));
        fieldValueGenerator.generatePreviousValue(LAST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesLastDayDoWGreaterThanRequestedDoW() {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWGreaterThanRequestedDoW();
        final List<Integer> values = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, LAST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY + 1);
        assertFalse(values.isEmpty());
        assertEquals(LAST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY, values.get(0), 0);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesLastDayDoWLessThanRequestedDoW() {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        final List<Integer> values = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, LAST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY + 1);
        assertFalse(values.isEmpty());
        assertEquals(LAST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY, values.get(0), 0);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesLastDayDoWEqualToRequestedDoW() {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        final List<Integer> values = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, LAST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY + 1);
        assertFalse(values.isEmpty());
        assertEquals(LAST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY, values.get(0), 0);
    }

    @Test
    public void testIsMatchLastDayDoWGreaterThanRequestedDoW() {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWGreaterThanRequestedDoW();
        assertTrue(fieldValueGenerator.isMatch(LAST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY));
        assertFalse(fieldValueGenerator.isMatch(LAST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY + 1));
    }

    @Test
    public void testIsMatchLastDayDoWLessThanRequestedDoW() {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        assertTrue(fieldValueGenerator.isMatch(LAST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY));
        assertFalse(fieldValueGenerator.isMatch(LAST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY + 1));
    }

    @Test
    public void testIsMatchLastDayDoWEqualToRequestedDoW() {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        assertTrue(fieldValueGenerator.isMatch(LAST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY));
        assertFalse(fieldValueGenerator.isMatch(LAST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY + 1));
    }

    private OnDayOfWeekValueGenerator createFieldValueGeneratorInstanceLastDayDoWGreaterThanRequestedDoW() {
        return new OnDayOfWeekValueGenerator(new CronField(CronFieldName.DAY_OF_WEEK,
                new On(new IntegerFieldValue(LAST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_VALUE), new SpecialCharFieldValue(SpecialChar.L)), constraints), YEAR,
                LAST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_MONTH, mondayDoWValue);
    }

    private OnDayOfWeekValueGenerator createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW() {
        return new OnDayOfWeekValueGenerator(new CronField(CronFieldName.DAY_OF_WEEK,
                new On(new IntegerFieldValue(LAST_DAY_DOW_LESS_THAN_REQUESTED_DOW_VALUE), new SpecialCharFieldValue(SpecialChar.L)), constraints), YEAR,
                LAST_DAY_DOW_LESS_THAN_REQUESTED_DOW_MONTH, mondayDoWValue);
    }

    private OnDayOfWeekValueGenerator createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW() {
        return new OnDayOfWeekValueGenerator(new CronField(CronFieldName.DAY_OF_WEEK,
                new On(new IntegerFieldValue(LAST_DAY_DOW_EQUALTO_REQUESTED_DOW_VALUE), new SpecialCharFieldValue(SpecialChar.L)), constraints), YEAR,
                LAST_DAY_DOW_EQUALTO_REQUESTED_DOW_MONTH, mondayDoWValue);
    }
}
