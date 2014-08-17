package com.cron.utils.model;

import com.cron.utils.CronFieldName;
import com.cron.utils.parser.field.FieldConstraints;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;

public class FieldDefinitionTest {

    private CronFieldName testFieldName;
    @Mock
    private FieldConstraints mockConstraints;

    private FieldDefinition fieldDefinition;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testFieldName = CronFieldName.SECOND;
        fieldDefinition = new FieldDefinition(testFieldName, mockConstraints);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldName(){
        new FieldDefinition(null, mockConstraints);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullConstraints(){
        new FieldDefinition(testFieldName, null);
    }

    @Test
    public void testGetFieldName() throws Exception {
        assertEquals(testFieldName, fieldDefinition.getFieldName());
    }

    @Test
    public void testGetConstraints() throws Exception {
        assertEquals(mockConstraints, fieldDefinition.getConstraints());
    }
}