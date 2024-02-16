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

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CronTest {
    private Cron cron;
    private CronFieldName testName;
    private List<CronField> fields;
    @Mock
    private CronField mockField;

    @BeforeEach
    public void setUp() {
        testName = CronFieldName.SECOND;
        when(mockField.getField()).thenReturn(testName);
        fields = Collections.singletonList(mockField);
        cron = new SingleCron(mock(CronDefinition.class), fields);
    }

    @Test
    public void testConstructorNullFieldsParameter() {
        assertThrows(NullPointerException.class, () -> new SingleCron(mock(CronDefinition.class), null));
    }

    @Test
    public void testConstructorNullDefinitionParameter() {
        assertThrows(NullPointerException.class, () -> new SingleCron(null, fields));
    }

    @Test
    public void testRetrieveNonNullParameter() {
        assertEquals(mockField, cron.retrieve(testName));
    }

    @Test
    public void testRetrieveNullParameter() {
        assertThrows(NullPointerException.class, () -> cron.retrieve(null));
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

}
