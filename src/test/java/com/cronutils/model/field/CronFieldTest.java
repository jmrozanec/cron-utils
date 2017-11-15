package com.cronutils.model.field;

import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.FieldExpression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
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
public class CronFieldTest {

    private CronField result;
    private CronFieldName cronFieldName;
    @Mock
    private FieldExpression mockFieldExpression;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        cronFieldName = CronFieldName.SECOND;
        result = new CronField(cronFieldName, mockFieldExpression, FieldConstraintsBuilder.instance().createConstraintsInstance());
    }

    @Test
    public void testGetField() throws Exception {
        assertEquals(cronFieldName, result.getField());
    }

    @Test
    public void testGetExpression() throws Exception {
        assertEquals(mockFieldExpression, result.getExpression());
    }

    @Test
    public void testCreateFieldComparator() throws Exception {
        Comparator<CronField> comparator = CronField.createFieldComparator();
        CronField mockResult1 = mock(CronField.class);
        CronFieldName cronFieldName1 = CronFieldName.SECOND;

        CronField mockResult2 = mock(CronField.class);
        CronFieldName cronFieldName2 = cronFieldName1;

        when(mockResult1.getField()).thenReturn(cronFieldName1);
        when(mockResult2.getField()).thenReturn(cronFieldName2);

        assertEquals(cronFieldName1, cronFieldName2);
        assertEquals(0, comparator.compare(mockResult1, mockResult2));

        cronFieldName2 = CronFieldName.MINUTE;

        when(mockResult1.getField()).thenReturn(cronFieldName1);
        when(mockResult2.getField()).thenReturn(cronFieldName2);

        assertNotEquals(cronFieldName1, cronFieldName2);
        assertTrue(0 != comparator.compare(mockResult1, mockResult2));
    }
}
