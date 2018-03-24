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

package com.cronutils.model;

import javax.ejb.ScheduleExpression;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.mapper.CronMapper;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.definition.TestCronDefinitionsFactory;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.parser.CronParser;
import com.cronutils.utils.CronUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CronTest {
    private Cron cron;
    private CronFieldName testName;
    private List<CronField> fields;
    @Mock
    private CronField mockField;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testName = CronFieldName.SECOND;
        when(mockField.getField()).thenReturn(testName);
        fields = Collections.singletonList(mockField);
        cron = new SingleCron(mock(CronDefinition.class), fields);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldsParameter() {
        new SingleCron(mock(CronDefinition.class), null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullDefinitionParameter() {
        new SingleCron(null, fields);
    }

    @Test
    public void testRetrieveNonNullParameter() {
        assertEquals(mockField, cron.retrieve(testName));
    }

    @Test(expected = NullPointerException.class)
    public void testRetrieveNullParameter() {
        cron.retrieve(null);
    }

    @Test
    public void testRetrieveFieldsAsMap() {
        assertNotNull(cron.retrieveFieldsAsMap());
        assertEquals(1, cron.retrieveFieldsAsMap().size());
        assertTrue(cron.retrieveFieldsAsMap().containsKey(testName));
        assertEquals(mockField, cron.retrieveFieldsAsMap().get(testName));
    }

    @Test
    public void testAsString() {
        final String expressionString = "somestring";
        final FieldExpression mockFieldExpression = mock(FieldExpression.class);
        when(mockField.getExpression()).thenReturn(mockFieldExpression);
        when(mockFieldExpression.asString()).thenReturn(expressionString);
        assertEquals(expressionString, cron.asString());
    }

    @Test
    public void testEquivalent() {
        final CronDefinition unixcd = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        final CronDefinition quartzcd = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        final CronParser unix = new CronParser(unixcd);
        final CronParser quartz = new CronParser(quartzcd);
        final Cron cron1 = unix.parse("* * * * MON");
        final Cron cron2 = unix.parse("*/1 * * * 1");
        final Cron cron3 = unix.parse("0 * * * *");
        final Cron cron4 = quartz.parse("0 * * ? * MON *");

        assertTrue(cron1.equivalent(CronMapper.sameCron(unixcd), cron2));
        assertFalse(cron1.equivalent(CronMapper.sameCron(unixcd), cron3));
        assertTrue(cron1.equivalent(CronMapper.fromQuartzToCron4j(), cron4));
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        final CronDefinition cron4jcd = CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J);
        final CronDefinition unixcd = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        final CronDefinition quartzcd = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        final CronParser unix = new CronParser(unixcd);
        final CronParser quartz = new CronParser(quartzcd);
        final CronParser cron4j = new CronParser(cron4jcd);

        final Cron[] toTest = new Cron[] {
                unix.parse("* * * * MON"),
                unix.parse("*/1 * * * 1"),
                unix.parse("0 * * * *"),
                unix.parse("*/2 * * * *"),
                quartz.parse("0 * * ? * MON *"),
                cron4j.parse("* 1 1,2 * 4"),
                cron4j.parse("* 1 1-2 * 4"),
                cron4j.parse("0 18 * * 1"),
                cron4j.parse("0/15 * * * *"),
                cron4j.parse("0 0/2 * * *"),
                cron4j.parse("0 6 * * MON-FRI")
        };

        for (final Cron expected : toTest) {
            final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try (ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {
                objOut.writeObject(expected);
            }

            try (ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()))) {
                final Cron actual = (Cron) objIn.readObject();
                assertEquals(expected.asString(), actual.asString());
            }
        }
    }

    @Test
    public void testAsScheduleExpression(){
        final CronDefinition cronDefinition = TestCronDefinitionsFactory.quartzNoDoWAndDoMRestrictionBothSameTime();
        final CronParser cronParser = new CronParser(cronDefinition);
        final Cron cron = cronParser.parse("0 * * 1 * MON *");
        ScheduleExpression expression = CronUtils.asScheduleExpression(cron);

        assertNotNull(expression);
        assertEquals(cron.retrieve(CronFieldName.SECOND).getExpression().asString(), expression.getSecond());
        assertEquals(cron.retrieve(CronFieldName.MINUTE).getExpression().asString(), expression.getMinute());
        assertEquals(cron.retrieve(CronFieldName.HOUR).getExpression().asString(), expression.getHour());
        assertEquals(cron.retrieve(CronFieldName.DAY_OF_MONTH).getExpression().asString(), expression.getDayOfMonth());
        assertEquals(cron.retrieve(CronFieldName.MONTH).getExpression().asString(), expression.getMonth());
        assertEquals(cron.retrieve(CronFieldName.DAY_OF_WEEK).getExpression().asString(), expression.getDayOfWeek());
        assertEquals(cron.retrieve(CronFieldName.YEAR).getExpression().asString(), expression.getYear());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAsScheduleExpressionQuestionMarkFails() {
        final CronDefinition quartzcd = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        final CronParser quartz = new CronParser(quartzcd);
        final Cron cron = quartz.parse("0 * * ? * MON *");
        CronUtils.asScheduleExpression(cron);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAsScheduleExpressionDoYNotSupported() {
        final CronDefinition cronDefinition = TestCronDefinitionsFactory.withDayOfYearDefinitionWhereNoQuestionMarkSupported();
        final CronParser cronParser= new CronParser(cronDefinition);
        final Cron cron = cronParser.parse("0 0 0 1 1-3 * * 1/14");
        CronUtils.asScheduleExpression(cron);
    }

    //@Test TODO
    public void testIssue308(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        Cron quartzCron = parser.parse("0 0 11 L-2 * ?");
        CronDescriptor descriptor = CronDescriptor.instance(Locale.ENGLISH);
        String description = descriptor.describe(quartzCron);

        // not sure what the exact string 'should' be ..
        assertEquals( "at 11:00 two days before the last day of month", description);
    }
}
