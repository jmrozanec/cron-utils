package com.cronutils.model.field.expression;

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.FieldExpression;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
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
public class FieldExpressionTest {
    private TestFieldExpression testCronFieldExpression;
    @Mock
    private FieldConstraints mockFieldConstraints;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.testCronFieldExpression = new TestFieldExpression(mockFieldConstraints);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullConstraints() throws Exception {
        new TestFieldExpression(null);
    }

    @Test
    public void testAnd() throws Exception {
        FieldExpression mockExpression = mock(FieldExpression.class);
        And and = testCronFieldExpression.and(mockExpression);
        assertTrue(and.getExpressions().contains(mockExpression));
        assertTrue(and.getExpressions().contains(testCronFieldExpression));
    }

    @Test
    public void testGetConstraints() throws Exception {
        assertEquals(mockFieldConstraints, testCronFieldExpression.getConstraints());
    }

    class TestFieldExpression extends FieldExpression {

        public TestFieldExpression(FieldConstraints constraints) {
            super(constraints);
        }

        @Override
        public String asString() {
            return null;
        }
    }
}