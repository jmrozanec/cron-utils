package com.cronutils.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.parser.CronParser;
import com.google.common.collect.Lists;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
        fields = Lists.newArrayList();
        fields.add(mockField);
        cron = new Cron(mock(CronDefinition.class), fields);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldsParameter() throws Exception {
        new Cron(mock(CronDefinition.class), null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullDefinitionParameter() throws Exception {
        new Cron(null, fields);
    }

    @Test
    public void testRetrieveNonNullParameter() throws Exception {
        assertEquals(mockField, cron.retrieve(testName));
    }

    @Test(expected = NullPointerException.class)
    public void testRetrieveNullParameter() throws Exception {
        cron.retrieve(null);
    }

    @Test
    public void testRetrieveFieldsAsMap() throws Exception {
        assertNotNull(cron.retrieveFieldsAsMap());
        assertEquals(1, cron.retrieveFieldsAsMap().size());
        assertTrue(cron.retrieveFieldsAsMap().containsKey(testName));
        assertEquals(mockField, cron.retrieveFieldsAsMap().get(testName));
    }

    @Test
    public void testAsString() throws Exception {
        String expressionString = "somestring";
        FieldExpression mockFieldExpression = mock(FieldExpression.class);
        when(mockField.getExpression()).thenReturn(mockFieldExpression);
        when(mockFieldExpression.asString()).thenReturn(expressionString);
        assertEquals(expressionString, cron.asString());
    }

    @Test
    public void testEquivalent() {
        CronDefinition unixcd = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronDefinition quartzcd = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser unix = new CronParser(unixcd);
        CronParser quartz = new CronParser(quartzcd);
        Cron cron1 = unix.parse("* * * * MON");
        Cron cron2 = unix.parse("*/1 * * * 1");
        Cron cron3 = unix.parse("0 * * * *");
        Cron cron4 = quartz.parse("0 * * ? * MON *");

        assertTrue(cron1.equivalent(CronMapper.sameCron(unixcd), cron2));
        assertFalse(cron1.equivalent(CronMapper.sameCron(unixcd), cron3));
        assertTrue(cron1.equivalent(CronMapper.fromQuartzToCron4j(), cron4));
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        CronDefinition cron4jcd = CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J);
        CronDefinition unixcd = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronDefinition quartzcd = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser unix = new CronParser(unixcd);
        CronParser quartz = new CronParser(quartzcd);
        CronParser cron4j = new CronParser(cron4jcd);

        Cron[] toTest = new Cron[] {
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

        for (Cron expected : toTest) {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try (ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {
                objOut.writeObject(expected);
            }

            try (ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()))) {
                Cron actual = (Cron) objIn.readObject();
                assertEquals(expected.asString(), actual.asString());
            }
        }
    }
}
