package com.cron.utils.parser.field;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class CronFieldExpressionTest {
    private TestCronFieldExpression testCronFieldExpression;
    @Mock
    private FieldConstraints mockFieldConstraints;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.testCronFieldExpression = new TestCronFieldExpression(mockFieldConstraints);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullConstraints() throws Exception {
        new TestCronFieldExpression(null);
    }

    @Test
    public void testAnd() throws Exception {
        CronFieldExpression mockExpression = mock(CronFieldExpression.class);
        And and = testCronFieldExpression.and(mockExpression);
        assertTrue(and.getExpressions().contains(mockExpression));
        assertTrue(and.getExpressions().contains(testCronFieldExpression));
    }

    @Test
    public void testGetConstraints() throws Exception {
        assertEquals(mockFieldConstraints, testCronFieldExpression.getConstraints());
    }

    class TestCronFieldExpression extends CronFieldExpression {

        public TestCronFieldExpression(FieldConstraints constraints) {
            super(constraints);
        }
    }
}