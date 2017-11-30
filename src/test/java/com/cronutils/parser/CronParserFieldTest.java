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

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.FieldExpression;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CronParserField.class, CronParser.class })
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

        Mockito.when(mockParser.parse(Matchers.anyString())).thenReturn(mockParseResponse);
        PowerMockito.whenNew(FieldParser.class)
                .withArguments(Matchers.any(FieldConstraints.class)).thenReturn(mockParser);

        cronParserField = new CronParserField(testFieldName, Mockito.mock(FieldConstraints.class));
    }

    @Test
    public void testGetField() {
        Assert.assertEquals(testFieldName, cronParserField.getField());
    }

    @Test
    public void testParse() {
        final String cron = UUID.randomUUID().toString();
        final CronField result = cronParserField.parse(cron);
        Assert.assertEquals(mockParseResponse, result.getExpression());
        Assert.assertEquals(testFieldName, result.getField());
        Mockito.verify(mockParser).parse(cron);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNameNull() {
        new CronParserField(null, Mockito.mock(FieldConstraints.class));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorConstraintsNull() {
        new CronParserField(testFieldName, null);
    }
}
