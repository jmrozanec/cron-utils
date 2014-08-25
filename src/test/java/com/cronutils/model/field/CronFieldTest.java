package com.cronutils.model.field;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Comparator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CronFieldTest {

    private CronField result;
    private CronFieldName cronFieldName;
    @Mock
    private FieldExpression mockFieldExpression;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        cronFieldName = CronFieldName.SECOND;
        result = new CronField(cronFieldName, mockFieldExpression);
    }

    @Test
    public void testGetField() throws Exception {
        assertEquals(cronFieldName, result.getField());
    }

    @Test
    public void testGetExpression() throws Exception {
        assertEquals(mockFieldExpression, result.getExpression());
    }

    @Test
    public void testCreateFieldComparator() throws Exception {
        Comparator<CronField> comparator = CronField.createFieldComparator();
        CronField mockResult1 = mock(CronField.class);
        CronFieldName cronFieldName1 = CronFieldName.SECOND;

        CronField mockResult2 = mock(CronField.class);
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
