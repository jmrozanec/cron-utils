package com.cron.utils.model;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CronDefinitionTest {
    private boolean lastFieldOptional;
    @Mock
    private FieldDefinition mockFieldDefinition;

    private CronDefinition cronDefinition;

    @Before
    public void setUp(){
        lastFieldOptional = true;
        List<FieldDefinition> fields = Lists.newArrayList();
        fields.add(mockFieldDefinition);
        cronDefinition = new CronDefinition(fields, lastFieldOptional);
    }


    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldsParameter() throws Exception {
        new CronDefinition(null, lastFieldOptional);
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