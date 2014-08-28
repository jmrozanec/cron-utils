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
    private CronFieldName testFieldName1;
    private CronFieldName testFieldName2;
    @Mock
    private FieldDefinition mockFieldDefinition1;
    @Mock
    private FieldDefinition mockFieldDefinition2;

    @Before
    public void setUp(){
        testFieldName1 = CronFieldName.SECOND;
        testFieldName2 = CronFieldName.MINUTE;
        MockitoAnnotations.initMocks(this);
        when(mockFieldDefinition1.getFieldName()).thenReturn(testFieldName1);
        when(mockFieldDefinition2.getFieldName()).thenReturn(testFieldName2);

        lastFieldOptional = false;
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
    public void testLastFieldOptionalTrueWhenSet() throws Exception {
        lastFieldOptional = true;
        List<FieldDefinition> fields = Lists.newArrayList();
        fields.add(mockFieldDefinition1);
        fields.add(mockFieldDefinition2);
        assertEquals(lastFieldOptional, new CronDefinition(fields, lastFieldOptional).isLastFieldOptional());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLastFieldOptionalNotAllowedOnSingleFieldDefinition() throws Exception {
        lastFieldOptional = true;
        List<FieldDefinition> fields = Lists.newArrayList();
        fields.add(mockFieldDefinition1);
        new CronDefinition(fields, lastFieldOptional).isLastFieldOptional();
    }

    @Test
    public void testGetFieldDefinitions() throws Exception {
        List<FieldDefinition> fields = Lists.newArrayList();
        fields.add(mockFieldDefinition1);
        CronDefinition cronDefinition = new CronDefinition(fields, lastFieldOptional);
        assertNotNull(cronDefinition.getFieldDefinitions());
        assertEquals(1, cronDefinition.getFieldDefinitions().size());
        assertTrue(cronDefinition.getFieldDefinitions().contains(mockFieldDefinition1));
    }
}