package com.cronutils.model.time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
public class TimeNodeTest {
    private static final int LIST_START_VALUE = 2;
    private static final int LIST_MEDIUM_VALUE = 4;
    private static final int LIST_END_VALUE = 6;
    private static final int LOW_INTERMEDIATE_VALUE = 1;
    private static final int HIGH_INTERMEDIATE_VALUE = 5;
    private List<Integer> values;
    private TimeNode timeNode;

    @Before
    public void setUp() throws Exception {
        values = new ArrayList<>();
        values.add(LIST_START_VALUE);
        values.add(LIST_MEDIUM_VALUE);
        values.add(LIST_END_VALUE);
        this.timeNode = new TimeNode(values);
    }

    @Test
    public void testGetNextValue() throws Exception {
        assertResult(LIST_START_VALUE, 0, timeNode.getNextValue(LIST_START_VALUE, 0));
        assertResult(LIST_MEDIUM_VALUE, 0, timeNode.getNextValue(LIST_MEDIUM_VALUE, 0));
        assertResult(LIST_END_VALUE, 0, timeNode.getNextValue(LIST_END_VALUE, 0));

        assertResult(LIST_MEDIUM_VALUE, 0, timeNode.getNextValue(LIST_START_VALUE, 1));
        assertResult(LIST_END_VALUE, 0, timeNode.getNextValue(LIST_MEDIUM_VALUE, 1));
        assertResult(LIST_START_VALUE, 1, timeNode.getNextValue(LIST_END_VALUE, 1));

        assertResult(LIST_START_VALUE, 1, timeNode.getNextValue(LIST_MEDIUM_VALUE, 2));
    }

    @Test
    public void testGetValues() throws Exception {
        assertEquals(values, timeNode.getValues());
    }

    @Test
    public void testGetPreviousValue() throws Exception {
        assertResult(LIST_START_VALUE, 0, timeNode.getPreviousValue(LIST_START_VALUE, 0));
        assertResult(LIST_MEDIUM_VALUE, 0, timeNode.getPreviousValue(LIST_MEDIUM_VALUE, 0));
        assertResult(LIST_END_VALUE, 0, timeNode.getPreviousValue(LIST_END_VALUE, 0));

        assertResult(LIST_END_VALUE, 1, timeNode.getPreviousValue(LIST_START_VALUE, 1));
        assertResult(LIST_START_VALUE, 0, timeNode.getPreviousValue(LIST_MEDIUM_VALUE, 1));
        assertResult(LIST_MEDIUM_VALUE, 0, timeNode.getPreviousValue(LIST_END_VALUE, 1));

        assertResult(LIST_END_VALUE, 1, timeNode.getPreviousValue(LIST_MEDIUM_VALUE, 2));

        assertResult(LIST_MEDIUM_VALUE, 0, timeNode.getPreviousValue(HIGH_INTERMEDIATE_VALUE, 1));
        assertResult(LIST_END_VALUE, 1, timeNode.getPreviousValue(LOW_INTERMEDIATE_VALUE, 0));
    }

    @Test
    public void testGetValueFromListWhereIndexLessThanZero() {
        int index = -1;
        int expectedShifts = 1;
        AtomicInteger shift = new AtomicInteger(0);
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        int value = timeNode.getValueFromList(list, index, shift);
        assertEquals(String.format("Shift was: %s; expected: %s", shift.get(), expectedShifts), expectedShifts, shift.get());
        assertEquals((int) list.get(list.size() + index), value);
    }

    @Test
    public void testGetValueFromListWhereIndexMoreThanZero() {
        int index = 1;
        int expectedShifts = 0;
        AtomicInteger shift = new AtomicInteger(0);
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        int value = timeNode.getValueFromList(list, index, shift);
        assertEquals(String.format("Shift was: %s; expected: %s", shift.get(), expectedShifts), expectedShifts, shift.get());
        assertEquals((int) list.get(index), value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueFromListWithEmptyList() {
        timeNode.getValueFromList(new ArrayList<>(), 0, new AtomicInteger(0));
    }

    private void assertResult(int value, int shift, NearestValue nearestValue) {
        assertEquals(String.format("Values do not match! Expected: %s Found: %s", value, nearestValue.getValue()), value, nearestValue.getValue());
        assertEquals(String.format("Shifts do not match! Expected: %s Found: %s", shift, nearestValue.getShifts()), shift, nearestValue.getShifts());
    }
}
