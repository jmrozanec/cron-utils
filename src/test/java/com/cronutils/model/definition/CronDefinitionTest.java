package com.cronutils.model.definition;

import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.FieldDefinition;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
/*
 * Copyright 2015 jmrozanec
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CronDefinitionTest {
    private boolean enforceStrictRange;
    private CronFieldName testFieldName1;
    private CronFieldName testFieldName2;
    private CronFieldName testFieldName3;
    @Mock
    private FieldDefinition mockFieldDefinition1;
    @Mock
    private FieldDefinition mockFieldDefinition2;
    @Mock
    private FieldDefinition mockFieldDefinition3optional;

    @Before
    public void setUp(){
        testFieldName1 = CronFieldName.SECOND;
        testFieldName2 = CronFieldName.MINUTE;
        testFieldName3 = CronFieldName.HOUR;
        MockitoAnnotations.initMocks(this);
        when(mockFieldDefinition1.getFieldName()).thenReturn(testFieldName1);
        when(mockFieldDefinition2.getFieldName()).thenReturn(testFieldName2);
        when(mockFieldDefinition3optional.getFieldName()).thenReturn(testFieldName3);
        when(mockFieldDefinition3optional.isOptional()).thenReturn(Boolean.TRUE);

        enforceStrictRange = false;
    }


    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldsParameter() throws Exception {
        new CronDefinition(null, Sets.newHashSet(), enforceStrictRange);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullConstraintsParameter() throws Exception {
        new CronDefinition(Lists.newArrayList(), null, enforceStrictRange);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyFieldsParameter() throws Exception {
        new CronDefinition(new ArrayList<>(), Sets.newHashSet(), enforceStrictRange);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLastFieldOptionalNotAllowedOnSingleFieldDefinition() throws Exception {
        List<FieldDefinition> fields = Lists.newArrayList();
        fields.add(mockFieldDefinition3optional);
        new CronDefinition(fields, Sets.newHashSet(), enforceStrictRange);
    }

    @Test
    public void testGetFieldDefinitions() throws Exception {
        List<FieldDefinition> fields = Lists.newArrayList();
        fields.add(mockFieldDefinition1);
        CronDefinition cronDefinition = new CronDefinition(fields, Sets.newHashSet(), enforceStrictRange);
        assertNotNull(cronDefinition.getFieldDefinitions());
        assertEquals(1, cronDefinition.getFieldDefinitions().size());
        assertTrue(cronDefinition.getFieldDefinitions().contains(mockFieldDefinition1));
    }
}