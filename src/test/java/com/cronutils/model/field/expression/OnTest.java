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

package com.cronutils.model.field.expression;

import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OnTest {
    private int time;
    private int nth;

    @BeforeEach
    public void setUp() {
        time = 5;
        nth = 3;
    }

    @Test
    public void testGetTime() {
        assertEquals(time, (int) new On(new IntegerFieldValue(time)).getTime().getValue());
    }

    @Test
    public void testGetNth() {
        assertEquals(nth,
                (int) new On(new IntegerFieldValue(time), new SpecialCharFieldValue(SpecialChar.HASH), new IntegerFieldValue(nth)).getNth().getValue());
    }

    @Test
    public void testOnlyNthFails() {
        assertThrows(RuntimeException.class, () -> new On(null, new SpecialCharFieldValue(SpecialChar.HASH), new IntegerFieldValue(nth)));
    }

    @Test
    public void testAsStringJustNumber() {
        final int expression = 3;
        assertEquals(String.format("%s", expression), new On(new IntegerFieldValue(expression)).asString());
    }

    @Test
    public void testAsStringSpecialCharW() {
        final String expression = "1W";
        assertEquals(expression, new On(new IntegerFieldValue(1), new SpecialCharFieldValue(SpecialChar.W)).asString());
    }

    @Test
    public void testAsStringSpecialCharL() {
        final String expression = "L";
        assertEquals(expression, new On(new SpecialCharFieldValue(SpecialChar.L)).asString());
    }

    @Test
    public void testAsStringSpecialCharLWithNth() {
        final String expression = "L-3";
        assertEquals(expression, new On(new IntegerFieldValue(-1), new SpecialCharFieldValue(SpecialChar.L), new IntegerFieldValue(3)).asString());
    }

    @Test
    public void testAsStringWithNth() {
        final int first = 3;
        final int second = 4;
        final String expression = String.format("%s#%s", first, second);
        assertEquals(expression,
                new On(new IntegerFieldValue(first), new SpecialCharFieldValue(SpecialChar.HASH), new IntegerFieldValue(second)).asString());
    }
}
