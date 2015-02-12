package com.cronutils.model.time;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NearestValueTest {
    private int value;
    private int shifts;

    private NearestValue nearestValue;

    @Before
    public void setUp(){
        value = 1;
        shifts = 1;
        nearestValue = new NearestValue(value, shifts);
    }

    @Test
    public void testGetValue() throws Exception {
        assertEquals(value, nearestValue.getValue());
    }

    @Test
    public void testGetShifts() throws Exception {
        assertEquals(shifts, nearestValue.getShifts());
    }
}