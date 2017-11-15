package com.cronutils.model.definition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.FieldDefinition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
    private boolean matchDayOfWeekAndDayOfMonth;
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
    public void setUp() {
        testFieldName1 = CronFieldName.SECOND;
        testFieldName2 = CronFieldName.MINUTE;
        testFieldName3 = CronFieldName.HOUR;
        MockitoAnnotations.initMocks(this);
        when(mockFieldDefinition1.getFieldName()).thenReturn(testFieldName1);
        when(mockFieldDefinition2.getFieldName()).thenReturn(testFieldName2);
        when(mockFieldDefinition3optional.getFieldName()).thenReturn(testFieldName3);
        when(mockFieldDefinition3optional.isOptional()).thenReturn(Boolean.TRUE);

        enforceStrictRange = false;
        matchDayOfWeekAndDayOfMonth = false;
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldsParameter() throws Exception {
        new CronDefinition(null, new HashSet<>(), enforceStrictRange, matchDayOfWeekAndDayOfMonth);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullConstraintsParameter() throws Exception {
        new CronDefinition(new ArrayList<>(), null, enforceStrictRange, matchDayOfWeekAndDayOfMonth);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyFieldsParameter() throws Exception {
        new CronDefinition(new ArrayList<>(), new HashSet<>(), enforceStrictRange, matchDayOfWeekAndDayOfMonth);
    }

    @Test
    public void testLastFieldOptionalTrueWhenSet() throws Exception {
        List<FieldDefinition> fields = new ArrayList<>();
        fields.add(mockFieldDefinition1);
        fields.add(mockFieldDefinition2);
        fields.add(mockFieldDefinition3optional);
        Set<FieldDefinition> fieldDefinitions = new CronDefinition(fields, new HashSet<>(), enforceStrictRange, matchDayOfWeekAndDayOfMonth)
                .getFieldDefinitions();
        List<FieldDefinition> sortedFieldDefinitions = new ArrayList<>(fieldDefinitions);
        sortedFieldDefinitions.sort(FieldDefinition.createFieldDefinitionComparator());
        assertTrue(sortedFieldDefinitions.get(fields.size() - 1).isOptional());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLastFieldOptionalNotAllowedOnSingleFieldDefinition() throws Exception {
        List<FieldDefinition> fields = new ArrayList<>();
        fields.add(mockFieldDefinition3optional);
        new CronDefinition(fields, new HashSet<>(), enforceStrictRange, matchDayOfWeekAndDayOfMonth);
    }

    @Test
    public void testGetFieldDefinitions() throws Exception {
        List<FieldDefinition> fields = new ArrayList<>();
        fields.add(mockFieldDefinition1);
        CronDefinition cronDefinition = new CronDefinition(fields, new HashSet<>(), enforceStrictRange, matchDayOfWeekAndDayOfMonth);
        assertNotNull(cronDefinition.getFieldDefinitions());
        assertEquals(1, cronDefinition.getFieldDefinitions().size());
        assertTrue(cronDefinition.getFieldDefinitions().contains(mockFieldDefinition1));
    }
}
