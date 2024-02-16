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

import com.cronutils.model.field.expression.visitor.FieldExpressionVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class FieldExpressionTest {
    private TestFieldExpression testCronFieldExpression;

    @BeforeEach
    public void setUp() {
        testCronFieldExpression = new TestFieldExpression();
    }

    @Test
    public void testAnd() {
        final FieldExpression mockExpression = mock(FieldExpression.class);
        final And and = testCronFieldExpression.and(mockExpression);
        assertTrue(and.getExpressions().contains(mockExpression));
        assertTrue(and.getExpressions().contains(testCronFieldExpression));
    }

    static class TestFieldExpression extends FieldExpression {

        private static final long serialVersionUID = 8101930390397976027L;

        @Override
        public FieldExpression accept(final FieldExpressionVisitor visitor) {
            return null;
        }

        @Override
        public String asString() {
            return "test";
        }
    }
}
