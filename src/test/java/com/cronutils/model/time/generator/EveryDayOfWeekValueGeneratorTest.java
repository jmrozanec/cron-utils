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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.value.IntegerFieldValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class EveryDayOfWeekValueGeneratorTest {
    private EveryFieldValueGenerator fieldValueGenerator;
    private final int year = 2018;
    private int month = 10;
    private Set<DayOfWeek> validDow;

    @Before
    public void setUp() {
        FieldConstraints constraints = FieldConstraintsBuilder.instance().createConstraintsInstance();
        // every 2 days between 1-5
        final CronField cronField = new CronField(CronFieldName.DAY_OF_WEEK,
                new Every(new Between(new IntegerFieldValue(1), new IntegerFieldValue(5)),
                        new IntegerFieldValue(2)),
                constraints);
        // Using monday = 1
        WeekDay mondayDoWValue = new WeekDay(1, false);
        // so MON-FRI/2, which should translate to MON,WED,FRI
        validDow = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

        fieldValueGenerator = new EveryDayOfWeekValueGenerator(cronField, year, month, mondayDoWValue);
    }

    @Test
    public void testGenerateNextValue() throws NoSuchValueException {
        // Oct 1, 2018 is a Monday. generateNextValue given 1 should return 3 (Wednesday Oct. 3, 2018)
        assertEquals(3, fieldValueGenerator.generateNextValue(1));
        // Next should be Friday (Oct. 5, 2018)
        assertEquals(5, fieldValueGenerator.generateNextValue(3));
        // Next should be Monday (Oct. 8, 2018) because of the restriction 1-5
        assertEquals(8, fieldValueGenerator.generateNextValue(5));
    }

    @Test
    public void testGeneratePreviousValue() throws NoSuchValueException {
        // Oct. 31, 2018 is a Wednesday, generatePreviousValue given 31 should give Monday 29, 2018
        assertEquals(29, fieldValueGenerator.generatePreviousValue(31));
        // Previous week's Friday is Oct. 26, 2018
        assertEquals(26, fieldValueGenerator.generatePreviousValue(29));
        assertEquals(24, fieldValueGenerator.generatePreviousValue(26));
        assertEquals(22, fieldValueGenerator.generatePreviousValue(24));
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremes() {
        int lengthOfMonth = LocalDate.of(year, month, 1).getMonth().length(Year.isLeap(year));
        List<Integer> results = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, lengthOfMonth);
        for (Integer day : results) {
            assertTrue(validDow.contains(LocalDate.of(year, month, day).getDayOfWeek()));
        }
        // should not contain the extremes, 1 and 31
        assertFalse(results.contains(1));
        assertFalse(results.contains(lengthOfMonth));
        assertTrue(results.containsAll(
                Arrays.asList(
                        3, 5,
                        8, 10, 12,
                        15, 17, 19,
                        22, 24, 26,
                        29)));
    }

    @Test
    public void testIsMatch() {
        assertFalse(fieldValueGenerator.isMatch(16));
        assertTrue(fieldValueGenerator.isMatch(17));
        assertFalse(fieldValueGenerator.isMatch(18));
    }

    @Test
    public void testMatchesFieldExpressionClass() {
        assertTrue(fieldValueGenerator.matchesFieldExpressionClass(mock(Every.class)));
        assertFalse(fieldValueGenerator.matchesFieldExpressionClass(mock(FieldExpression.class)));
    }
}
