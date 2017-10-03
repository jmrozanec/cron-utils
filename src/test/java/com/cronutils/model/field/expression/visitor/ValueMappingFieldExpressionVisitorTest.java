package com.cronutils.model.field.expression.visitor;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.Function;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.model.field.value.FieldValue;

public class ValueMappingFieldExpressionVisitorTest {
    private ValueMappingFieldExpressionVisitor valueMappingFieldExpressionVisitor;

    @Before
    public void setUp() throws Exception {
        Function<FieldValue, FieldValue> transform = new Function<FieldValue, FieldValue>() {
            @Override
            public FieldValue apply(FieldValue input) {
                return input;
            }
        };
        this.valueMappingFieldExpressionVisitor = new ValueMappingFieldExpressionVisitor(transform);
    }

    @Test
    public void testVisitQuestionMark() throws Exception {
        QuestionMark param = new QuestionMark();
        QuestionMark questionMark = (QuestionMark) valueMappingFieldExpressionVisitor.visit(param);
        assertTrue(param!=questionMark);//not same instance
    }
}
