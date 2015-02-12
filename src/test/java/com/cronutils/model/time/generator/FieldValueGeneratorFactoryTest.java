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
    public void testCreateDayOfWeekValueGeneratorInstanceBADCronFieldName() throws Exception {
        when(mockCronField.getField()).thenReturn(CronFieldName.YEAR);
        when(mockCronField.getExpression()).thenReturn(mock(On.class));
        fieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(mockCronField, 2015, 1, new WeekDay(1, false));
    }
}