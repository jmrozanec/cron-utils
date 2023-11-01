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

package com.cronutils.model.field;

import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.*;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.parser.FieldParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FieldParserTest {
    private FieldParser parser;

    @BeforeEach
    public void setUp() {
        parser = new FieldParser(FieldConstraintsBuilder.instance().addHashSupport().createConstraintsInstance());
    }

    @Test
    public void testParseAlways() {
        assertTrue(parser.parse("*") instanceof Always);
    }

    @Test
    public void testParseAlwaysEveryX() {
        final int every = 5;
        final Every expression = (Every) parser.parse("*/" + every);
        assertEquals(every, (int) (expression).getPeriod().getValue());
        assertTrue(expression.getExpression() instanceof Always);
    }

    @Test
    public void testParseOn() {
        final int on = 5;
        assertEquals(on, (int) ((On) parser.parse("" + on)).getTime().getValue());
    }

    @Test //#194
    public void testParseOnWithHash01() {
        final int on = 5;
        final int hashValue = 3;
        final On onExpression = (On) parser.parse(String.format("%s#%s", on, hashValue));
        assertEquals(on, (int) (onExpression.getTime().getValue()));
        assertEquals(hashValue, onExpression.getNth().getValue().intValue());
        assertEquals(SpecialChar.HASH, onExpression.getSpecialChar().getValue());
    }

    @Test
    public void testRejectHashIfFieldDoesNotClaimToSupportIt() {
        assertThrows(IllegalArgumentException.class, () -> new FieldParser(FieldConstraintsBuilder.instance().createConstraintsInstance()).parse("5#3"));
    }

    @Test
    public void testParseAnd() {
        final int on1 = 3;
        final int on2 = 4;
        final And and = (And) parser.parse(String.format("%s,%s", on1, on2));
        assertEquals(2, and.getExpressions().size());
        assertEquals(on1, (int) ((On) and.getExpressions().get(0)).getTime().getValue());
        assertEquals(on2, (int) ((On) and.getExpressions().get(1)).getTime().getValue());
    }

    @Test
    public void testParseBetween() {
        final int from = 3;
        final int to = 4;
        final Between between = (Between) parser.parse(String.format("%s-%s", from, to));
        assertEquals(from, (int) ((IntegerFieldValue) between.getFrom()).getValue());
        assertEquals(to, (int) ((IntegerFieldValue) between.getTo()).getValue());
    }

    @Test
    public void testParseBetweenEveryX() {
        final int from = 10;
        final int to = 40;
        final int every = 5;
        final Every expression = (Every) parser.parse(String.format("%s-%s/%s", from, to, every));
        final Between between = (Between) expression.getExpression();
        assertEquals(from, (int) ((IntegerFieldValue) between.getFrom()).getValue());
        assertEquals(to, (int) ((IntegerFieldValue) between.getTo()).getValue());
        assertEquals(every, (int) (expression.getPeriod()).getValue());
    }

    @Test
    public void testCostructorNullConstraints() {
        assertThrows(NullPointerException.class, () -> new FieldParser(null));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"abLW", "LWlw", "1LW7"})
    public void testParseOnWithLWForException(String expression) {
    	String exceptionMessage = String.format("Expected: LW, found: %s", expression.replace("LW", ""));
    	Exception exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(expression));
    	assertEquals(exceptionMessage, exception.getMessage());
    }
}
