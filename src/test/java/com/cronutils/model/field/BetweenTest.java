package com.cronutils.model.field;

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
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
public class BetweenTest {
    private int from;
    private int to;
    private int every;
    private FieldConstraints nullFieldConstraints;

    @Before
    public void setUp() {
        from = 1;
        to = 5;
        every = 2;
        nullFieldConstraints = FieldConstraintsBuilder.instance().createConstraintsInstance();
    }

    @Test
    public void testGetFrom() throws Exception {
        assertEquals(from, new Between(nullFieldConstraints, "" + from, "" + to).getFrom());
    }

    @Test
    public void testGetTo() throws Exception {
        assertEquals(to, new Between(nullFieldConstraints, "" + from, "" + to).getTo());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromGreaterThanTo() throws Exception {
        new Between(nullFieldConstraints, "" + to, "" + from);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromEqualThanTo() throws Exception {
        new Between(nullFieldConstraints, "" + from, "" + from);
    }

    @Test
    public void testGetEveryDefault() throws Exception {
        assertEquals(1, new Between(nullFieldConstraints, "" + from, "" + to).getEvery().getTime());
    }

    @Test
    public void testGetEveryX() throws Exception {
        assertEquals(every, new Between(nullFieldConstraints, "" + from, "" + to, "" + every).getEvery().getTime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEveryXBiggerThanRange() throws Exception {
        assertEquals(1, new Between(nullFieldConstraints, "" + from, "" + to, "" + 2 * to).getEvery().getTime());
    }
}