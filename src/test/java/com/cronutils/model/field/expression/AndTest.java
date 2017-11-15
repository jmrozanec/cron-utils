package com.cronutils.model.field.expression;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
public class AndTest {

    private And and;
    private FieldExpression expression1;
    private FieldExpression expression2;

    @Before
    public void setUp() throws Exception {
        and = new And();
        expression1 = mock(FieldExpression.class);
        expression2 = mock(FieldExpression.class);
    }

    @Test
    public void testAnd() throws Exception {
        and.and(expression1).and(expression2);
        assertEquals(2, and.getExpressions().size());
        assertEquals(expression1, and.getExpressions().get(0));
        assertEquals(expression2, and.getExpressions().get(1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetExpressionsImmutable() throws Exception {
        and.and(expression1).getExpressions().add(expression2);
    }

    @Test
    public void testAString() throws Exception {
        String expression1String = "expression1";
        String expression2String = "expression2";
        when(expression1.asString()).thenReturn(expression1String);
        when(expression2.asString()).thenReturn(expression2String);
        and.and(expression1).and(expression2);

        assertEquals(String.format("%s,%s", expression1String, expression2String), and.asString());
    }
}
