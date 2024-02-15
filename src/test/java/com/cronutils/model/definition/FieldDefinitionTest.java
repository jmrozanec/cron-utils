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

package com.cronutils.model.definition;

import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.definition.FieldDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class FieldDefinitionTest {

    private CronFieldName testFieldName;
    @Mock
    private FieldConstraints mockConstraints;

    private FieldDefinition fieldDefinition;

    @BeforeEach
    public void setUp() {
        testFieldName = CronFieldName.SECOND;
        fieldDefinition = new FieldDefinition(testFieldName, mockConstraints);
    }

    @Test
    public void testConstructorNullFieldName() {
        assertThrows(NullPointerException.class, () -> new FieldDefinition(null, mockConstraints));
    }

    @Test
    public void testConstructorNullConstraints() {
        assertThrows(NullPointerException.class, () -> new FieldDefinition(testFieldName, null));
    }

    @Test
    public void testGetFieldName() {
        assertEquals(testFieldName, fieldDefinition.getFieldName());
    }

    @Test
    public void testGetConstraints() {
        assertEquals(mockConstraints, fieldDefinition.getConstraints());
    }

    @Test
    public void testCreateFieldDefinitionComparatorNotNull() {
        assertNotNull(FieldDefinition.createFieldDefinitionComparator());
    }

    @Test
    public void testCreateFieldDefinitionComparatorEqual() {
        final CronFieldName name = CronFieldName.DAY_OF_MONTH;
        final FieldDefinition fieldDefinition1 = new FieldDefinition(name, mockConstraints);
        final FieldDefinition fieldDefinition2 = new FieldDefinition(name, mock(FieldConstraints.class));
        assertEquals(name.getOrder(), name.getOrder());
        assertEquals(0, FieldDefinition.createFieldDefinitionComparator().compare(fieldDefinition1, fieldDefinition2));
    }

    @Test
    public void testCreateFieldDefinitionComparatorGreater() {
        final CronFieldName name1 = CronFieldName.DAY_OF_MONTH;
        final CronFieldName name2 = CronFieldName.SECOND;
        final FieldDefinition fieldDefinition1 = new FieldDefinition(name1, mockConstraints);
        final FieldDefinition fieldDefinition2 = new FieldDefinition(name2, mock(FieldConstraints.class));
        assertNotEquals(name1.getOrder(), name2.getOrder());
        assertTrue(FieldDefinition.createFieldDefinitionComparator().compare(fieldDefinition1, fieldDefinition2) > 0);
    }

    @Test
    public void testCreateFieldDefinitionComparatorLesser() {
        final CronFieldName name1 = CronFieldName.DAY_OF_MONTH;
        final CronFieldName name2 = CronFieldName.SECOND;
        final FieldDefinition fieldDefinition1 = new FieldDefinition(name1, mockConstraints);
        final FieldDefinition fieldDefinition2 = new FieldDefinition(name2, mock(FieldConstraints.class));
        assertNotEquals(name1.getOrder(), name2.getOrder());
        assertTrue(FieldDefinition.createFieldDefinitionComparator().compare(fieldDefinition2, fieldDefinition1) < 0);
    }
}
