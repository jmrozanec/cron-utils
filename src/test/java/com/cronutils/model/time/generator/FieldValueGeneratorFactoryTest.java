package com.cronutils.model.time.generator;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.*;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
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
    private FieldValueGeneratorFactory fieldValueGeneratorFactory;

    private CronField mockCronField;
    @Before
    public void setUp() throws Exception {
        fieldValueGeneratorFactory = FieldValueGeneratorFactory.instance();
        mockCronField = mock(CronField.class);
    }

    @Test
    public void testInstance() throws Exception {
        assertNotNull(FieldValueGeneratorFactory.instance());
    }

    @Test
    public void testForCronFieldAlways() throws Exception {
        when(mockCronField.getExpression()).thenReturn(mock(Always.class));
        assertEquals(AlwaysFieldValueGenerator.class, fieldValueGeneratorFactory.forCronField(mockCronField).getClass());
    }

    @Test
    public void testForCronFieldAnd() throws Exception {
        when(mockCronField.getExpression()).thenReturn(mock(And.class));
        assertEquals(AndFieldValueGenerator.class, fieldValueGeneratorFactory.forCronField(mockCronField).getClass());
    }

    @Test
    public void testForCronFieldBetween() throws Exception {
        when(mockCronField.getExpression()).thenReturn(mock(Between.class));
        assertEquals(BetweenFieldValueGenerator.class, fieldValueGeneratorFactory.forCronField(mockCronField).getClass());
    }

    @Test
    public void testForCronFieldOnSpecialCharNone() throws Exception {
        On mockOn = mock(On.class);
        when(mockOn.getSpecialChar()).thenReturn(SpecialChar.NONE);
        when(mockCronField.getExpression()).thenReturn(mockOn);
        assertEquals(OnFieldValueGenerator.class, fieldValueGeneratorFactory.forCronField(mockCronField).getClass());
    }

    @Test
    public void testForCronFieldOnSpecialCharNotNone() throws Exception {
        On mockOn = mock(On.class);
        for(SpecialChar s : SpecialChar.values()){
            if(!s.equals(SpecialChar.NONE)){
                boolean gotException = false;
                when(mockOn.getSpecialChar()).thenReturn(s);
                when(mockCronField.getExpression()).thenReturn(mockOn);
                try{
                    fieldValueGeneratorFactory.forCronField(mockCronField);
                }catch (RuntimeException e){
                    gotException = true;
                }
                assertTrue("Should get exception when asking for OnValueGenerator with special char", gotException);
            }
        }
    }

    @Test
    public void testForCronField() throws Exception {
        when(mockCronField.getExpression()).thenReturn(mock(FieldExpression.class));
        assertEquals(NullFieldValueGenerator.class, fieldValueGeneratorFactory.forCronField(mockCronField).getClass());
    }

    @Test
    public void testCreateDayOfMonthValueGeneratorInstance() throws Exception {
        when(mockCronField.getField()).thenReturn(CronFieldName.DAY_OF_MONTH);
        when(mockCronField.getExpression()).thenReturn(mock(On.class));
        assertEquals(
                OnDayOfMonthValueGenerator.class,
                fieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(mockCronField, 2015, 1).getClass()
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDayOfMonthValueGeneratorInstanceBadCronFieldName() throws Exception {
        when(mockCronField.getField()).thenReturn(CronFieldName.YEAR);
        when(mockCronField.getExpression()).thenReturn(mock(On.class));
        fieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(mockCronField, 2015, 1);
    }

    @Test
    public void testCreateDayOfWeekValueGeneratorInstance() throws Exception {
        when(mockCronField.getField()).thenReturn(CronFieldName.DAY_OF_WEEK);
        when(mockCronField.getExpression()).thenReturn(mock(On.class));
        assertEquals(
                OnDayOfWeekValueGenerator.class,
                fieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(mockCronField, 2015, 1, new WeekDay(1, false)).getClass()
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDayOfWeekValueGeneratorInstanceBadCronFieldName() throws Exception {
        when(mockCronField.getField()).thenReturn(CronFieldName.YEAR);
        when(mockCronField.getExpression()).thenReturn(mock(On.class));
        fieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(mockCronField, 2015, 1, new WeekDay(1, false));
    }
}