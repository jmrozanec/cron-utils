package com.cronutils.parser.field;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.parser.CronParser;
import com.cronutils.parser.CronParserField;
import com.cronutils.parser.FieldParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
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
@PrepareForTest({CronParserField.class, CronParser.class})
public class CronParserFieldTest {

    private CronFieldName testFieldName;
    @Mock
    private FieldParser mockParser;
    @Mock
    private FieldExpression mockParseResponse;

    private CronParserField cronParserField;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testFieldName = CronFieldName.SECOND;

        when(mockParser.parse(anyString())).thenReturn(mockParseResponse);
        PowerMockito.whenNew(FieldParser.class)
                .withArguments(any(FieldConstraints.class)).thenReturn(mockParser);

        cronParserField = new CronParserField(testFieldName, mock(FieldConstraints.class));
    }

    @Test
    public void testGetField() throws Exception {
        assertEquals(testFieldName, cronParserField.getField());
    }

    @Test
    public void testParse() throws Exception {
        String cron = UUID.randomUUID().toString();
        CronField result = cronParserField.parse(cron);
        assertEquals(mockParseResponse, result.getExpression());
        assertEquals(testFieldName, result.getField());
        verify(mockParser).parse(cron);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNameNull() throws Exception {
        new CronParserField(null, mock(FieldConstraints.class));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorConstraintsNull() throws Exception {
        new CronParserField(testFieldName, null);
    }
}
