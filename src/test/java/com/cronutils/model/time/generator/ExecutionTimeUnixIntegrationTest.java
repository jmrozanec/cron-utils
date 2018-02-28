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

package com.cronutils.model.time.generator;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class ExecutionTimeUnixIntegrationTest {

    private static final String LAST_EXECUTION_NOT_PRESENT_ERROR = "last execution was not present";
    private static final String NEXT_EXECUTION_NOT_PRESENT_ERROR = "next execution was not present";
    private static final ZoneId ZONE_ID_NEW_YORK = ZoneId.of("America/New_York");

    @Test
    public void testIsMatchForUnix01() {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        final String crontab = "* * * * *";//m,h,dom,M,dow
        final Cron cron = parser.parse(crontab);
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        final ZonedDateTime scanTime = ZonedDateTime.parse("2016-02-29T11:00:00.000-06:00");
        assertTrue(executionTime.isMatch(scanTime));
    }

    @Test
    public void testIsMatchForUnix02() {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        final String crontab = "0 * * * 1-5";//m,h,dom,M,dow
        final Cron cron = parser.parse(crontab);
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        final ZonedDateTime scanTime = ZonedDateTime.parse("2016-03-04T11:00:00.000-06:00");
        assertTrue(executionTime.isMatch(scanTime));
    }

    /**
     * Issue #37: for pattern "every 10 minutes", nextExecution returns a date from past.
     */
    @Test
    public void testEveryTenMinutesNextExecution() {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("*/10 * * * *"));
        final ZonedDateTime time = ZonedDateTime.parse("2015-09-05T13:43:00.000-07:00");
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(time);
        if (nextExecution.isPresent()) {
            assertEquals(ZonedDateTime.parse("2015-09-05T13:50:00.000-07:00"), nextExecution.get());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #38: every 2 min schedule doesn't roll over to next hour.
     */
    @Test
    public void testEveryTwoMinRollsOverHour() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        final Cron cron = new CronParser(cronDefinition).parse("*/2 * * * *");
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        final ZonedDateTime time = ZonedDateTime.parse("2015-09-05T13:56:00.000-07:00");
        final Optional<ZonedDateTime> nextExecutionTime = executionTime.nextExecution(time);
        if (nextExecutionTime.isPresent()) {
            final ZonedDateTime next = nextExecutionTime.get();
            final Optional<ZonedDateTime> shouldBeInNextHourExecution = executionTime.nextExecution(next);
            if (shouldBeInNextHourExecution.isPresent()) {
                assertEquals(next.plusMinutes(2), shouldBeInNextHourExecution.get());
                return;
            }
        }
        fail("one of the asserted values was not present.");
    }

    /**
     * Issue #41: for everything other than a dayOfWeek value == 1, nextExecution and lastExecution do not return correct results.
     */
    @Test
    public void testEveryTuesdayAtThirdHourOfDayNextExecution() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        final CronParser parser = new CronParser(cronDefinition);
        final Cron myCron = parser.parse("0 3 * * 3");
        final ZonedDateTime time = ZonedDateTime.parse("2015-09-17T00:00:00.000-07:00");
        final Optional<ZonedDateTime> nextExecution = ExecutionTime.forCron(myCron).nextExecution(time);
        if (nextExecution.isPresent()) {
            assertEquals(ZonedDateTime.parse("2015-09-23T03:00:00.000-07:00"), nextExecution.get());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #41: for everything other than a dayOfWeek value == 1, nextExecution and lastExecution do not return correct results.
     */
    @Test
    public void testEveryTuesdayAtThirdHourOfDayLastExecution() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        final CronParser parser = new CronParser(cronDefinition);
        final Cron myCron = parser.parse("0 3 * * 3");
        final ZonedDateTime time = ZonedDateTime.parse("2015-09-17T00:00:00.000-07:00");
        final Optional<ZonedDateTime> lastExecution = ExecutionTime.forCron(myCron).lastExecution(time);
        if (lastExecution.isPresent()) {
            assertEquals(ZonedDateTime.parse("2015-09-16T03:00:00.000-07:00"), lastExecution.get());
        } else {
            fail(LAST_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #45: last execution does not match expected date. Result is not in same timezone as reference date.
     */
    @Test
    public void testMondayWeekdayLastExecution() {
        final Cron cron = getUnixCron("* * * * 1");
        final Optional<ZonedDateTime> lastExecution = getLastExecutionFor(cron, ZonedDateTime.parse("2015-10-13T17:26:54.468-07:00"));
        if (lastExecution.isPresent()) {
            assertEquals(ZonedDateTime.parse("2015-10-12T23:59:00.000-07:00"), lastExecution.get());
        } else {
            fail(LAST_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    private Cron getUnixCron(final String cronExpression) {
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        final CronParser parser = new CronParser(cronDefinition);
        return parser.parse(cronExpression);
    }

    private Optional<ZonedDateTime> getLastExecutionFor(final Cron cron, final ZonedDateTime dateTime) {
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        return  executionTime.lastExecution(dateTime);
    }

    private Optional<ZonedDateTime> getNextExecutionFor(final Cron cron, final ZonedDateTime dateTime) {
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        return  executionTime.nextExecution(dateTime);
    }

    /**
     * Issue #45: next execution does not match expected date. Result is not in same timezone as reference date.
     */
    @Test
    public void testMondayWeekdayNextExecution() {
        final String crontab = "* * * * 1";
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        final CronParser parser = new CronParser(cronDefinition);
        final Cron cron = parser.parse(crontab);
        final ZonedDateTime date = ZonedDateTime.parse("2015-10-13T17:26:54.468-07:00");
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(date);
        if (nextExecution.isPresent()) {
            assertEquals(ZonedDateTime.parse("2015-10-19T00:00:00.000-07:00"), nextExecution.get());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #50: last execution does not match expected date when cron specifies day of week and last execution is in previous month.
     */
    @Test
    public void testLastExecutionDaysOfWeekOverMonthBoundary() {
        final Cron cron = getUnixCron("0 11 * * 1");
        final Optional<ZonedDateTime> lastExecution = getLastExecutionFor(cron, ZonedDateTime.parse("2015-11-02T00:10:00Z"));
        if (lastExecution.isPresent()) {
            assertEquals(ZonedDateTime.parse("2015-10-26T11:00:00Z"), lastExecution.get());
        } else {
            fail(LAST_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #52: "And" doesn't work for day of the week
     * 1,2 should be Monday and Tuesday, but instead it is treated as 1st/2nd of month.
     */
    @Test
    public void testWeekdayAndLastExecution() {
        final Cron cron = getUnixCron("* * * * 1,2");
        final Optional<ZonedDateTime> lastExecution = getLastExecutionFor(cron, ZonedDateTime.parse("2015-11-10T17:01:00Z"));
        if (lastExecution.isPresent()) {
            assertEquals(ZonedDateTime.parse("2015-11-10T17:00:00Z"), lastExecution.get());
        } else {
            fail(LAST_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Isue #52: Additional test to ensure after fix that "And" and "Between" can both be used
     * 1,2-3 should be Monday, Tuesday and Wednesday.
     */
    @Test
    public void testWeekdayAndWithMixOfOnAndBetweenLastExecution() {
        final Cron cron = getUnixCron("* * * * 1,2-3");
        final Optional<ZonedDateTime> lastExecution = getLastExecutionFor(cron, ZonedDateTime.parse("2015-11-10T17:01:00Z"));
        if (lastExecution.isPresent()) {
            assertEquals(ZonedDateTime.parse("2015-11-10T17:00:00Z"), lastExecution.get());
        } else {
            fail(LAST_EXECUTION_NOT_PRESENT_ERROR);
        }

    }

    /**
     * Issue #59: Incorrect next execution time for "month" and "day of week".
     * Considers Month in range 0-11 instead of 1-12
     */
    @Test
    public void testCorrectMonthScaleForNextExecution1() {
        final Cron cron = getUnixCron("* * */3 */4 */5");
        final Optional<ZonedDateTime> nextExecution = getNextExecutionFor(cron, ZonedDateTime.parse("2015-12-10T16:32:56.586-08:00"));
        if (nextExecution.isPresent()) {
            //DoW: 0-6 -> 0, 5 (sunday, friday)
            //DoM: 1-31 -> 1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31
            //M: 1-12 -> 1, 5, 9
            assertEquals(ZonedDateTime.parse("2016-01-01T00:00:00.000-08:00"), nextExecution.get());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #59: Incorrect next execution time for "day of month" in "time" situation
     * dom "* / 4" should mean 1, 5, 9, 13, 17th... of month instead of 4, 8, 12, 16th...
     */
    @Test
    public void testCorrectMonthScaleForNextExecution2() {
        final Cron cron = getUnixCron("* * */4 * *");
        final Optional<ZonedDateTime> nextExecution = getNextExecutionFor(cron, ZonedDateTime.parse("2015-12-10T16:32:56.586-08:00"));
        if (nextExecution.isPresent()) {
            assertEquals(ZonedDateTime.parse("2015-12-13T00:00:00.000-08:00"), nextExecution.get());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #59: Incorrect next execution time for "month" and "day of week".
     * Considers bad DoW
     */
    @Test
    public void testCorrectNextExecutionDoW() {
        //DoW: 0-6 -> 0, 4 (sunday, thursday)
        final Cron cron = getUnixCron("* * * * */4");
        final Optional<ZonedDateTime> nextExecution = getNextExecutionFor(cron, ZonedDateTime.parse("2016-01-28T16:32:56.586-08:00"));
        if (nextExecution.isPresent()) {
            assertEquals(ZonedDateTime.parse("2016-02-04T00:00:00.000-08:00"), nextExecution.get());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #69: Getting next execution fails on leap-year when using day-of-week.
     */
    @Test
    public void testCorrectNextExecutionDoWForLeapYear() {
        //DoW: 0-6 -> 1, 2, 3, 4, 5 -> in this year:
        final Optional<ZonedDateTime> nextExecution = getNextExecutionFor(getUnixCron("0 * * * 1-5"), ZonedDateTime.parse("2016-02-29T11:00:00.000-06:00"));
        if (nextExecution.isPresent()) {
            assertEquals(ZonedDateTime.parse("2016-02-29T12:00:00.000-06:00"), nextExecution.get());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }

    }

    /**
     * Issue #61: nextExecution over daylight savings is wrong.
     */
    @Test
    public void testNextExecutionDaylightSaving() {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 17 * * *"));// daily at 17:00
        // Daylight savings for New York 2016 is Mar 13 at 2am
        final ZonedDateTime last = ZonedDateTime.of(2016, 3, 12, 17, 0, 0, 0, ZONE_ID_NEW_YORK);
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(last);
        if (nextExecution.isPresent()) {
            final long millis = Duration.between(last, nextExecution.get()).toMillis();
            assertEquals(23, (millis / 3600000));
            assertEquals(last.getZone(), nextExecution.get().getZone());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #61: lastExecution over daylight savings is wrong.
     */
    @Test
    public void testLastExecutionDaylightSaving() {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 17 * * *"));// daily at 17:00
        // Daylight savings for New York 2016 is Mar 13 at 2am
        final ZonedDateTime now = ZonedDateTime.of(2016, 3, 12, 17, 0, 0, 0, ZoneId.of("America/Phoenix"));
        final Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(now);
        if (lastExecution.isPresent()) {
            final long millis = Duration.between(lastExecution.get(), now).toMillis();
            assertEquals(24, (millis / 3600000));
            assertEquals(now.getZone(), lastExecution.get().getZone());
        } else {
            fail(LAST_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #79: Next execution skipping valid date.
     */
    @Test
    public void testNextExecution2014() {
        final String crontab = "0 8 * * 1";//m,h,dom,m,dow ; every monday at 8AM
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        final CronParser parser = new CronParser(cronDefinition);
        final Cron cron = parser.parse(crontab);
        final ZonedDateTime date = ZonedDateTime.parse("2014-11-30T00:00:00Z");
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(date);
        if (nextExecution.isPresent()) {
            assertEquals(ZonedDateTime.parse("2014-12-01T08:00:00Z"), nextExecution.get());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }

    }

    /**
     * Issue #92: Next execution skipping valid date.
     */
    @Test
    public void testNextExecution2016() {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("1 0 * * tue"));
        final ZonedDateTime date = ZonedDateTime.parse("2016-05-24T01:02:50Z");
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(date);
        if (nextExecution.isPresent()) {
            assertEquals(ZonedDateTime.parse("2016-05-31T00:01:00Z"), nextExecution.get());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #112: Calling nextExecution exactly on the first instant of the fallback hour (after the DST ends) makes it go back to DST.
     * https://github.com/jmrozanec/cron-utils/issues/112
     */
    @Test
    public void testWrongNextExecutionOnDSTEnd() {
        final ZoneId zone = ZoneId.of("America/Sao_Paulo");

        //2016-02-20T23:00-03:00[America/Sao_Paulo], first minute of fallback hour
        final ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1456020000000L), zone);
        final ZonedDateTime expected = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1456020000000L + 60000), zone);

        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("* * * * *"));
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(date);
        if (nextExecution.isPresent()) {
            assertEquals(expected, nextExecution.get());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #112: Calling nextExecution on a date-time during the overlap hour of DST causes an incorrect offset
     * to be returned.
     */
    @Test
    public void testDSTOverlap() {
        final ZoneId zoneId = ZONE_ID_NEW_YORK;

        // For the America/New_York time zone, DST ends (UTC-4:00 to UTC-5:00 / EDT -> EST) at 2:00 AM
        // on the these days for the years 2015-2026:
        final Set<LocalDate> dstDates = new HashSet<>();
        dstDates.add(LocalDate.of(2015, Month.NOVEMBER, 1));
        dstDates.add(LocalDate.of(2016, Month.NOVEMBER, 6));
        dstDates.add(LocalDate.of(2017, Month.NOVEMBER, 5));
        dstDates.add(LocalDate.of(2018, Month.NOVEMBER, 4));
        dstDates.add(LocalDate.of(2019, Month.NOVEMBER, 3));
        dstDates.add(LocalDate.of(2020, Month.NOVEMBER, 1));
        dstDates.add(LocalDate.of(2021, Month.NOVEMBER, 7));
        dstDates.add(LocalDate.of(2022, Month.NOVEMBER, 6));
        dstDates.add(LocalDate.of(2023, Month.NOVEMBER, 5));
        dstDates.add(LocalDate.of(2024, Month.NOVEMBER, 3));
        dstDates.add(LocalDate.of(2025, Month.NOVEMBER, 2));
        dstDates.add(LocalDate.of(2026, Month.NOVEMBER, 1));

        // Starting at 12 AM Nov. 1, 2015
        ZonedDateTime date = ZonedDateTime.of(2015, 11, 1, 0, 0, 0, 0, zoneId);

        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        // Scheduling pattern for 1:30 AM for the first 7 days of every November
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("30 1 1-7 11 *"));

        final ZoneOffset easternDaylightTimeOffset = ZoneOffset.ofHours(-4);
        final ZoneOffset easternStandardTimeOffset = ZoneOffset.ofHours(-5);

        for (int year = 2015; year <= 2026; year++) {
            boolean pastDSTEnd = false;
            int dayOfMonth = 1;
            while (dayOfMonth < 8) {
                final LocalDateTime expectedLocalDateTime = LocalDateTime.of(year, Month.NOVEMBER, dayOfMonth, 1, 30);
                final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(date);
                assert (nextExecution.isPresent());
                date = nextExecution.get();

                final ZoneOffset expectedOffset = pastDSTEnd ? easternStandardTimeOffset : easternDaylightTimeOffset;

                if (dstDates.contains(LocalDate.of(year, Month.NOVEMBER, dayOfMonth))) {
                    if (!pastDSTEnd) {
                        // next iteration should be past the DST transition
                        pastDSTEnd = true;
                    } else {
                        dayOfMonth++;
                    }
                } else {
                    dayOfMonth++;
                }
                assertEquals(ZonedDateTime.ofInstant(expectedLocalDateTime, expectedOffset, zoneId), date);
            }
        }
    }

    /**
     * Test that a cron expression that only runs at a certain time that falls inside the DST start gap
     * does not run on the DST start day. Ex. 2:15 AM is an invalid local time for the America/New_York
     * time zone on the DST start days.
     */
    @Test
    public void testDSTGap() {
        final ZoneId zoneId = ZONE_ID_NEW_YORK;
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        // Run at 2:15 AM each day for March 7 to 14
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("15 2 7-14 3 *"));

        // Starting at 12 AM March. 7, 2015
        ZonedDateTime date = ZonedDateTime.of(2015, 3, 7, 0, 0, 0, 0, zoneId);

        // For America/New_York timezone, DST starts at 2 AM local time and moves forward 1 hour
        // DST dates for 2015-2026
        final Map<Integer, LocalDate> dstDates = new HashMap<>();
        dstDates.put(2015, LocalDate.of(2015, Month.MARCH, 8));
        dstDates.put(2016, LocalDate.of(2016, Month.MARCH, 13));
        dstDates.put(2017, LocalDate.of(2017, Month.MARCH, 12));
        dstDates.put(2018, LocalDate.of(2018, Month.MARCH, 11));
        dstDates.put(2019, LocalDate.of(2019, Month.MARCH, 10));
        dstDates.put(2020, LocalDate.of(2020, Month.MARCH, 8));
        dstDates.put(2021, LocalDate.of(2021, Month.MARCH, 14));
        dstDates.put(2022, LocalDate.of(2022, Month.MARCH, 13));
        dstDates.put(2023, LocalDate.of(2023, Month.MARCH, 12));
        dstDates.put(2024, LocalDate.of(2024, Month.MARCH, 10));
        dstDates.put(2025, LocalDate.of(2025, Month.MARCH, 9));
        dstDates.put(2026, LocalDate.of(2026, Month.MARCH, 8));

        final ZoneOffset easternDaylightTimeOffset = ZoneOffset.ofHours(-4);
        final ZoneOffset easternStandardTimeOffset = ZoneOffset.ofHours(-5);
        for (int year = 2015; year <= 2026; year++) {
            final LocalDate dstDateForYear = dstDates.get(year);
            boolean isPastDSTStart = false;
            int dayOfMonth = 7;
            while (dayOfMonth < 15) {
                final LocalDateTime localDateTime = LocalDateTime.of(year, Month.MARCH, dayOfMonth, 2, 15);
                // skip the DST start days... 2:15 AM does not exist in the local time
                if (localDateTime.toLocalDate().isEqual(dstDateForYear)) {
                    dayOfMonth++;
                    isPastDSTStart = true;
                    continue;
                }
                final ZoneOffset expectedOffset = isPastDSTStart ? easternDaylightTimeOffset : easternStandardTimeOffset;
                final ZonedDateTime expectedDateTime = ZonedDateTime.ofLocal(localDateTime, zoneId, expectedOffset);

                final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(date);
                assert (nextExecution.isPresent());
                date = nextExecution.get();
                assertEquals(expectedDateTime, date);
                dayOfMonth++;
            }
        }
    }

    /**
     * Issue #125: Prints stack trace for NoSuchValueException for expressions with comma-separated values
     * https://github.com/jmrozanec/cron-utils/issues/125
     */
    @Test
    public void testNextExecutionProducesStackTraces() {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("45 1,13 * * *"));
        executionTime.nextExecution(ZonedDateTime.parse("2016-05-24T01:02:50Z"));
    }

    /**
     * Issue #130: Wrong last execution time if schedule hit is less than one second ago
     * https://github.com/jmrozanec/cron-utils/issues/130
     */
    @Test
    public void exactHitReturnsFullIntervalDuration() {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        final Cron cron = parser.parse("0 12 * * *");
        final ZonedDateTime time = ZonedDateTime.of(2016, 12, 2, 12, 0, 0, 0, ZoneId.of("Europe/Vienna"));
        final Optional<Duration> timeFromLastExecution = ExecutionTime.forCron(cron).timeFromLastExecution(time);
        if (timeFromLastExecution.isPresent()) {
            assertEquals(timeFromLastExecution.get(), Duration.ofHours(24));
        } else {
            fail(LAST_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #130: Wrong last execution time if schedule hit is less than one second ago
     * https://github.com/jmrozanec/cron-utils/issues/130
     */
    @Test
    public void fuzzyHitReturnsVerySmallIntervalDuration() {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        final Cron cron = parser.parse("0 12 * * *");
        ZonedDateTime time = ZonedDateTime.of(2016, 12, 2, 12, 0, 0, 0, ZoneId.of("Europe/Vienna"));
        final Duration diff = Duration.ofMillis(300);
        time = time.plus(diff);
        final Optional<Duration> timeFromLastExecution = ExecutionTime.forCron(cron).timeFromLastExecution(time);
        if (timeFromLastExecution.isPresent()) {
            assertEquals(timeFromLastExecution.get(), Duration.ofDays(1).plus(diff));
        } else {
            fail(LAST_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    @Test
    public void invalidDayInMonthCron() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        final CronParser parser = new CronParser(cronDefinition);
        final Cron myCron = parser.parse("0 0 31 2 *");
        final ZonedDateTime time = ZonedDateTime.parse("2015-09-17T00:00:00.000-07:00");
        final Optional<ZonedDateTime> nextExecution = ExecutionTime.forCron(myCron).nextExecution(time);
        assertFalse(nextExecution.isPresent());
    }
}
