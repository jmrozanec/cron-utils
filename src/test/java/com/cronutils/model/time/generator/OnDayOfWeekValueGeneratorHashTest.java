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

public class OnDayOfWeekValueGeneratorHashTest {
    FieldConstraints constraints = FieldConstraintsBuilder.instance().addHashSupport().createConstraintsInstance();
    private OnDayOfWeekValueGenerator fieldValueGenerator;
    private static final WeekDay MONDAY_DOW_VALUE = ConstantsMapper.QUARTZ_WEEK_DAY;
    private static final int YEAR = 2015;

    private static final int FIRST_DAY_DOW_GREATER_THAT_REQUESTED_DOW_MONTH = 5;
    private static final int FIRST_DAY_DOW_GREATER_THAT_REQUESTED_DOW_TIMEVALUE = 2;
    private static final int FIRST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_HASHVALUE = 4;
    private static final int FIRST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY = 25;//4th Monday of month (2#4) is 25

    private static final int FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_MONTH = 2;
    private static final int FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_TIMEVALUE = 6;
    private static final int FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_HASHVALUE = 4;
    private static final int FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY = 27;//4th Friday of month (6#4) is 27

    private static final int FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_MONTH = 2;
    private static final int FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_TIMEVALUE = 1;
    private static final int FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_HASHVALUE = 3;
    private static final int FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY = 15;//3rd Sunday of month (1#3) is 15

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueLastDayDoWGreaterThanRequestedDoW() throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstanceFirstDayDoWGreaterThanRequestedDoW();
        assertEquals(FIRST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(FIRST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueLastDayDoWLessThanRequestedDoW() throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        assertEquals(FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValueLastDayDoWEqualToRequestedDoW() throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        assertEquals(FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueLastDayDoWGreaterThanRequestedDoW() throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstanceFirstDayDoWGreaterThanRequestedDoW();
        assertEquals(FIRST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY, fieldValueGenerator.generatePreviousValue(FIRST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY + 1));
        fieldValueGenerator.generatePreviousValue(FIRST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueLastDayDoWLessThanRequestedDoW() throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        assertEquals(FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY, fieldValueGenerator.generatePreviousValue(FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY + 1));
        fieldValueGenerator.generatePreviousValue(FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValueLastDayDoWEqualToRequestedDoW() throws NoSuchValueException {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        assertEquals(FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY, fieldValueGenerator.generatePreviousValue(FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY + 1));
        fieldValueGenerator.generatePreviousValue(FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesLastDayDoWGreaterThanRequestedDoW() {
        fieldValueGenerator = createFieldValueGeneratorInstanceFirstDayDoWGreaterThanRequestedDoW();
        final List<Integer> values = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, FIRST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY + 1);
        assertFalse(values.isEmpty());
        assertEquals(FIRST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY, values.get(0), 0);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesLastDayDoWLessThanRequestedDoW() {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        final List<Integer> values = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY + 1);
        assertFalse(values.isEmpty());
        assertEquals(FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY, values.get(0), 0);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremesLastDayDoWEqualToRequestedDoW() {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        final List<Integer> values = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY + 1);
        assertFalse(values.isEmpty());
        assertEquals(FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY, values.get(0), 0);
    }

    @Test
    public void testIsMatchLastDayDoWGreaterThanRequestedDoW() {
        fieldValueGenerator = createFieldValueGeneratorInstanceFirstDayDoWGreaterThanRequestedDoW();
        assertTrue(fieldValueGenerator.isMatch(FIRST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY));
        assertFalse(fieldValueGenerator.isMatch(FIRST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_DAY + 1));
    }

    @Test
    public void testIsMatchLastDayDoWLessThanRequestedDoW() {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW();
        assertTrue(fieldValueGenerator.isMatch(FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY));
        assertFalse(fieldValueGenerator.isMatch(FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_DAY + 1));
    }

    @Test
    public void testIsMatchLastDayDoWEqualToRequestedDoW() {
        fieldValueGenerator = createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW();
        assertTrue(fieldValueGenerator.isMatch(FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY));
        assertFalse(fieldValueGenerator.isMatch(FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_DAY + 1));
    }

    private OnDayOfWeekValueGenerator createFieldValueGeneratorInstanceFirstDayDoWGreaterThanRequestedDoW() {
        return new OnDayOfWeekValueGenerator(
                new CronField(
                        CronFieldName.DAY_OF_WEEK,
                        new On(new IntegerFieldValue(FIRST_DAY_DOW_GREATER_THAT_REQUESTED_DOW_TIMEVALUE),
                                new SpecialCharFieldValue(SpecialChar.HASH), new IntegerFieldValue(FIRST_DAY_DOW_GREATER_THAN_REQUESTED_DOW_HASHVALUE)
                        ),
                        constraints),
                YEAR, FIRST_DAY_DOW_GREATER_THAT_REQUESTED_DOW_MONTH, MONDAY_DOW_VALUE
        );
    }

    private OnDayOfWeekValueGenerator createFieldValueGeneratorInstanceLastDayDoWLessThanRequestedDoW() {
        return new OnDayOfWeekValueGenerator(
                new CronField(
                        CronFieldName.DAY_OF_WEEK,
                        new On(new IntegerFieldValue(FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_TIMEVALUE),
                                new SpecialCharFieldValue(SpecialChar.HASH), new IntegerFieldValue(FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_HASHVALUE)),
                        constraints),
                YEAR, FIRST_DAY_DOW_LESS_THAN_REQUESTED_DOW_MONTH, MONDAY_DOW_VALUE
        );
    }

    private OnDayOfWeekValueGenerator createFieldValueGeneratorInstanceLastDayDoWEqualToRequestedDoW() {
        return new OnDayOfWeekValueGenerator(
                new CronField(
                        CronFieldName.DAY_OF_WEEK,
                        new On(
                                new IntegerFieldValue(FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_TIMEVALUE),
                                new SpecialCharFieldValue(SpecialChar.HASH),
                                new IntegerFieldValue(FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_HASHVALUE)
                        ), constraints),
                YEAR, FIRST_DAY_DOW_EQUALTO_REQUESTED_DOW_MONTH, MONDAY_DOW_VALUE
        );
    }
}
