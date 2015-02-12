package com.cronutils.mapper;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WeekDayTest {
    private WeekDay weekDay;
    private int mondayDoWValue = 1;
    private boolean firstDayIsZero = false;

    @Before
    public void setUp(){
        this.weekDay = new WeekDay(mondayDoWValue, firstDayIsZero);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFailsIfMondayDoWNegative() throws Exception {
        new WeekDay(-1, firstDayIsZero);
    }

    @Test
    public void testGetMondayDoWValue() throws Exception {
        assertEquals(mondayDoWValue, weekDay.getMondayDoWValue());
    }

    @Test
    public void testMapIntervalWithZeroNotStartingMonday() throws Exception {
        WeekDay toBeConverted = new WeekDay(1, true);
        assertEquals(7, weekDay.map(toBeConverted, 0));
    }

    @Test
    public void testMapIntervalWithZeroStartingMonday() throws Exception {
        WeekDay toBeConverted = new WeekDay(0, true);
        assertEquals(1, weekDay.map(toBeConverted, 0));
    }

    @Test
    public void testMapIntervalWithoutZeroStartingMonday() throws Exception {
        int value = 7;
        WeekDay toBeConverted = new WeekDay(1, false);
        assertEquals(value, weekDay.map(toBeConverted, value));
    }

    @Test
    public void testMapIntervalWithoutZeroStartingSunday() throws Exception {
        int value = 7;
        WeekDay toBeConverted = new WeekDay(2, false);
        assertEquals(value-1, weekDay.map(toBeConverted, value));
    }

    @Test
    public void testQuartzToJodatime() throws Exception {
        WeekDay quartz = ConstantsMapper.QUARTZ_WEEK_DAY;
        WeekDay jodatime = ConstantsMapper.JODATIME_WEEK_DAY;
        assertEquals(7, jodatime.map(quartz, 1));
    }
}