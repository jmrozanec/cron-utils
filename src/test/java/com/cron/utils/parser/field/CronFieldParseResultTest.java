package com.cron.utils.parser.field;

import com.cron.utils.CronFieldName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Comparator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CronFieldParseResultTest {

    private CronFieldParseResult result;
    private CronFieldName cronFieldName;
    @Mock
    private CronFieldExpression mockCronFieldExpression;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        cronFieldName = CronFieldName.SECOND;
        result = new CronFieldParseResult(cronFieldName, mockCronFieldExpression);
    }

    @Test
    public void testGetField() throws Exception {
        assertEquals(cronFieldName, result.getField());
    }

    @Test
    public void testGetExpression() throws Exception {
        assertEquals(mockCronFieldExpression, result.getExpression());
    }

    @Test
    public void testCreateFieldComparator() throws Exception {
        Comparator<CronFieldParseResult> comparator = CronFieldParseResult.createFieldComparator();
        CronFieldParseResult mockResult1 = mock(CronFieldParseResult.class);
        CronFieldName cronFieldName1 = CronFieldName.SECOND;

        CronFieldParseResult mockResult2 = mock(CronFieldParseResult.class);
        CronFieldName cronFieldName2 = cronFieldName1;

        when(mockResult1.getField()).thenReturn(cronFieldName1);
        when(mockResult2.getField()).thenReturn(cronFieldName2);

        assertEquals(cronFieldName1, cronFieldName2);
        assertEquals(0, comparator.compare(mockResult1, mockResult2));

        cronFieldName2 = CronFieldName.MINUTE;

        when(mockResult1.getField()).thenReturn(cronFieldName1);
        when(mockResult2.getField()).thenReturn(cronFieldName2);

        assertNotEquals(cronFieldName1, cronFieldName2);
        assertTrue(0 != comparator.compare(mockResult1, mockResult2));
    }
}
