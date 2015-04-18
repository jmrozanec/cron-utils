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
public class OnTest {
    private int time;
    private int nth;
    private FieldConstraints nullFieldConstraints;

    @Before
    public void setUp() {
        time = 5;
        nth = 3;
        nullFieldConstraints =
                FieldConstraintsBuilder.instance()
                        .addHashSupport()
                        .addLSupport()
                        .addWSupport()
                        .createConstraintsInstance();
    }

    @Test
    public void testGetTime() throws Exception {
        assertEquals(time, new On(nullFieldConstraints, "" + time).getTime());
    }

    @Test
    public void testGetNth() throws Exception {
        assertEquals(nth, new On(nullFieldConstraints, String.format("%s#%s", time, nth)).getNth());
    }

    @Test(expected = RuntimeException.class)
    public void testOnlyNthFails() throws Exception {
        new On(nullFieldConstraints, String.format("#%s", nth));
    }

    @Test
    public void testAsStringJustNumber(){
        String expression = "3";
        assertEquals(expression, new On(nullFieldConstraints, expression).asString());
    }

    @Test
    public void testAsStringSpecialCharW(){
        String expression = "1W";
        assertEquals(expression, new On(nullFieldConstraints, expression).asString());
    }

    @Test
    public void testAsStringSpecialCharL(){
        String expression = "L";
        assertEquals(expression, new On(nullFieldConstraints, expression).asString());
    }

    @Test
    public void testAsStringWithNth(){
        String expression = "3#4";
        assertEquals(expression, new On(nullFieldConstraints, expression).asString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJustNumberInvalidChar(){
        String expression = "$";
        assertEquals(expression, new On(nullFieldConstraints, expression).asString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAsStringSpecialCharWInvalidChar(){
        String expression = "$W";
        assertEquals(expression, new On(nullFieldConstraints, expression).asString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAsStringWithNthInvalidCharFirstTerm(){
        String expression = "$#4";
        assertEquals(expression, new On(nullFieldConstraints, expression).asString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAsStringWithNthInvalidCharSecondTerm(){
        String expression = "3#$";
        assertEquals(expression, new On(nullFieldConstraints, expression).asString());
    }
}