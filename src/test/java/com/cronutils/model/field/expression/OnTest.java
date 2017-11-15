package com.cronutils.model.field.expression;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;

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
public class OnTest {
    private int time;
    private int nth;

    @Before
    public void setUp() {
        time = 5;
        nth = 3;
    }

    @Test
    public void testGetTime() throws Exception {
        assertEquals(time, (int) new On(new IntegerFieldValue(time)).getTime().getValue());
    }

    @Test
    public void testGetNth() throws Exception {
        assertEquals(nth,
                (int) new On(new IntegerFieldValue(time), new SpecialCharFieldValue(SpecialChar.HASH), new IntegerFieldValue(nth)).getNth().getValue());
    }

    @Test(expected = RuntimeException.class)
    public void testOnlyNthFails() throws Exception {
        new On(null, new SpecialCharFieldValue(SpecialChar.HASH), new IntegerFieldValue(nth));
    }

    @Test
    public void testAsStringJustNumber() {
        int expression = 3;
        assertEquals(String.format("%s", expression), new On(new IntegerFieldValue(expression)).asString());
    }

    @Test
    public void testAsStringSpecialCharW() {
        String expression = "1W";
        assertEquals(expression, new On(new IntegerFieldValue(1), new SpecialCharFieldValue(SpecialChar.W)).asString());
    }

    @Test
    public void testAsStringSpecialCharL() {
        String expression = "L";
        assertEquals(expression, new On(new SpecialCharFieldValue(SpecialChar.L)).asString());
    }

    @Test
    public void testAsStringWithNth() {
        int first = 3;
        int second = 4;
        String expression = String.format("%s#%s", first, second);
        assertEquals(expression,
                new On(new IntegerFieldValue(first), new SpecialCharFieldValue(SpecialChar.HASH), new IntegerFieldValue(second)).asString());
    }
}
