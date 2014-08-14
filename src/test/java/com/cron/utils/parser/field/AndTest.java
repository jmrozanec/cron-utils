package com.cron.utils.parser.field;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

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
}