package com.cronutils.model.field;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.parser.FieldParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/*
 * Copyright 2015 jmrozanec Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
public class FieldParserTest {
    private FieldParser parser;

    @Before
    public void setUp() {
        parser = new FieldParser(FieldConstraintsBuilder.instance().addHashSupport().createConstraintsInstance());
    }

    @Test
    public void testParseAlways() throws Exception {
        assertTrue(parser.parse("*") instanceof Always);
    }

    @Test
    public void testParseAlwaysEveryX() throws Exception {
        int every = 5;
        Every expression = (Every) parser.parse("*/" + every);
        assertEquals(every, (int) (expression).getPeriod().getValue());
        assertTrue(expression.getExpression() instanceof Always);
    }

    @Test
    public void testParseOn() throws Exception {
        int on = 5;
        assertEquals(on, (int) ((On) parser.parse("" + on)).getTime().getValue());
    }

    @Test //#194
    public void testParseOnWithHash01() {
        int on = 5;
        int hashValue = 3;
        On onExpression = (On) parser.parse(String.format("%s#%s", on, hashValue));
        assertEquals(on, (int) (onExpression.getTime().getValue()));
        assertEquals(hashValue, onExpression.getNth().getValue().intValue());
        assertEquals(SpecialChar.HASH, onExpression.getSpecialChar().getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectHashIfFieldDoesNotClaimToSupportIt() {
        new FieldParser(FieldConstraintsBuilder.instance().createConstraintsInstance()).parse("5#3");
    }

    @Test
    public void testParseAnd() throws Exception {
        int on1 = 3;
        int on2 = 4;
        And and = (And) parser.parse(String.format("%s,%s", on1, on2));
        assertEquals(2, and.getExpressions().size());
        assertEquals(on1, (int) ((On) and.getExpressions().get(0)).getTime().getValue());
        assertEquals(on2, (int) ((On) and.getExpressions().get(1)).getTime().getValue());
    }

    @Test
    public void testParseBetween() throws Exception {
        int from = 3;
        int to = 4;
        Between between = (Between) parser.parse(String.format("%s-%s", from, to));
        assertEquals(from, (int) ((IntegerFieldValue) between.getFrom()).getValue());
        assertEquals(to, (int) ((IntegerFieldValue) between.getTo()).getValue());
    }

    @Test
    public void testParseBetweenEveryX() throws Exception {
        int from = 10;
        int to = 40;
        int every = 5;
        Every expression = (Every) parser.parse(String.format("%s-%s/%s", from, to, every));
        Between between = (Between) expression.getExpression();
        assertEquals(from, (int) ((IntegerFieldValue) between.getFrom()).getValue());
        assertEquals(to, (int) ((IntegerFieldValue) between.getTo()).getValue());
        assertEquals(every, (int) (expression.getPeriod()).getValue());
    }

    @Test(expected = NullPointerException.class)
    public void testCostructorNullConstraints() throws Exception {
        new FieldParser(null);
    }
}
