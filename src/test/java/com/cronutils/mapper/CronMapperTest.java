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

import com.cronutils.Function;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.On;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CronMapperTest {
    private static final CronFieldName TEST_CRON_FIELD_NAME = CronFieldName.SECOND;

    @Mock
    private CronField mockCronField;

    @Test
    void testConstructorSourceDefinitionNull() {
        assertThrows(NullPointerException.class, () -> new CronMapper(mock(CronDefinition.class), null, null));
    }

    @Test
    void testConstructorTargetDefinitionNull() {
        assertThrows(NullPointerException.class, () -> new CronMapper(null, mock(CronDefinition.class), null));
    }

    @Test
    void testReturnSameExpression() {
        final Function<CronField, CronField> function = CronMapper.returnSameExpression();
        assertEquals(mockCronField, function.apply(mockCronField));
    }

    @Test
    void testReturnOnZeroExpression() {
        final Function<CronField, CronField> function = CronMapper.returnOnZeroExpression(TEST_CRON_FIELD_NAME);

        assertEquals(TEST_CRON_FIELD_NAME, function.apply(mockCronField).getField());
        final On result = (On) function.apply(mockCronField).getExpression();
        assertEquals(0, (int) result.getTime().getValue());
    }

    @Test
    void testReturnAlwaysExpression() {
        final Function<CronField, CronField> function = CronMapper.returnAlwaysExpression(TEST_CRON_FIELD_NAME);

        assertEquals(TEST_CRON_FIELD_NAME, function.apply(mockCronField).getField());
        assertEquals(Always.class, function.apply(mockCronField).getExpression().getClass());
    }
}
