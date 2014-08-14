package com.cron.utils.parser.field;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

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
    }
}