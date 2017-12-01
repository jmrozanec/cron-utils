/*
 * Copyright 2014 jmrozanec
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

package com.cronutils.model.field.expression.visitor;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.Function;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.model.field.value.FieldValue;

import static org.junit.Assert.assertTrue;

public class ValueMappingFieldExpressionVisitorTest {
    private ValueMappingFieldExpressionVisitor valueMappingFieldExpressionVisitor;

    @Before
    public void setUp() {
        final Function<FieldValue<?>, FieldValue<?>> transform = input -> input;
        valueMappingFieldExpressionVisitor = new ValueMappingFieldExpressionVisitor(transform);
    }

    @Test
    public void testVisitQuestionMark() {
        final FieldExpression param = FieldExpression.questionMark();
        final QuestionMark questionMark = (QuestionMark) valueMappingFieldExpressionVisitor.visit(param);
        assertTrue(param == questionMark);//always the same cause of singleton pattern
    }
}
