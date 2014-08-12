package com.cron.utils.parser.field;

import com.cron.utils.CronParameter;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CronFieldParseResultTest {

    private CronFieldParseResult result;
    private CronParameter cronParameter;
    private CronFieldExpression mockCronFieldExpression;

    @Before
    public void setUp(){
        cronParameter = CronParameter.SECOND;
        mockCronFieldExpression = mock(CronFieldExpression.class);
        result = new CronFieldParseResult(cronParameter, mockCronFieldExpression);
    }

    @Test
    public void testGetField() throws Exception {
        assertEquals(cronParameter, result.getField());
    }

    @Test
    public void testGetExpression() throws Exception {
        assertEquals(mockCronFieldExpression, result.getExpression());
    }

    @Test
    public void testCreateFieldComparator() throws Exception {
        Comparator<CronFieldParseResult> comparator = CronFieldParseResult.createFieldComparator();
        CronFieldParseResult mockResult1 = mock(CronFieldParseResult.class);
        CronParameter cronParameter1 = CronParameter.SECOND;

        CronFieldParseResult mockResult2 = mock(CronFieldParseResult.class);
        CronParameter cronParameter2 = cronParameter1;

        when(mockResult1.getField()).thenReturn(cronParameter1);
        when(mockResult2.getField()).thenReturn(cronParameter2);

        assertEquals(cronParameter1, cronParameter2);
        assertEquals(0, comparator.compare(mockResult1, mockResult2));

        cronParameter2 = CronParameter.MINUTE;

        when(mockResult1.getField()).thenReturn(cronParameter1);
        when(mockResult2.getField()).thenReturn(cronParameter2);

        assertNotEquals(cronParameter1, cronParameter2);
        assertTrue(0 != comparator.compare(mockResult1, mockResult2));
    }
}
