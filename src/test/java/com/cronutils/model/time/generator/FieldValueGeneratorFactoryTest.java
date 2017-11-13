package com.cronutils.model.time.generator;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
public class FieldValueGeneratorFactoryTest {
    private CronField mockCronField;

    @Before
    public void setUp() throws Exception {
        mockCronField = mock(CronField.class);
    }

    @Test
    public void testForCronFieldAlways() throws Exception {
        when(mockCronField.getExpression()).thenReturn(mock(Always.class));
        assertEquals(AlwaysFieldValueGenerator.class, FieldValueGeneratorFactory.forCronField(mockCronField).getClass());
    }

    @Test
    public void testForCronFieldAnd() throws Exception {
        when(mockCronField.getExpression()).thenReturn(mock(And.class));
        assertEquals(AndFieldValueGenerator.class, FieldValueGeneratorFactory.forCronField(mockCronField).getClass());
    }

    @Test
    public void testForCronFieldBetween() throws Exception {
        when(mockCronField.getExpression()).thenReturn(mock(Between.class));
        assertEquals(BetweenFieldValueGenerator.class, FieldValueGeneratorFactory.forCronField(mockCronField).getClass());
    }

    @Test
    public void testForCronFieldOnSpecialCharNone() throws Exception {
        On mockOn = mock(On.class);
        when(mockOn.getSpecialChar()).thenReturn(new SpecialCharFieldValue(SpecialChar.NONE));
        when(mockCronField.getExpression()).thenReturn(mockOn);
        assertEquals(OnFieldValueGenerator.class, FieldValueGeneratorFactory.forCronField(mockCronField).getClass());
    }

    @Test
    public void testForCronFieldOnSpecialCharNotNone() throws Exception {
        On mockOn = mock(On.class);
        for (SpecialChar s : SpecialChar.values()) {
            if (!s.equals(SpecialChar.NONE)) {
                boolean gotException = false;
                when(mockOn.getSpecialChar()).thenReturn(new SpecialCharFieldValue(s));
                when(mockCronField.getExpression()).thenReturn(mockOn);
                try {
                    FieldValueGeneratorFactory.forCronField(mockCronField);
                } catch (RuntimeException e) {
                    gotException = true;
                }
                assertTrue("Should get exception when asking for OnValueGenerator with special char", gotException);
            }
        }
    }

    @Test
    public void testForCronField() throws Exception {
        when(mockCronField.getExpression()).thenReturn(mock(FieldExpression.class));
        assertEquals(NullFieldValueGenerator.class, FieldValueGeneratorFactory.forCronField(mockCronField).getClass());
    }

    @Test
    public void testCreateDayOfMonthValueGeneratorInstanceForL() throws Exception {
        assertEquals(
                OnDayOfMonthValueGenerator.class,
                createDayOfMonthValueGeneratorInstance(SpecialChar.L).getClass()
        );
    }

    @Test
    public void testCreateDayOfMonthValueGeneratorInstanceForW() throws Exception {
        assertEquals(
                OnDayOfMonthValueGenerator.class,
                createDayOfMonthValueGeneratorInstance(SpecialChar.W).getClass()
        );
    }

    @Test
    public void testCreateDayOfMonthValueGeneratorInstanceForLW() throws Exception {
        assertEquals(
                OnDayOfMonthValueGenerator.class,
                createDayOfMonthValueGeneratorInstance(SpecialChar.LW).getClass()
        );
    }

    @Test
    public void testCreateDayOfMonthValueGeneratorInstanceForHash() throws Exception {
        assertEquals(
                OnDayOfMonthValueGenerator.class,
                createDayOfMonthValueGeneratorInstance(SpecialChar.HASH).getClass()
        );
    }

    @Test
    public void testCreateDayOfMonthValueGeneratorInstanceForNONE() throws Exception {
        assertEquals(
                OnFieldValueGenerator.class,
                createDayOfMonthValueGeneratorInstance(SpecialChar.NONE).getClass()
        );
    }

    private FieldValueGenerator createDayOfMonthValueGeneratorInstance(SpecialChar specialChar) {
        when(mockCronField.getField()).thenReturn(CronFieldName.DAY_OF_MONTH);
        On mockOn = mock(On.class);
        when(mockOn.getSpecialChar()).thenReturn(new SpecialCharFieldValue(specialChar));
        when(mockCronField.getExpression()).thenReturn(mockOn);
        return FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(mockCronField, 2015, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDayOfMonthValueGeneratorInstanceBadCronFieldName() throws Exception {
        when(mockCronField.getField()).thenReturn(CronFieldName.YEAR);
        On mockOn = mock(On.class);
        when(mockOn.getSpecialChar()).thenReturn(new SpecialCharFieldValue(SpecialChar.L));//any value except NONE
        when(mockCronField.getExpression()).thenReturn(mockOn);

        FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(mockCronField, 2015, 1);
    }

    @Test
    public void testCreateDayOfWeekValueGeneratorInstance() throws Exception {
        when(mockCronField.getField()).thenReturn(CronFieldName.DAY_OF_WEEK);
        when(mockCronField.getExpression()).thenReturn(mock(On.class));
        assertEquals(
                OnDayOfWeekValueGenerator.class,
                FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(mockCronField, 2015, 1, new WeekDay(1, false)).getClass()
        );
    }

    @Test
    public void testCreateDayOfWeekValueGeneratorInstance_Between() throws Exception {
        Between between = new Between(new IntegerFieldValue(1), new IntegerFieldValue(7));
        when(mockCronField.getField()).thenReturn(CronFieldName.DAY_OF_WEEK);
        when(mockCronField.getExpression()).thenReturn(between);
        assertEquals(
                BetweenDayOfWeekValueGenerator.class,
                FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(mockCronField, 2015, 1, new WeekDay(1, false)).getClass()
        );
    }

    @Test
    public void testCreateDayOfWeekValueGeneratorInstance_And() throws Exception {
        when(mockCronField.getField()).thenReturn(CronFieldName.DAY_OF_WEEK);
        when(mockCronField.getExpression()).thenReturn(mock(And.class));
        assertEquals(
                AndDayOfWeekValueGenerator.class,
                FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(mockCronField, 2015, 1, new WeekDay(1, false)).getClass()
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDayOfWeekValueGeneratorInstanceBadCronFieldName() throws Exception {
        when(mockCronField.getField()).thenReturn(CronFieldName.YEAR);
        when(mockCronField.getExpression()).thenReturn(mock(On.class));
        FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(mockCronField, 2015, 1, new WeekDay(1, false));
    }
}
