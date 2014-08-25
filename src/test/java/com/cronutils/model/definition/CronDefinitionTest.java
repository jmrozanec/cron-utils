package com.cronutils.model.definition;

import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.FieldDefinition;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class CronDefinitionTest {
    private boolean lastFieldOptional;
    private CronFieldName testFieldName;
    @Mock
    private FieldDefinition mockFieldDefinition;

    private CronDefinition cronDefinition;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        when(mockFieldDefinition.getFieldName()).thenReturn(testFieldName);

        List<FieldDefinition> fields = Lists.newArrayList();
        fields.add(mockFieldDefinition);
        lastFieldOptional = true;
        testFieldName = CronFieldName.SECOND;

        cronDefinition = new CronDefinition(fields, lastFieldOptional);
    }


    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldsParameter() throws Exception {
        new CronDefinition(null, lastFieldOptional);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyFieldsParameter() throws Exception {
        new CronDefinition(new ArrayList<FieldDefinition>(), lastFieldOptional);
    }

    @Test
    public void testLastFieldOptional() throws Exception {
        assertEquals(lastFieldOptional, cronDefinition.isLastFieldOptional());
    }

    @Test
    public void testGetFieldDefinitions() throws Exception {
        assertNotNull(cronDefinition.getFieldDefinitions());
        assertEquals(1, cronDefinition.getFieldDefinitions().size());
        assertTrue(cronDefinition.getFieldDefinitions().contains(mockFieldDefinition));
    }
}