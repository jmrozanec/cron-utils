package com.cronutils.model.definition;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
public class CronDefinitionBuilderTest {

    private CronDefinitionBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = CronDefinitionBuilder.defineCron();
    }

    @Test
    public void testDefineCron() throws Exception {
        assertNotNull(CronDefinitionBuilder.defineCron());
        assertEquals(CronDefinitionBuilder.class, CronDefinitionBuilder.defineCron().getClass());
    }

    @Test
    public void testWithSeconds() throws Exception {
        Set<FieldDefinition> fieldDefinitions = builder.withSeconds().and().instance().getFieldDefinitions();
        assertNotNull(fieldDefinitions);
        assertEquals(1, fieldDefinitions.size());
        assertEquals(CronFieldName.SECOND, fieldDefinitions.iterator().next().getFieldName());
    }

    @Test
    public void testWithMinutes() throws Exception {
        Set<FieldDefinition> fieldDefinitions = builder.withMinutes().and().instance().getFieldDefinitions();
        assertNotNull(fieldDefinitions);
        assertEquals(1, fieldDefinitions.size());
        assertEquals(CronFieldName.MINUTE, fieldDefinitions.iterator().next().getFieldName());
    }

    @Test
    public void testWithHours() throws Exception {
        Set<FieldDefinition> fieldDefinitions = builder.withHours().and().instance().getFieldDefinitions();
        assertNotNull(fieldDefinitions);
        assertEquals(1, fieldDefinitions.size());
        assertEquals(CronFieldName.HOUR, fieldDefinitions.iterator().next().getFieldName());
    }

    @Test
    public void testWithDayOfMonth() throws Exception {
        Set<FieldDefinition> fieldDefinitions = builder.withDayOfMonth().and().instance().getFieldDefinitions();
        assertNotNull(fieldDefinitions);
        assertEquals(1, fieldDefinitions.size());
        assertEquals(CronFieldName.DAY_OF_MONTH, fieldDefinitions.iterator().next().getFieldName());
    }

    @Test
    public void testWithMonth() throws Exception {
        Set<FieldDefinition> fieldDefinitions = builder.withMonth().and().instance().getFieldDefinitions();
        assertNotNull(fieldDefinitions);
        assertEquals(1, fieldDefinitions.size());
        assertEquals(CronFieldName.MONTH, fieldDefinitions.iterator().next().getFieldName());
    }

    @Test
    public void testWithDayOfWeek() throws Exception {
        Set<FieldDefinition> fieldDefinitions = builder.withDayOfWeek().and().instance().getFieldDefinitions();
        assertNotNull(fieldDefinitions);
        assertEquals(1, fieldDefinitions.size());
        assertEquals(CronFieldName.DAY_OF_WEEK, fieldDefinitions.iterator().next().getFieldName());
    }

    @Test
    public void testWithYear() throws Exception {
        Set<FieldDefinition> fieldDefinitions = builder.withYear().and().instance().getFieldDefinitions();
        assertNotNull(fieldDefinitions);
        assertEquals(1, fieldDefinitions.size());
        assertEquals(CronFieldName.YEAR, fieldDefinitions.iterator().next().getFieldName());
    }

    @Test
    public void testLastFieldOptionalFalseByDefault() throws Exception {
        CronDefinition definition = builder.withHours().and().instance();
        assertNotNull(definition);
    }

    @Test
    public void testRegister() throws Exception {
        FieldDefinition testFieldDefinition =
                new FieldDefinition(
                        CronFieldName.SECOND,
                        new FieldConstraints(
                                Collections.emptyMap(),
                                Collections.emptyMap(),
                                Collections.emptySet(), 0, 1)
                );
        builder.register(testFieldDefinition);
        Set<FieldDefinition> definitions = builder.instance().getFieldDefinitions();
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertEquals(testFieldDefinition, definitions.iterator().next());
    }

    @Test
    public void testInstanceDefinitionForUnix() throws Exception {
        assertNotNull(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
    }

    @Test
    public void testInstanceDefinitionForQuartz() throws Exception {
        assertNotNull(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    @Test
    public void testInstanceDefinitionForCron4j() throws Exception {
        assertNotNull(CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J));
    }

    @Test(expected = RuntimeException.class)
    public void testInstanceDefinitionForUnknownValue() throws Exception {
        assertNotNull(CronDefinitionBuilder.instanceDefinitionFor(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCronDefinitionShouldNotAcceptQuestionmark() throws Exception {
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron quartzCron = parser.parse("* * * * ?");
        quartzCron.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCronDefinitionShouldNotAcceptMultipleOptionalFields() throws Exception {
        CronDefinitionBuilder.defineCron()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().optional().and()
                .withMonth().optional().and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).and()
                .enforceStrictRanges()
                .instance();
    }
}
