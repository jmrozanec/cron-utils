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

package com.cronutils.model.field;

import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.model.field.definition.FieldDefinitionBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Disabled
public class FieldDefinitionBuilderTest {
    private CronFieldName testFieldName;
    @Mock
    private CronDefinitionBuilder mockParserBuilder;
    @Mock
    private FieldConstraintsBuilder mockConstraintsBuilder;

    private FieldDefinitionBuilder fieldDefinitionBuilder;

    private MockedStatic<FieldConstraintsBuilder> mockedFieldConstraintsBuilder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testFieldName = CronFieldName.SECOND;

        when(mockConstraintsBuilder.forField(any(CronFieldName.class))).thenReturn(mockConstraintsBuilder);
        mockedFieldConstraintsBuilder = Mockito.mockStatic(FieldConstraintsBuilder.class);
        mockedFieldConstraintsBuilder.when(FieldConstraintsBuilder::instance).thenReturn(mockConstraintsBuilder);
        fieldDefinitionBuilder = new FieldDefinitionBuilder(mockParserBuilder, testFieldName);
    }

    @AfterEach
    public void tearDown() {
        mockedFieldConstraintsBuilder.close();
    }

    @Test
    public void testWithIntMapping() {
        final int source = 7;
        final int dest = 0;

        fieldDefinitionBuilder.withIntMapping(source, dest);

        verify(mockConstraintsBuilder).withIntValueMapping(source, dest);
    }

    @Test
    public void testAnd() {
        final FieldConstraints constraints = mock(FieldConstraints.class);
        when(mockConstraintsBuilder.createConstraintsInstance()).thenReturn(constraints);
        final ArgumentCaptor<FieldDefinition> argument = ArgumentCaptor.forClass(FieldDefinition.class);

        fieldDefinitionBuilder.and();

        verify(mockParserBuilder).register(argument.capture());
        assertEquals(testFieldName, argument.getValue().getFieldName());
        verify(mockConstraintsBuilder).createConstraintsInstance();
    }

    @Test
    public void testConstructorNullParserBuilder() {
        assertThrows(NullPointerException.class, () -> new FieldDefinitionBuilder(null, testFieldName));
    }

    @Test
    public void testConstructorNullTestFieldName() {
        assertThrows(NullPointerException.class, () -> new FieldDefinitionBuilder(mockParserBuilder, null));
    }
}
