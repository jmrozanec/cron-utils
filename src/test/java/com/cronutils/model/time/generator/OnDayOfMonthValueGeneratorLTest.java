package com.cronutils.model.time.generator;

import java.time.LocalDate;
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
public class OnDayOfMonthValueGeneratorLTest {
    private OnDayOfMonthValueGenerator fieldValueGenerator;
    private int year = 2015;
    private int month = 2;
    private int lastDayInMonth = LocalDate.of(2015, 2, 1).lengthOfMonth();

    @Before
    public void setUp() {
        FieldConstraints constraints = FieldConstraintsBuilder.instance().addLSupport().createConstraintsInstance();
        fieldValueGenerator = new OnDayOfMonthValueGenerator(
                new CronField(CronFieldName.DAY_OF_MONTH, new On(new SpecialCharFieldValue(SpecialChar.L)), constraints), year, month);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGenerateNextValue() throws Exception {
        assertEquals(lastDayInMonth, fieldValueGenerator.generateNextValue(1));
        fieldValueGenerator.generateNextValue(lastDayInMonth);
    }

    @Test(expected = NoSuchValueException.class)
    public void testGeneratePreviousValue() throws Exception {
        assertEquals(lastDayInMonth, fieldValueGenerator.generatePreviousValue(lastDayInMonth + 1));
        fieldValueGenerator.generatePreviousValue(lastDayInMonth);
    }

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremes() throws Exception {
        List<Integer> candidates = fieldValueGenerator.generateCandidatesNotIncludingIntervalExtremes(1, 32);
        assertEquals(1, candidates.size());
        assertEquals(lastDayInMonth, candidates.get(0), 0);
    }

    @Test
    public void testIsMatch() throws Exception {
        assertTrue(fieldValueGenerator.isMatch(lastDayInMonth));
        assertFalse(fieldValueGenerator.isMatch(lastDayInMonth - 1));
    }
}
