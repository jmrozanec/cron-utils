package com.cron.utils.model;

import com.cron.utils.model.field.CronFieldName;
import com.cron.utils.model.field.CronField;
import com.cron.utils.model.field.FieldExpression;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CronTest {

    private Cron cron;
    private CronFieldName testName;
    @Mock
    private CronField mockField;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        testName = CronFieldName.SECOND;
        when(mockField.getField()).thenReturn(testName);
        List<CronField> fields = Lists.newArrayList();
        fields.add(mockField);
        cron = new Cron(fields);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldsParameter() throws Exception {
        new Cron(null);
    }

    @Test
    public void testRetrieveNonNullParameter() throws Exception {
        assertEquals(mockField, cron.retrieve(testName));
    }

    @Test(expected = NullPointerException.class)
    public void testRetrieveNullParameter() throws Exception {
        cron.retrieve(null);
    }

    @Test
    public void testRetrieveFieldsAsMap() throws Exception {
        assertNotNull(cron.retrieveFieldsAsMap());
        assertEquals(1, cron.retrieveFieldsAsMap().size());
        assertTrue(cron.retrieveFieldsAsMap().containsKey(testName));
        assertEquals(mockField, cron.retrieveFieldsAsMap().get(testName));
    }

    @Test
    public void testAsString() throws Exception {
        String expressionString = "somestring";
        FieldExpression mockFieldExpression = mock(FieldExpression.class);
        when(mockField.getExpression()).thenReturn(mockFieldExpression);
        when(mockFieldExpression.asString()).thenReturn(expressionString);
        assertEquals(expressionString, cron.asString());
    }
}