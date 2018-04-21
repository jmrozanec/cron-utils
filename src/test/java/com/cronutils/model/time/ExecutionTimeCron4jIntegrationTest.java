/*
 * Copyright 2015 jmrozanec Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 */

package com.cronutils.model.time;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExecutionTimeCron4jIntegrationTest {
    private CronParser cron4jCronParser;
    private static final String EVERY_MONDAY_AT_18 = "0 18 * * 1";
    private static final String EVERY_15_MINUTES = "0/15 * * * *";
    private static final String EVERY_2_HOURS = "0 0/2 * * *";
    private static final String EVERY_WEEKDAY_AT_6 = "0 6 * * MON-FRI";
    private static final Logger log = LoggerFactory.getLogger(ExecutionTimeCron4jIntegrationTest.class);
    private static final String NEXT_EXECUTION_NOT_PRESENT_ERROR = "next execution not present";
    private static final String LOG_LAST_RUN = "LastRun = [{}]";
    private static final String LOG_NEXT_RUN = "NextRun = [{}]";

    @Before
    public void setUp() {
        cron4jCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J));
    }

    @Test
    public void testForCron() {
        assertEquals(SingleExecutionTime.class, ExecutionTime.forCron(cron4jCronParser.parse(EVERY_MONDAY_AT_18)).getClass());
    }

    /**
     * Issue #37: nextExecution not calculating correct time.
     */
    @Test
    public void testEveryWeekdayAt6() {
        ZonedDateTime lastRun = ZonedDateTime.now();
        final ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse(EVERY_WEEKDAY_AT_6));

        // iterate through the next 8 days so we roll over for a week
        // and make sure the next run time is always in the future from the prior run time
        for (int i = 0; i < 8; i++) {

            final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(lastRun);
            if (nextExecution.isPresent()) {
                final ZonedDateTime nextRun = nextExecution.get();
                log.info(LOG_LAST_RUN, lastRun);
                log.info(LOG_NEXT_RUN, nextRun);

                assertNotEquals(6, nextRun.getDayOfWeek());
                assertNotEquals(7, nextRun.getDayOfWeek());
                assertTrue(lastRun.isBefore(nextRun));
                lastRun = lastRun.plusDays(1);
            } else {
                fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
            }
        }
    }

    /**
     * Issue #37: nextExecution not calculating correct time.
     */
    @Test
    public void testEvery2Hours() {
        ZonedDateTime lastRun = ZonedDateTime.now();
        final ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse(EVERY_2_HOURS));

        // iterate through the next 36 hours so we roll over the to the next day
        // and make sure the next run time is always in the future from the prior run time
        for (int i = 0; i < 36; i++) {

            final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(lastRun);
            if (nextExecution.isPresent()) {
                final ZonedDateTime nextRun = nextExecution.get();
                log.info(LOG_LAST_RUN, lastRun);
                log.info(LOG_NEXT_RUN, nextRun);

                assertTrue(String.format("Hour is %s", nextRun.getHour()), nextRun.getHour() % 2 == 0);
                assertTrue(String.format("Last run is before next one: %s", lastRun.isBefore(nextRun)), lastRun.isBefore(nextRun));
                lastRun = lastRun.plusHours(1);
            } else {
                fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
            }
        }
    }

    @Test
    public void testQuick() {
        ZonedDateTime lastRun = ZonedDateTime.of(2017, 3, 12, 0, 55, 50, 630, ZoneId.of("America/Los_Angeles"));
        final ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse(EVERY_2_HOURS));

        // iterate through the next 36 hours so we roll over the to the next day
        // and make sure the next run time is always in the future from the prior run time
        for (int i = 0; i < 1; i++) {

            final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(lastRun);
            if (nextExecution.isPresent()) {
                final ZonedDateTime nextRun = nextExecution.get();
                log.info(LOG_LAST_RUN, lastRun);
                log.info(LOG_NEXT_RUN, nextRun);

                assertTrue(String.format("Hour is %s", nextRun.getHour()), nextRun.getHour() % 2 == 0);
                assertTrue(String.format("Last run is before next one: %s", lastRun.isBefore(nextRun)), lastRun.isBefore(nextRun));
                lastRun = lastRun.plusHours(1);
            } else {
                fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
            }
        }
    }

    /**
     * Issue #37: nextExecution not calculating correct time.
     */
    @Test
    public void testEvery15Minutes() {
        ZonedDateTime lastRun = ZonedDateTime.now();
        final ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse(EVERY_15_MINUTES));

        // iterate through the next 75 minutes so we roll over the top of the hour
        // and make sure the next run time is always in the future from the prior run time
        for (int i = 0; i < 75; i++) {

            final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(lastRun);
            if (nextExecution.isPresent()) {
                final ZonedDateTime nextRun = nextExecution.get();
                log.debug(LOG_LAST_RUN, lastRun);
                log.debug(LOG_NEXT_RUN, nextRun);

                assertTrue(nextRun.getMinute() % 15 == 0);
                assertTrue(lastRun.isBefore(nextRun));
                lastRun = lastRun.plusMinutes(1);
            } else {
                fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
            }
        }
    }

    /**
     * Issue #26: bug 1: if day of week specified, always from day of month is not considered.
     */
    @Test
    public void testDayOfWeekOverridesAlwaysAtDayOfMonth() {
        final ZonedDateTime now = ZonedDateTime.now();
        final ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse(EVERY_MONDAY_AT_18));
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        if (nextExecution.isPresent()) {
            final ZonedDateTime next = nextExecution.get();
            assertEquals(1, next.getDayOfWeek().getValue());
            assertTrue(now.isBefore(next));
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #26: bug 1: if day of week specified, always from day of month is not considered.
     */
    @Test
    public void testDayOfMonthOverridesAlwaysAtDayOfWeek() {
        final ZonedDateTime now = ZonedDateTime.now();
        final ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse("0 18 1 * *"));
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        if (nextExecution.isPresent()) {
            final ZonedDateTime next = nextExecution.get();
            assertEquals(1, next.getDayOfMonth());
            assertTrue(now.isBefore(next));
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #26: bug 2: nextNext should be greater than next, not the same value.
     */
    @Test
    public void testNextExecutionOverNextExecution() {
        final ZonedDateTime now = ZonedDateTime.now();
        final ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse(EVERY_MONDAY_AT_18));
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        if (nextExecution.isPresent()) {
            final Optional<ZonedDateTime> nextNextExecution = executionTime.nextExecution(nextExecution.get());
            if (nextNextExecution.isPresent()) {
                assertTrue(now.isBefore(nextExecution.get()));
                assertTrue(nextExecution.get().isBefore(nextNextExecution.get()));
                return;
            }
        }
        fail("one of the asserted values was not present");
    }

    /**
     * Issue #203: cron4j definition should generate next execution times matching both the day of month and day of week when both are restricted.
     */
    @Test
    public void testFixedDayOfMonthAndDayOfWeek() {
        // Run only on January 1st if it is a Tuesday, at 9:00AM
        final ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse("0 9 1 1 tue"));
        // The next four Tuesday January 1 after January 1, 2017 are in 2019, 2030, 2036, and 2041
        final int[] expectedYears = { 2019, 2030, 2036, 2041 };
        ZonedDateTime next = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        for (final int expectedYear : expectedYears) {
            final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(next);
            assert nextExecution.isPresent();
            next = nextExecution.get();
            final ZonedDateTime expectedDate = ZonedDateTime.of(expectedYear, 1, 1, 9, 0, 0, 0, ZoneId.systemDefault());
            final String expectedMessage = String.format("Expected next execution time: %s, Actual next execution time: %s", expectedDate, next);
            assertEquals(expectedMessage, DayOfWeek.TUESDAY, next.getDayOfWeek());
            assertEquals(expectedMessage, 1, next.getDayOfMonth());
            assertEquals(expectedMessage, expectedYear, next.getYear());
            assertEquals(expectedMessage, 9, next.getHour());
            assertEquals(expectedMessage, expectedDate, next);
        }
    }

    /**
     * Issue #203: cron4j definition should generate next execution times matching both the day of month and day of week when both are restricted.
     */
    @Test
    public void testRandomDayOfMonthAndDayOfWeek() {
        // pick a random day of week and day of month
        // DayOfWeek uses 1 (Mon) to 7 (Sun) while cron4j allows 0 (Sun) to 6 (Sat)
        final Random random = new Random();
        final DayOfWeek dayOfWeek = DayOfWeek.of(random.nextInt(7) + 1);
        int dayOfWeekValue = dayOfWeek.getValue();
        if (dayOfWeekValue == 7) {
            dayOfWeekValue = 0;
        }
        final int month = random.nextInt(12) + 1;
        // using max length so it is possible to use February 29 (leap-year)
        final int dayOfMonth = random.nextInt(Month.of(month).maxLength()) + 1;
        final String expression = String.format("0 0 %d %d %d", dayOfMonth, month, dayOfWeekValue);
        final ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse(expression));
        log.debug("cron4j expression: {}", expression);
        ZonedDateTime next = ZonedDateTime.now();
        log.debug("Start date: {}", next);
        for (int i = 1; i <= 20; i++) {
            final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(next);
            assert (nextExecution.isPresent());
            next = nextExecution.get();
            log.debug("Execution #{} date: {}", i, next);
            assertEquals("Incorrect day of the week", dayOfWeek, next.getDayOfWeek());
            assertEquals("Incorrect day of the month", dayOfMonth, next.getDayOfMonth());
            assertEquals("Incorrect month", month, next.getMonthValue());
        }
    }
}
