package com.cronutils.model.field;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.model.field.definition.FieldDefinitionBuilder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
@RunWith(PowerMockRunner.class)
@PrepareForTest({ FieldConstraintsBuilder.class, FieldDefinitionBuilder.class })
public class FieldDefinitionBuilderTest {
    private CronFieldName testFieldName;
    @Mock
    private CronDefinitionBuilder mockParserBuilder;
    @Mock
    private FieldConstraintsBuilder mockConstraintsBuilder;

    private FieldDefinitionBuilder fieldDefinitionBuilder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testFieldName = CronFieldName.SECOND;

        when(mockConstraintsBuilder.forField(any(CronFieldName.class))).thenReturn(mockConstraintsBuilder);
        PowerMockito.mockStatic(FieldConstraintsBuilder.class);
        PowerMockito.when(FieldConstraintsBuilder.instance()).thenReturn(mockConstraintsBuilder);

        fieldDefinitionBuilder = new FieldDefinitionBuilder(mockParserBuilder, testFieldName);
    }

    @Test
    public void testWithIntMapping() throws Exception {
        int source = 7;
        int dest = 0;

        fieldDefinitionBuilder.withIntMapping(source, dest);

        verify(mockConstraintsBuilder).withIntValueMapping(source, dest);
    }

    @Test
    public void testAnd() throws Exception {
        FieldConstraints constraints = mock(FieldConstraints.class);
        when(mockConstraintsBuilder.createConstraintsInstance()).thenReturn(constraints);
        ArgumentCaptor<FieldDefinition> argument = ArgumentCaptor.forClass(FieldDefinition.class);

        fieldDefinitionBuilder.and();

        verify(mockParserBuilder).register(argument.capture());
        assertEquals(testFieldName, argument.getValue().getFieldName());
        verify(mockConstraintsBuilder).createConstraintsInstance();
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullParserBuilder() {
        new FieldDefinitionBuilder(null, testFieldName);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullTestFieldName() {
        new FieldDefinitionBuilder(mockParserBuilder, null);
    }
}
