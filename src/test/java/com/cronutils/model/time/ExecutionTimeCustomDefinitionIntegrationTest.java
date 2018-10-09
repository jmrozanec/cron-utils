/*
 * Copyright 2015 jmrozanec Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */

package com.cronutils.model.time;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExecutionTimeCustomDefinitionIntegrationTest {

    private static final String NEXT_EXECUTION_NOT_PRESENT_ERROR = "next execution was not present";

    @Test
    public void testCronExpressionAfterHalf() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).and()
                .instance();

        final CronParser parser = new CronParser(cronDefinition);
        final Cron cron = parser.parse("*/30 * * * * *");

        final ZonedDateTime startDateTime = ZonedDateTime.of(2015, 8, 28, 12, 5, 44, 0, UTC);
        final ZonedDateTime expectedDateTime = ZonedDateTime.of(2015, 8, 28, 12, 6, 0, 0, UTC);

        final ExecutionTime executionTime = ExecutionTime.forCron(cron);

        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(startDateTime);
        if (nextExecution.isPresent()) {
            final ZonedDateTime nextExecutionDateTime = nextExecution.get();
            assertEquals(expectedDateTime, nextExecutionDateTime);
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    @Test
    public void testCronExpressionBeforeHalf() {

        final CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).and()
                .instance();

        final CronParser parser = new CronParser(cronDefinition);
        final Cron cron = parser.parse("0/30 * * * * *");

        final ZonedDateTime startDateTime = ZonedDateTime.of(2015, 8, 28, 12, 5, 14, 0, UTC);
        final ZonedDateTime expectedDateTime = ZonedDateTime.of(2015, 8, 28, 12, 5, 30, 0, UTC);

        final ExecutionTime executionTime = ExecutionTime.forCron(cron);

        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(startDateTime);
        if (nextExecution.isPresent()) {
            assertEquals(expectedDateTime, nextExecution.get());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Test for issue #38
     * https://github.com/jmrozanec/cron-utils/issues/38
     * Reported case: lastExecution and nextExecution do not work properly
     * Expected: should return expected date
     */
    @Test
    public void testCronExpressionEveryTwoHoursAsteriskSlash() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).and()
                .instance();

        final CronParser parser = new CronParser(cronDefinition);
        final Cron cron = parser.parse("0 0 */2 * * *");
        final ZonedDateTime startDateTime = ZonedDateTime.parse("2015-08-28T12:05:14.000-03:00");

        final Optional<ZonedDateTime> nextExecution = ExecutionTime.forCron(cron).nextExecution(startDateTime);
        if (nextExecution.isPresent()) {
            assertTrue(ZonedDateTime.parse("2015-08-28T14:00:00.000-03:00").compareTo(nextExecution.get()) == 0);
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Test for issue #38
     * https://github.com/jmrozanec/cron-utils/issues/38
     * Reported case: lastExecution and nextExecution do not work properly
     * Expected: should return expected date
     */
    @Test
    public void testCronExpressionEveryTwoHoursSlash() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).and()
                .instance();

        final CronParser parser = new CronParser(cronDefinition);
        final Cron cron = parser.parse("0 0 /2 * * *");
        final ZonedDateTime startDateTime = ZonedDateTime.parse("2015-08-28T12:05:14.000-03:00");

        final Optional<ZonedDateTime> nextExecution = ExecutionTime.forCron(cron).nextExecution(startDateTime);
        if (nextExecution.isPresent()) {
            assertTrue(ZonedDateTime.parse("2015-08-28T14:00:00.000-03:00").compareTo(nextExecution.get()) == 0);
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Test for issue #57
     * https://github.com/jmrozanec/cron-utils/issues/57
     * Reported case: BetweenDayOfWeekValueGenerator does not work for the first day of a month in some cases.
     * Expected: first day of month should be returned ok
     */
    @Test
    public void testCronExpressionBetweenDayOfWeekValueGeneratorCorrectFirstDayOfMonth() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth()
                .supportsL().supportsW()
                .and()
                .withMonth().and()
                .withDayOfWeek()
                .withMondayDoWValue(1)
                .withValidRange(1, 7)
                .supportsHash().supportsL()
                .and()
                .withYear().optional().and()
                .instance();

        final CronParser parser = new CronParser(cronDefinition);
        final Cron cron = parser.parse("30 3 * * MON-FRI");
        final ZonedDateTime sameDayBeforeEventStartDateTime = ZonedDateTime.parse("1970-01-01T00:00:00.000-03:00");
        final Optional<ZonedDateTime> sameDayBeforeEventStartDateTimeExecution = ExecutionTime.forCron(cron).nextExecution(sameDayBeforeEventStartDateTime);
        if (sameDayBeforeEventStartDateTimeExecution.isPresent()) {
            assertEquals(1, sameDayBeforeEventStartDateTimeExecution.get().getDayOfMonth());
        } else {
            fail("sameDayBeforeEventStartDateTimeExecution was not present");
        }

        final ZonedDateTime sameDayAfterEventStartDateTime = ZonedDateTime.parse("1970-01-01T12:00:00.000-03:00");
        final Optional<ZonedDateTime> sameDayAfterEventStartDateTimeExecution = ExecutionTime.forCron(cron).nextExecution(sameDayAfterEventStartDateTime);
        if (sameDayAfterEventStartDateTimeExecution.isPresent()) {
            assertEquals(2, sameDayAfterEventStartDateTimeExecution.get().getDayOfMonth());
        } else {
            fail("sameDayAfterEventStartDateTimeExecution was not present");
        }
    }

    /**
     * Issue #136: Bug exposed at PR #136
     * https://github.com/jmrozanec/cron-utils/pull/136
     * Reported case: when executing isMatch for a given range of dates,
     * if date is invalid, we get an exception, not a boolean as response.
     */
    @Test
    public void testMatchWorksAsExpectedForCustomCronsWhenPreviousOrNextOccurrenceIsMissing() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withDayOfMonth()
                .supportsL().supportsW()
                .and()
                .withMonth().and()
                .withYear()
                .and().instance();

        final CronParser parser = new CronParser(cronDefinition);
        final Cron cron = parser.parse("05 05 2004");
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime start = ZonedDateTime.of(2004, 5, 5, 23, 55, 0, 0, ZoneId.of("UTC"));
        final ZonedDateTime end = ZonedDateTime.of(2004, 5, 6, 1, 0, 0, 0, ZoneId.of("UTC"));
        while (start.compareTo(end) < 0) {
            assertTrue(executionTime.isMatch(start) == (start.getDayOfMonth() == 5));
            start = start.plusMinutes(1);
        }
    }

    /**
     * A CronDefinition with only 3 required fields is legal to instantiate, but the parser considers an expression
     * with 4 fields as an error:
     * java.lang.IllegalArgumentException: Cron expression contains 4 parts but we expect one of [6, 7]
     */
    @Test
    public void testThreeRequiredFieldsSupported() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().supportsL().supportsW().supportsLW().supportsQuestionMark().optional().and()
                .withMonth().optional().and()
                .withDayOfWeek().withValidRange(1, 7).withMondayDoWValue(2).supportsHash().supportsL()
                .supportsQuestionMark().optional().and()
                .withYear().withValidRange(2000, 2099).optional().and()
                .instance();
        final CronParser cronParser = new CronParser(cronDefinition);
        cronParser.parse("* * 4 3");
    }

    /**
     * A CronDefinition with only 5 required fields is legal to instantiate, but the parser considers an expression
     * with 5 fields as an error:
     * java.lang.IllegalArgumentException: Cron expression contains 4 parts but we expect one of [6, 7]
     */
    @Test
    public void testFiveRequiredFieldsSupported() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().supportsL().supportsW().supportsLW().supportsQuestionMark().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(1, 7).withMondayDoWValue(2).supportsHash().supportsL()
                .supportsQuestionMark().optional().and()
                .withYear().withValidRange(2000, 2099).optional().and()
                .instance();
        final CronParser cronParser = new CronParser(cronDefinition);
        cronParser.parse("* * 4 3 *");
    }
}
