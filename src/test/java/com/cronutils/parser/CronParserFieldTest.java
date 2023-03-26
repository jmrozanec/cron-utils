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

package com.cronutils.parser;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.FieldExpression;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled
public class CronParserFieldTest {

    private CronFieldName testFieldName;
    @Mock
    private FieldConstraints mockConstraints;
    private FieldParser mockParser;
    private MockedConstruction<FieldParser> mockedConstruction;
    @Mock
    private FieldExpression mockParseResponse;

    private CronParserField cronParserField;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testFieldName = CronFieldName.SECOND;

        mockedConstruction = Mockito.mockConstruction(FieldParser.class, (mock, context) -> {
            Mockito.when(mock.parse(ArgumentMatchers.anyString())).thenReturn(mockParseResponse);
            mockParser = mock;
        });

        cronParserField = new CronParserField(testFieldName, mockConstraints);
    }

    @AfterEach
    public void tearDown() {
        mockedConstruction.close();
    }

    @Test
    public void testGetField() {
        assertEquals(testFieldName, cronParserField.getField());
    }

    @Test
    public void testParse() {
        final String cron = UUID.randomUUID().toString();
        final CronField result = cronParserField.parse(cron);
        assertEquals(mockParseResponse, result.getExpression());
        assertEquals(testFieldName, result.getField());
        Mockito.verify(mockParser).parse(cron);
    }

    @Test
    public void testParse_lastDoWInteger() {
        cronParserField = new CronParserField(CronFieldName.DAY_OF_WEEK, mockConstraints);

        Mockito.when(mockConstraints.getStringMappingValue("1")).thenReturn(null);

        final CronField result = cronParserField.parse("1L");
        assertEquals(mockParseResponse, result.getExpression());
        assertEquals(CronFieldName.DAY_OF_WEEK, result.getField());

        Mockito.verify(mockConstraints).getStringMappingValue("1");
        Mockito.verify(mockParser).parse("1L");
    }

    @Test
    public void testParse_lastDoWString() {
        cronParserField = new CronParserField(CronFieldName.DAY_OF_WEEK, mockConstraints);

        Mockito.when(mockConstraints.getStringMappingValue("MON")).thenReturn(1);

        final CronField result = cronParserField.parse("MONL");
        assertEquals(mockParseResponse, result.getExpression());
        assertEquals(CronFieldName.DAY_OF_WEEK, result.getField());

        Mockito.verify(mockConstraints).getStringMappingValue("MON");
        Mockito.verify(mockParser).parse("1L");
    }

    @Test
    public void testConstructorNameNull() {
        assertThrows(NullPointerException.class, () -> new CronParserField(null, Mockito.mock(FieldConstraints.class)));
    }

    @Test
    public void testConstructorConstraintsNull() {
        assertThrows(NullPointerException.class, () -> new CronParserField(testFieldName, null));
    }
}
