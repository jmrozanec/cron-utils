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

package com.cronutils.mapper;

import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.On;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class CronMapperTest {
    private CronFieldName testCronFieldName;
    @Mock
    private CronField mockCronField;

    @BeforeEach
    public void setUp() {
        testCronFieldName = CronFieldName.SECOND;
    }

    @Test
    public void testConstructorSourceDefinitionNull() {
        assertThrows(NullPointerException.class, () -> new CronMapper(mock(CronDefinition.class), null, null));
    }

    @Test
    public void testConstructorTargetDefinitionNull() {
        assertThrows(NullPointerException.class, () -> new CronMapper(null, mock(CronDefinition.class), null));
    }

    @Test
    public void testReturnSameExpression() {
        final Function<CronField, CronField> function = CronMapper.returnSameExpression();
        assertEquals(mockCronField, function.apply(mockCronField));
    }

    @Test
    public void testReturnOnZeroExpression() {
        final Function<CronField, CronField> function = CronMapper.returnOnZeroExpression(testCronFieldName);

        assertEquals(testCronFieldName, function.apply(mockCronField).getField());
        final On result = (On) function.apply(mockCronField).getExpression();
        assertEquals(0, (int) result.getTime().getValue());
    }

    @Test
    public void testReturnAlwaysExpression() {
        final Function<CronField, CronField> function = CronMapper.returnAlwaysExpression(testCronFieldName);

        assertEquals(testCronFieldName, function.apply(mockCronField).getField());
        assertEquals(Always.class, function.apply(mockCronField).getExpression().getClass());
    }
}
