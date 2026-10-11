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

import com.cronutils.model.field.expression.visitor.*;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.parser.CronParser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @ParameterizedTest
    @CsvSource({
            "*, true, false",
            "*/1, true, false",
            "*/5, false, true",
            "5/10, false, true",
            "5-25/10, false, true",
            "10, false, false",
            "5-25, false, false",
            "'0,15,30', false, false",
            "'*/5,7', false, false"
    })
    public void testParsedExpressionTypes(final String minute, final boolean always, final boolean every) {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        final FieldExpression expression = parser.parse(minute + " * * * *")
                .retrieve(CronFieldName.MINUTE).getExpression();
        assertEquals(always, expression.isAlways());
        assertEquals(every, expression.isEvery());
    }

    @Test
    public void testEveryOneRetainsItsExpressionType() {
        final FieldExpression expression = FieldExpressionFactory.every(1);
        assertEquals("*", expression.asString());
        assertFalse(expression.isAlways());
        assertTrue(expression.isEvery());
    }

    @Test
    public void testDeserializedAlwaysRetainsItsExpressionType() throws Exception {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(FieldExpression.always());
        }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            final FieldExpression expression = (FieldExpression) input.readObject();
            assertTrue(expression.isAlways());
            assertFalse(expression.isEvery());
        }
    }

    @Test
    public void testOtherExpressionTypes() {
        assertFalse(FieldExpression.questionMark().isAlways());
        assertFalse(FieldExpression.questionMark().isEvery());
        assertFalse(testCronFieldExpression.isAlways());
        assertFalse(testCronFieldExpression.isEvery());
    }

    class TestFieldExpression extends FieldExpression {

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
