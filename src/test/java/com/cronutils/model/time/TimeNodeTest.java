package com.cronutils.model.time;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
    private static final int LIST_START_VALUE = 1;
    private static final int LIST_MEDIUM_VALUE = 3;
    private static final int LIST_END_VALUE = 5;
    private List<Integer> values;
    private TimeNode timeNode;

    @Before
    public void setUp() throws Exception {
        values = Lists.newArrayList();
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
    }

    private void assertResult(int value, int shift, NearestValue nearestValue){
        assertEquals(String.format("Values do not match! Expected: %s Found: %s", value, nearestValue.getValue()), value, nearestValue.getValue());
        assertEquals(String.format("Shifts do not match! Expected: %s Found: %s", shift, nearestValue.getShifts()), shift, nearestValue.getShifts());
    }
}