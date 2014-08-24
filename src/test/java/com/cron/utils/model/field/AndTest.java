package com.cron.utils.model.field;

import com.cron.utils.model.field.And;
import com.cron.utils.model.field.FieldExpression;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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