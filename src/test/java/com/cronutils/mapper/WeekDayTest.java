package com.cronutils.mapper;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
public class WeekDayTest {
    private WeekDay source;
    private int mondayDoWValue = 1;
    private boolean firstDayIsZero = false;

    @Before
    public void setUp() {
        this.source = new WeekDay(mondayDoWValue, firstDayIsZero);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFailsIfMondayDoWNegative() throws Exception {
        new WeekDay(-1, firstDayIsZero);
    }

    @Test
    public void testGetMondayDoWValue() throws Exception {
        assertEquals(mondayDoWValue, source.getMondayDoWValue());
    }

    @Test
    public void testMapIntervalWithZeroNotStartingMonday() throws Exception {
        WeekDay target = new WeekDay(1, true);
        assertEquals(0, source.mapTo(7, target));
    }

    @Test
    public void testMapIntervalWithZeroStartingMonday() throws Exception {
        WeekDay target = new WeekDay(0, true);
        assertEquals(0, source.mapTo(1, target));
    }

    @Test
    public void testMapIntervalWithoutZeroStartingMonday() throws Exception {
        int value = 7;
        WeekDay target = new WeekDay(1, false);
        assertEquals(value, source.mapTo(value, target));
    }

    @Test
    public void testMapIntervalWithoutZeroStartingSunday() throws Exception {
        int value = 7;
        WeekDay target = new WeekDay(2, false);
        assertEquals(1, source.mapTo(value, target));
    }
}
