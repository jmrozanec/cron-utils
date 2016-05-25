package com.cronutils.model.field.expression.visitor;

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.model.field.value.FieldValue;
import com.google.common.base.Function;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ValueMappingFieldExpressionVisitorTest {
    private FieldConstraints destinationConstraints;
    private ValueMappingFieldExpressionVisitor valueMappingFieldExpressionVisitor;

    @Before
    public void setUp() throws Exception {
        destinationConstraints = FieldConstraintsBuilder.instance().createConstraintsInstance();
        Function<FieldValue, FieldValue> transform = new Function<FieldValue, FieldValue>() {
            @Override
            public FieldValue apply(FieldValue input) {
                return input;
            }
        };
        this.valueMappingFieldExpressionVisitor = new ValueMappingFieldExpressionVisitor(transform);
    }

    @Test
    public void testVisitAlways() throws Exception {

    }

    @Test
    public void testVisitAnd() throws Exception {

    }

    @Test
    public void testVisitBetween() throws Exception {

    }

    @Test
    public void testVisitEvery() throws Exception {

    }

    @Test
    public void testVisitOn() throws Exception {

    }

    @Test
    public void testVisitQuestionMark() throws Exception {
        QuestionMark param = new QuestionMark();
        QuestionMark questionMark = (QuestionMark) valueMappingFieldExpressionVisitor.visit(param);
        assertTrue(param!=questionMark);//not same instance
    }
}