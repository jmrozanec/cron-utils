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
    public void setUp() throws Exception {
        final Function<FieldValue<?>, FieldValue<?>> transform = input -> input;
        valueMappingFieldExpressionVisitor = new ValueMappingFieldExpressionVisitor(transform);
    }

    @Test
    public void testVisitQuestionMark() throws Exception {
        final FieldExpression param = FieldExpression.questionMark();
        final QuestionMark questionMark = (QuestionMark) valueMappingFieldExpressionVisitor.visit(param);
        assertTrue(param == questionMark);//always the same cause of singleton pattern
    }
}
