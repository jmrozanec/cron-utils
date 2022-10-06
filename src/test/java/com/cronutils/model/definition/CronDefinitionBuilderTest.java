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

package com.cronutils.model.definition;

import com.cronutils.builder.CronBuilder;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.model.field.expression.Weekdays;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static com.cronutils.model.field.expression.FieldExpressionFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CronDefinitionBuilderTest {

    private CronDefinitionBuilder builder;

    @BeforeEach
    public void setUp() {
        builder = CronDefinitionBuilder.defineCron();
    }

    @Test
    public void testDefineCron() {
        assertNotNull(CronDefinitionBuilder.defineCron());
        assertEquals(CronDefinitionBuilder.class, CronDefinitionBuilder.defineCron().getClass());
    }

    @Test
    public void testWithSeconds() {
        final Set<FieldDefinition> fieldDefinitions = builder.withSeconds().and().instance().getFieldDefinitions();
        assertNotNull(fieldDefinitions);
        assertEquals(1, fieldDefinitions.size());
        assertEquals(CronFieldName.SECOND, fieldDefinitions.iterator().next().getFieldName());
    }

    @Test
    public void testWithMinutes() {
        final Set<FieldDefinition> fieldDefinitions = builder.withMinutes().and().instance().getFieldDefinitions();
        assertNotNull(fieldDefinitions);
        assertEquals(1, fieldDefinitions.size());
        assertEquals(CronFieldName.MINUTE, fieldDefinitions.iterator().next().getFieldName());
    }

    @Test
    public void testWithHours() {
        final Set<FieldDefinition> fieldDefinitions = builder.withHours().and().instance().getFieldDefinitions();
        assertNotNull(fieldDefinitions);
        assertEquals(1, fieldDefinitions.size());
        assertEquals(CronFieldName.HOUR, fieldDefinitions.iterator().next().getFieldName());
    }

    @Test
    public void testWithDayOfMonth() {
        final Set<FieldDefinition> fieldDefinitions = builder.withDayOfMonth().and().instance().getFieldDefinitions();
        assertNotNull(fieldDefinitions);
        assertEquals(1, fieldDefinitions.size());
        assertEquals(CronFieldName.DAY_OF_MONTH, fieldDefinitions.iterator().next().getFieldName());
    }

    @Test
    public void testWithMonth() {
        final Set<FieldDefinition> fieldDefinitions = builder.withMonth().and().instance().getFieldDefinitions();
        assertNotNull(fieldDefinitions);
        assertEquals(1, fieldDefinitions.size());
        assertEquals(CronFieldName.MONTH, fieldDefinitions.iterator().next().getFieldName());
    }

    @Test
    public void testWithDayOfWeek() {
        final Set<FieldDefinition> fieldDefinitions = builder.withDayOfWeek().and().instance().getFieldDefinitions();
        assertNotNull(fieldDefinitions);
        assertEquals(1, fieldDefinitions.size());
        assertEquals(CronFieldName.DAY_OF_WEEK, fieldDefinitions.iterator().next().getFieldName());
    }

    @Test
    public void testWithYear() {
        final Set<FieldDefinition> fieldDefinitions = builder.withYear().and().instance().getFieldDefinitions();
        assertNotNull(fieldDefinitions);
        assertEquals(1, fieldDefinitions.size());
        assertEquals(CronFieldName.YEAR, fieldDefinitions.iterator().next().getFieldName());
    }

    @Test
    public void testLastFieldOptionalFalseByDefault() {
        final CronDefinition definition = builder.withHours().and().instance();
        assertNotNull(definition);
    }

    @Test
    public void testRegister() {
        final FieldDefinition testFieldDefinition =
                new FieldDefinition(
                        CronFieldName.SECOND,
                        new FieldConstraints(
                                Collections.emptyMap(),
                                Collections.emptyMap(),
                                Collections.emptySet(), 0, 1, true)
                );
        builder.register(testFieldDefinition);
        final Set<FieldDefinition> definitions = builder.instance().getFieldDefinitions();
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertEquals(testFieldDefinition, definitions.iterator().next());
    }

    @Test
    public void testInstanceDefinitionForUnix() {
        assertNotNull(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
    }

    @Test
    public void testInstanceDefinitionForQuartz() {
        assertNotNull(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    @Test
    public void testInstanceDefinitionForCron4j() {
        assertNotNull(CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J));
    }

    @Test
    public void testInstanceDefinitionForUnknownValue() {
        assertThrows(RuntimeException.class, () -> assertNotNull(CronDefinitionBuilder.instanceDefinitionFor(null)));
    }

    @Test
    public void testCronDefinitionShouldNotAcceptQuestionmark() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        final CronParser parser = new CronParser(cronDefinition);
        assertThrows(IllegalArgumentException.class, () -> parser.parse("* * * * ?"));
    }

    @Test
    public void testCronDefinitionShouldNotAcceptMultipleOptionalFields() {
        assertThrows(IllegalArgumentException.class, () -> CronDefinitionBuilder.defineCron()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().optional().and()
                .withMonth().optional().and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).and()
                .instance());
    }

    /**
     * Test for issue https://github.com/jmrozanec/cron-utils/issues/315
     * We need to provide means to easily perform accurate DoW values mapping when building a cron expression.
     */
    @Test
    public void testDoWProperWeekdayOffset(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronBuilder builder = CronBuilder.cron(cronDefinition)
                .withYear(always())
                .withMonth(always())
                .withDoW(on(Weekdays.FRIDAY.getWeekday(cronDefinition)))
                .withDoM(questionMark())
                .withHour(on(12))
                .withMinute(on(0))
                .withSecond(on(0));

        Cron cron = builder.instance();
        String result = cron.asString();

        assertEquals("0 0 12 ? * 6 *", result);
    }

    /**
     * Test for issue https://github.com/jmrozanec/cron-utils/issues/508
     * DoW as Sunday using QUARTZ definition was going out of range [1,7]
     */
    @Test
    public void testDoWProperWeekdayOffsetForSunday(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronBuilder builder = CronBuilder.cron(cronDefinition)
                .withYear(always())
                .withMonth(always())
                .withDoW(on(Weekdays.SUNDAY.getWeekday(cronDefinition)))
                .withDoM(questionMark())
                .withHour(on(12))
                .withMinute(on(0))
                .withSecond(on(0));

        Cron cron = builder.instance();
        String result = cron.asString();

        assertEquals("0 0 12 ? * 1 *", result);
    }

    @Test
    public void testSpringSchedule(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING);
        CronBuilder builder = CronBuilder.cron(cronDefinition)
                .withDoW(questionMark())
                .withMonth(always())
                .withDoM(always())
                .withHour(always())
                .withMinute(every(on(0), 5))
                .withSecond(always());

        Cron cron = builder.instance();
        String result = cron.asString();

        assertEquals("* 0/5 * * * ?", result);
    }

    @Test
    public void testSpring53ScheduleExample1(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        CronBuilder builder = CronBuilder.cron(cronDefinition)
                .withDoW(always())
                .withMonth(always())
                .withDoM(on(SpecialChar.L))
                .withHour(on(0))
                .withMinute(on(0))
                .withSecond(on(0));

        Cron cron = builder.instance();
        String result = cron.asString();

        assertEquals("0 0 0 L * *", result);
    }

    @Test
    public void testSpring53ScheduleExample2(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        CronBuilder builder = CronBuilder.cron(cronDefinition)
                .withDoW(always())
                .withMonth(always())
                .withDoM(between(SpecialChar.L, 3))
                .withHour(on(0))
                .withMinute(on(0))
                .withSecond(on(0));

        Cron cron = builder.instance();
        String result = cron.asString();

        assertEquals("0 0 0 L-3 * *", result);
    }

    @Test
    public void testSpring53ScheduleExample3(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        CronBuilder builder = CronBuilder.cron(cronDefinition)
                .withDoW(on(5, SpecialChar.L))
                .withMonth(always())
                .withDoM(always())
                .withHour(on(0))
                .withMinute(on(0))
                .withSecond(on(0));

        Cron cron = builder.instance();
        String result = cron.asString();

        assertEquals("0 0 0 * * 5L", result);
    }

    @Test
    public void testSpring53ScheduleExample4(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        CronBuilder builder = CronBuilder.cron(cronDefinition)
                .withDoW(on(4, SpecialChar.L))
                .withMonth(always())
                .withDoM(always())
                .withHour(on(0))
                .withMinute(on(0))
                .withSecond(on(0));

        Cron cron = builder.instance();
        CronParser parser = new CronParser(cronDefinition);
        Cron parsedCron = parser.parse("0 0 0 * * THUL");

        assertEquals(cron.asString(), parsedCron.asString());
    }

    @Test
    public void testSpring53ScheduleExample5(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        CronBuilder builder = CronBuilder.cron(cronDefinition)
                .withDoW(always())
                .withMonth(always())
                .withDoM(on(1, SpecialChar.W))
                .withHour(on(0))
                .withMinute(on(0))
                .withSecond(on(0));

        Cron cron = builder.instance();
        String result = cron.asString();

        assertEquals("0 0 0 1W * *", result);
    }

    @Test
    public void testSpring53ScheduleExample6(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        CronBuilder builder = CronBuilder.cron(cronDefinition)
                .withDoW(always())
                .withMonth(always())
                .withDoM(on( SpecialChar.LW))
                .withHour(on(0))
                .withMinute(on(0))
                .withSecond(on(0));

        Cron cron = builder.instance();
        String result = cron.asString();

        assertEquals("0 0 0 LW * *", result);
    }

    @Test
    public void testSpring53ScheduleExample7(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        CronBuilder builder = CronBuilder.cron(cronDefinition)
                .withDoW(on(5, SpecialChar.HASH, 2))
                .withMonth(always())
                .withDoM(on(SpecialChar.QUESTION_MARK))
                .withHour(on(0))
                .withMinute(on(0))
                .withSecond(on(0));

        Cron cron = builder.instance();
        String result = cron.asString();

        assertEquals("0 0 0 ? * 5#2", result);
    }

    /**
     * Corresponds to issue #527
     * https://github.com/jmrozanec/cron-utils/issues/527
     */
    @Test
    public void testSpring53ScheduleExample8(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        CronBuilder builder = CronBuilder.cron(cronDefinition)
                .withDoW(on(1, SpecialChar.HASH, 1))
                .withMonth(always())
                .withDoM(on(SpecialChar.QUESTION_MARK))
                .withHour(on(0))
                .withMinute(on(0))
                .withSecond(on(0));

        Cron cron = builder.instance();
        CronParser parser = new CronParser(cronDefinition);
        Cron parsedCron = parser.parse("0 0 0 ? * MON#1");

        assertEquals(cron.asString(), parsedCron.asString());
    }

    @Test
    public void testSpring53ScheduleYearly(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        CronParser parser = new CronParser(cronDefinition);
        parser.parse("@yearly");
    }

    @Test
    public void testSpring53ScheduleAnnually(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        CronParser parser = new CronParser(cronDefinition);
        parser.parse("@annually");
    }

    @Test
    public void testSpring53ScheduleMonthly(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        CronParser parser = new CronParser(cronDefinition);
        parser.parse("@monthly");
    }

    @Test
    public void testSpring53ScheduleWeekly(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        CronParser parser = new CronParser(cronDefinition);
        parser.parse("@weekly");
    }

    @Test
    public void testSpring53ScheduleDaily(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        CronParser parser = new CronParser(cronDefinition);
        parser.parse("@daily");
    }

    @Test
    public void testSpring53ScheduleMidnight(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        CronParser parser = new CronParser(cronDefinition);
        parser.parse("@midnight");
    }

    @Test
    public void testSpring53ScheduleHourly(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53);
        CronParser parser = new CronParser(cronDefinition);
        parser.parse("@hourly");
    }

    @Test
    public void testScheduleReboot(){
        CronDefinition cronDefinition = builder.withSupportedNicknameReboot().instance();
        CronParser parser = new CronParser(cronDefinition);
        parser.parse("@reboot");
    }
}
