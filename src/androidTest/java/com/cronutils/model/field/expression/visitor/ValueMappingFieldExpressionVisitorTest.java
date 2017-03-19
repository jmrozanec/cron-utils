package com.cronutils.model.field.expression.visitor;

import android.support.test.runner.AndroidJUnit4;

import com.cronutils.BaseAndroidTest;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.model.field.value.FieldValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.cronutils.Function;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ValueMappingFieldExpressionVisitorTest {
    private ValueMappingFieldExpressionVisitor valueMappingFieldExpressionVisitor;

    @Before
    public void setUp() throws Exception {
        Function<FieldValue, FieldValue> transform = input -> input;
        this.valueMappingFieldExpressionVisitor = new ValueMappingFieldExpressionVisitor(transform);
    }

    @Test
    public void testVisitQuestionMark() throws Exception {
        QuestionMark param = new QuestionMark();
        QuestionMark questionMark = (QuestionMark) valueMappingFieldExpressionVisitor.visit(param);
        assertTrue(param!=questionMark);//not same instance
    }
}