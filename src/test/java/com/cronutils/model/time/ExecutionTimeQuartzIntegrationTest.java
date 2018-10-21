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

package com.cronutils.model.time;

import java.text.ParseException;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cronutils.model.CronType.QUARTZ;
import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExecutionTimeQuartzIntegrationTest {
    private CronParser parser;
    private static final String EVERY_SECOND = "* * * * * ? *";

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionTimeQuartzIntegrationTest.class);
    private static final String NEXT_EXECUTION_NOT_PRESENT_ERROR = "next execution was not present";
    private static final String LAST_EXECUTION_NOT_PRESENT_ERROR = "last execution was not present";
    private static final String DURATION_NOT_PRESENT_ERROR = "duration was not present";
    private static final String ASSERTED_EXECUTION_NOT_PRESENT = "one of the asserted executions was not present";

    @Before
    public void setUp() {
        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
    }

    @Test
    public void testForCron() {
        assertEquals(SingleExecutionTime.class, ExecutionTime.forCron(parser.parse(EVERY_SECOND)).getClass());
    }

    @Test
    public void testNextExecutionEverySecond() {
        final ZonedDateTime now = truncateToSeconds(ZonedDateTime.now());
        final ZonedDateTime expected = truncateToSeconds(now.plusSeconds(1));
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(EVERY_SECOND));
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        if (nextExecution.isPresent()) {
            assertEquals(expected, nextExecution.get());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    @Test
    public void testTimeToNextExecution() {
        final ZonedDateTime now = truncateToSeconds(ZonedDateTime.now());
        final ZonedDateTime expected = truncateToSeconds(now.plusSeconds(1));
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(EVERY_SECOND));
        final Optional<Duration> duration = executionTime.timeToNextExecution(now);
        if (duration.isPresent()) {
            assertEquals(Duration.between(now, expected), duration.get());
        } else {
            fail(DURATION_NOT_PRESENT_ERROR);
        }
    }

    @Test
    public void testLastExecution() {
        final ZonedDateTime now = truncateToSeconds(ZonedDateTime.now());
        final ZonedDateTime expected = truncateToSeconds(now.minusSeconds(1));
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(EVERY_SECOND));
        final Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(now);
        if (lastExecution.isPresent()) {
            assertEquals(expected, lastExecution.get());
        } else {
            fail(LAST_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    @Test
    public void testTimeFromLastExecution() {
        final ZonedDateTime now = truncateToSeconds(ZonedDateTime.now());
        final ZonedDateTime expected = truncateToSeconds(now.minusSeconds(1));
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(EVERY_SECOND));
        final Optional<Duration> duration = executionTime.timeToNextExecution(now);
        if (duration.isPresent()) {
            assertEquals(Duration.between(expected, now), duration.get());
        } else {
            fail(DURATION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Test for issue #9
     * https://github.com/jmrozanec/cron-utils/issues/9
     * Reported case: If you write a cron expression that contains a month or day of week, nextExection() ignores it.
     * Expected: should not ignore month or day of week field
     */
    @Test
    public void testDoesNotIgnoreMonthOrDayOfWeek() {
        //seconds, minutes, hours, dayOfMonth, month, dayOfWeek
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 11 11 11 11 ?"));
        final ZonedDateTime now = ZonedDateTime.of(2015, 4, 15, 0, 0, 0, 0, UTC);
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        if (nextExecution.isPresent()) {
            final ZonedDateTime whenToExecuteNext = nextExecution.get();
            assertEquals(2015, whenToExecuteNext.getYear());
            assertEquals(11, whenToExecuteNext.getMonthValue());
            assertEquals(11, whenToExecuteNext.getDayOfMonth());
            assertEquals(11, whenToExecuteNext.getHour());
            assertEquals(11, whenToExecuteNext.getMinute());
            assertEquals(0, whenToExecuteNext.getSecond());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Test for issue #18.
     */
    @Test
    public void testHourlyIntervalTimeFromLastExecution() {
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime previousHour = now.minusHours(1);
        final String quartzCronExpression = String.format("0 0 %s * * ?", previousHour.getHour());
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(quartzCronExpression));
        final Optional<Duration> duration = executionTime.timeFromLastExecution(now);
        if (duration.isPresent()) {
            assertTrue(duration.get().toMinutes() <= 120);
        } else {
            fail(DURATION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Test for issue #19
     * https://github.com/jmrozanec/cron-utils/issues/19
     * Reported case: When nextExecution shifts to the 24th hour (e.g. 23:59:59 + 00:00:01), JodaTime will throw an exception
     * Expected: should shift one day
     */
    @Test
    public void testShiftTo24thHour() {
        final String expression = "0/1 * * 1/1 * ? *";  // every second every day
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(expression));

        final ZonedDateTime now = ZonedDateTime.of(LocalDate.of(2016, 8, 5), LocalTime.of(23, 59, 59, 0), UTC);
        final ZonedDateTime expected = now.plusSeconds(1);
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        if (nextExecution.isPresent()) {
            assertEquals(expected, nextExecution.get());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Test for issue #19
     * https://github.com/jmrozanec/cron-utils/issues/19
     * Reported case: When nextExecution shifts to 32nd day (e.g. 2015-01-31 23:59:59 + 00:00:01), JodaTime will throw an exception
     * Expected: should shift one month
     */
    @Test
    public void testShiftTo32ndDay() {
        final String expression = "0/1 * * 1/1 * ? *";  // every second every day
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(expression));

        final ZonedDateTime now = ZonedDateTime.of(2015, 1, 31, 23, 59, 59, 0, UTC);
        final ZonedDateTime expected = now.plusSeconds(1);
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        if (nextExecution.isPresent()) {
            assertEquals(expected, nextExecution.get());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #24: next execution not properly calculated.
     */
    @Test
    public void testTimeShiftingProperlyDone() {
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0/10 22 ? * *"));
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(ZonedDateTime.now().withHour(15).withMinute(27));
        if (nextExecution.isPresent()) {
            final ZonedDateTime nextExecutionTime = nextExecution.get();
            assertEquals(22, nextExecutionTime.getHour());
            assertEquals(0, nextExecutionTime.getMinute());
        }
    }

    /**
     * Issue #27: execution time properly calculated.
     */
    @Test
    public void testMonthRangeExecutionTime() {
        assertNotNull(ExecutionTime.forCron(parser.parse("0 0 0 * JUL-AUG ? *")));
    }

    /**
     * Issue #30: execution time properly calculated.
     */
    @Test
    public void testSaturdayExecutionTime() {
        final ZonedDateTime now = ZonedDateTime.now();
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0 3 ? * 6"));
        final Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(now);
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        if (lastExecution.isPresent() && nextExecution.isPresent()) {
            final ZonedDateTime last = lastExecution.get();
            final ZonedDateTime next = nextExecution.get();
            assertNotEquals(last, next);
        } else {
            fail(ASSERTED_EXECUTION_NOT_PRESENT);
        }
    }

    /**
     * Issue: execution time properly calculated.
     */
    @Test
    public void testWeekdayExecutionTime() {
        final ZonedDateTime now = ZonedDateTime.now();
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0 3 ? * *"));
        final Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(now);
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        if (lastExecution.isPresent() && nextExecution.isPresent()) {
            final ZonedDateTime last = lastExecution.get();
            final ZonedDateTime next = nextExecution.get();
            assertNotEquals(last, next);
        } else {
            fail(ASSERTED_EXECUTION_NOT_PRESENT);
        }
    }

    /**
     * Issue #64: Incorrect next execution time for ranges.
     */
    @Test
    public void testExecutionTimeForRanges() {
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("* 10-20 * * * ? 2099"));
        final ZonedDateTime scanTime = ZonedDateTime.parse("2016-02-29T11:00:00.000-06:00");
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(scanTime);
        if (nextExecution.isPresent()) {
            final ZonedDateTime nextTime = nextExecution.get();
            assertNotNull(nextTime);
            assertEquals(10, nextTime.getMinute());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #65: Incorrect last execution time for fixed month.
     */
    @Test
    public void testLastExecutionTimeForFixedMonth() {
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 30 12 1 9 ? 2010"));
        final ZonedDateTime scanTime = ZonedDateTime.parse("2016-01-08T11:00:00.000-06:00");
        final Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(scanTime);
        if (lastExecution.isPresent()) {
            final ZonedDateTime lastTime = lastExecution.get();
            assertNotNull(lastTime);
            assertEquals(9, lastTime.getMonthValue());
        } else {
            fail(LAST_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #66: Incorrect Day Of Week processing for Quartz when Month or Year isn't '*'.
     */
    @Test
    public void testNextExecutionRightDoWForFixedMonth() {
        //cron format: s,m,H,DoM,M,DoW,Y
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 * * ? 5 1 *"));
        final ZonedDateTime scanTime = ZonedDateTime.parse("2016-03-06T20:17:28.000-03:00");
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(scanTime);
        if (nextExecution.isPresent()) {
            final ZonedDateTime nextTime = nextExecution.get();
            assertNotNull(nextTime);
            assertEquals(DayOfWeek.SUNDAY, nextTime.getDayOfWeek());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #66: Incorrect Day Of Week processing for Quartz when Month or Year isn't '*'.
     */
    @Test
    public void testNextExecutionRightDoWForFixedYear() {
        //cron format: s,m,H,DoM,M,DoW,Y
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 * * ? * 1 2099"));
        final ZonedDateTime scanTime = ZonedDateTime.parse("2016-03-06T20:17:28.000-03:00");
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(scanTime);
        if (nextExecution.isPresent()) {
            final ZonedDateTime nextTime = nextExecution.get();
            assertNotNull(nextTime);
            assertEquals(DayOfWeek.SUNDAY, nextTime.getDayOfWeek());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #70: Illegal question mark value on cron pattern assumed valid.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalQuestionMarkValue() {
        ExecutionTime.forCron(parser.parse("0 0 12 1W ? *"));//s,m,H,DoM,M,DoW
    }

    /**
     * Issue #72: Stacktrace printed.
     */
    @Test
    public void testNextExecutionProducingInvalidPrintln() {
        final String cronText = "0 0/15 * * * ?";
        final Cron cron = parser.parse(cronText);
        ExecutionTime.forCron(cron);
    }

    /**
     * Issue #73: NextExecution not working as expected.
     */
    @Test
    public void testNextExecutionProducingInvalidValues() {
        final String cronText = "0 0 18 ? * MON";
        final Cron cron = parser.parse(cronText);
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        final ZonedDateTime now = ZonedDateTime.parse("2016-03-18T19:02:51.424+09:00");
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        if (nextExecution.isPresent()) {
            final ZonedDateTime next = nextExecution.get();
            final Optional<ZonedDateTime> nextNextExecution = executionTime.nextExecution(next);
            if (nextNextExecution.isPresent()) {
                final ZonedDateTime nextNext = nextNextExecution.get();
                assertEquals(DayOfWeek.MONDAY, next.getDayOfWeek());
                assertEquals(DayOfWeek.MONDAY, nextNext.getDayOfWeek());
                assertEquals(18, next.getHour());
                assertEquals(18, nextNext.getHour());
                return;
            }
        }
        fail(ASSERTED_EXECUTION_NOT_PRESENT);
    }

    /**
     * Test for issue #83
     * https://github.com/jmrozanec/cron-utils/issues/83
     * Reported case: Candidate values are false when combining range and multiple patterns
     * Expected: Candidate values should be correctly identified
     */
    @Test
    public void testMultipleMinuteIntervalTimeFromLastExecution() {
        final String expression = "* 8-10,23-25,38-40,53-55 * * * ? *"; // every second for intervals of minutes
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(expression));

        assertEquals(301, getDurationInSeconds(executionTime, 3, 1, 0, 0));
        assertEquals(1, getDurationInSeconds(executionTime, 13, 8, 4, 0));
        assertEquals(1, getDurationInSeconds(executionTime, 13, 11, 0, 0));
        assertEquals(63, getDurationInSeconds(executionTime, 13, 12, 2, 0));
    }

    private long getDurationInSeconds(final ExecutionTime executionTime, final int hour, final int minute, final int second, final int nanoOfSecond) {
        final Optional<Duration> duration = executionTime.timeFromLastExecution(ZonedDateTime.of(LocalDate.now(),
                                                                                LocalTime.of(hour, minute, second, nanoOfSecond),
                                                                                UTC));
        if (duration.isPresent()) {
            return duration.get().getSeconds();
        }
        throw new NullPointerException(DURATION_NOT_PRESENT_ERROR);
    }

    /**
     * Test for issue #83
     * https://github.com/jmrozanec/cron-utils/issues/83
     * Reported case: Candidate values are false when combining range and multiple patterns
     * Expected: Candidate values should be correctly identified
     */
    @Test
    public void testMultipleMinuteIntervalMatch() {
        final String testExpression = "* * 21-23,0-4 * * ?";
        assertEquals(ExecutionTime.forCron(parser.parse(testExpression)).isMatch(ZonedDateTime.of(2014, 9, 20, 20, 0, 0, 0, UTC)), false);
        assertEquals(ExecutionTime.forCron(parser.parse(testExpression)).isMatch(ZonedDateTime.of(2014, 9, 20, 21, 0, 0, 0, UTC)), true);
        assertEquals(ExecutionTime.forCron(parser.parse(testExpression)).isMatch(ZonedDateTime.of(2014, 9, 20, 0, 0, 0, 0, UTC)), true);
        assertEquals(ExecutionTime.forCron(parser.parse(testExpression)).isMatch(ZonedDateTime.of(2014, 9, 20, 4, 0, 0, 0, UTC)), true);
        assertEquals(ExecutionTime.forCron(parser.parse(testExpression)).isMatch(ZonedDateTime.of(2014, 9, 20, 5, 0, 0, 0, UTC)), false);
    }

    @Test
    public void testDayLightSavingsSwitch() {
        //every 2 minutes
        final String expression = "* 0/2 * * * ?";
        final Cron cron = parser.parse(expression);

        // SIMULATE SCHEDULE JUST PRIOR TO DST
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss").withZone(ZoneId.of("America/Denver"));
        final ZonedDateTime startTime = ZonedDateTime.parse("2016 03 13 01:59:59", formatter);

        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        final Optional<ZonedDateTime> nextExecutionBeforeDst = executionTime.nextExecution(startTime);
        if (nextExecutionBeforeDst.isPresent()) {
            final ZonedDateTime executionTimeBeforeDst = nextExecutionBeforeDst.get();
            // Assert we got 3:00am
            assertEquals("Incorrect Hour", 3, executionTimeBeforeDst.getHour());
            assertEquals("Incorrect Minute", 0, executionTimeBeforeDst.getMinute());

            final Optional<ZonedDateTime> nextExecutionAfterDst = executionTime.nextExecution(executionTimeBeforeDst.plusMinutes(1));
            if (nextExecutionAfterDst.isPresent()) {
                // SIMULATE SCHEDULE POST DST - simulate a schedule after DST 3:01 with the same cron, expect 3:02
                final ZonedDateTime executionTimeAfterDst = nextExecutionAfterDst.get();
                assertEquals("Incorrect Hour", 3, executionTimeAfterDst.getHour());
                assertEquals("Incorrect Minute", 2, executionTimeAfterDst.getMinute());

                // SIMULATE SCHEDULE NEXT DAY DST - verify after midnight on DST switch things still work as expected
                final ZonedDateTime oneDayAfterDst = ZonedDateTime.parse("2016-03-14T00:00:59Z");
                final Optional<ZonedDateTime> nextExecutionOneDayAfterDst = executionTime.nextExecution(oneDayAfterDst);
                if (nextExecutionOneDayAfterDst.isPresent()) {
                    final ZonedDateTime executionTimeOneDayAfterDst = nextExecutionOneDayAfterDst.get();
                    assertEquals("incorrect hour", executionTimeOneDayAfterDst.getHour(), 0);
                    assertEquals("incorrect minute", executionTimeOneDayAfterDst.getMinute(), 2);
                    return;
                }
            }
        }
        fail(ASSERTED_EXECUTION_NOT_PRESENT);
    }

    @Test
    public void bigNumbersOnDayOfMonthField() {
        final Cron cron = parser.parse("0 0 0 31 * ?");
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        final ZonedDateTime now = ZonedDateTime.of(2016, 11, 1, 0, 0, 0, 0, ZoneId.of("UTC"));

        //nextRun expected to be  2016-12-31 00:00:00 000
        //quartz-2.2.3 return the right date
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        if (nextExecution.isPresent()) {
            final ZonedDateTime nextRun = nextExecution.get();
            assertEquals(ZonedDateTime.of(2016, 12, 31, 0, 0, 0, 0, ZoneId.of("UTC")), nextRun);
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    @Test
    public void noSpecificDayOfMonthEvaluatedOnLastDay() {
        final Cron cron = parser.parse("0 * * ? * *");
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        final ZonedDateTime now = ZonedDateTime.of(2016, 8, 31, 10, 10, 0, 0, ZoneId.of("UTC"));
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        if (nextExecution.isPresent()) {
            final ZonedDateTime nextRun = nextExecution.get();
            assertEquals(ZonedDateTime.of(2016, 8, 31, 10, 11, 0, 0, ZoneId.of("UTC")), nextRun);
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #75: W flag not behaving as expected: did not return first workday of month, but an exception.
     */
    @Test
    public void testCronWithFirstWorkDayOfWeek() {
        final String cronText = "0 0 12 1W * ? *";
        final Cron cron = parser.parse(cronText);
        final ZonedDateTime dt = ZonedDateTime.parse("2016-03-29T00:00:59Z");

        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(dt);
        if (nextExecution.isPresent()) {
            final ZonedDateTime nextRun = nextExecution.get();
            assertEquals("incorrect Day", nextRun.getDayOfMonth(), 1); // should be April 1st (Friday)
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #81: MON-SUN flags are not mapped correctly to 1-7 number representations
     * Fixed by adding shifting function when changing monday position.
     */
    @Test
    public void testDayOfWeekMapping() {
        final ZonedDateTime fridayMorning = ZonedDateTime.of(2016, 4, 22, 0, 0, 0, 0, UTC);
        final ExecutionTime numberExec = ExecutionTime.forCron(parser.parse("0 0 12 ? * 2,3,4,5,6 *"));
        final ExecutionTime nameExec = ExecutionTime.forCron(parser.parse("0 0 12 ? * MON,TUE,WED,THU,FRI *"));
        assertEquals("same generated dates", numberExec.nextExecution(fridayMorning),
                nameExec.nextExecution(fridayMorning));
    }

    /**
     * Issue #91: Calculating the minimum interval for a cron expression.
     */
    @Test
    public void testMinimumInterval() {
        final Duration s1 = Duration.ofSeconds(1);
        assertEquals(getMinimumInterval("* * * * * ?"), s1);
        assertEquals("Should ignore whitespace", getMinimumInterval("*   *    *  *       * ?"), s1);
        assertEquals(getMinimumInterval("0/1 * * * * ?"), s1);
        assertEquals(getMinimumInterval("*/1 * * * * ?"), s1);

        final Duration s60 = Duration.ofSeconds(60);
        assertEquals(getMinimumInterval("0 * * * * ?"), s60);
        assertEquals(getMinimumInterval("0 */1 * * * ?"), s60);

        assertEquals(getMinimumInterval("0 */5 * * * ?"), Duration.ofSeconds(300));
        assertEquals(getMinimumInterval("0 0 * * * ?"), Duration.ofSeconds(3600));
        assertEquals(getMinimumInterval("0 0 */3 * * ?"), Duration.ofSeconds(10800));
        assertEquals(getMinimumInterval("0 0 0 * * ?"), Duration.ofSeconds(86400));
    }

    /**
     * Issue #110: DateTimeException thrown from ExecutionTime.nextExecution
     */
    @Test
    public void noDateTimeExceptionIsThrownGeneratingNextExecutionWithDayOfWeekFilters() {
        final ZonedDateTime wednesdayNov9 = ZonedDateTime.of(2016, 11, 9, 1, 1, 0, 0, ZoneId.of("UTC"));
        final ZonedDateTime startOfThursdayNov10 = wednesdayNov9.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        final ZonedDateTime thursdayOct27 = ZonedDateTime.of(2016, 10, 27, 23, 55, 0, 0, ZoneId.of("UTC"));
        final String[] cronExpressionsExcludingWednesdayAndIncludingThursday = {
                // Non-range type day-of-week filters function as expected...
                "0 0/1 * ? * 5",
                "0 0/1 * ? * 2,5",
                "0 0/1 * ? * THU",
                "0 0/1 * ? * THU,SAT",
                /* Range-based day-of-week filters are consitently broken. Exception thrown:
                 *  DateTimeException: Invalid value for DayOfMonth (valid values 1 - 28/31): 0
                 */
                "0 0/1 * ? * 5-6",
                "0 0/1 * ? * THU-FRI"
        };
        for (final String cronExpression : cronExpressionsExcludingWednesdayAndIncludingThursday) {
            assertExpectedNextExecution(cronExpression, wednesdayNov9, startOfThursdayNov10);
            assertExpectedNextExecution(cronExpression, thursdayOct27, thursdayOct27.plusMinutes(1));
        }
        final ZonedDateTime endOfThursdayNov3 = ZonedDateTime.of(2016, 11, 3, 23, 59, 0, 0, ZoneId.of("UTC"));
        final ZonedDateTime endOfFridayNov4 = endOfThursdayNov3.plusDays(1);
        final ZonedDateTime endOfSaturdayNov5 = endOfThursdayNov3.plusDays(2);
        final ZonedDateTime endOfMondayNov7 = endOfThursdayNov3.plusDays(4);
        assertExpectedNextExecution("0 0/1 * ? * 5", endOfThursdayNov3, startOfThursdayNov10);
        assertExpectedNextExecution("0 0/1 * ? * 2,5", endOfMondayNov7, startOfThursdayNov10);
        assertExpectedNextExecution("0 0/1 * ? * THU", endOfThursdayNov3, startOfThursdayNov10);
        assertExpectedNextExecution("0 0/1 * ? * THU,SAT", endOfSaturdayNov5, startOfThursdayNov10);
        assertExpectedNextExecution("0 0/1 * ? * 5-6", endOfFridayNov4, startOfThursdayNov10); //110
        assertExpectedNextExecution("0 0/1 * ? * THU-FRI", endOfFridayNov4, startOfThursdayNov10); //110
    }

    /**
     * Issue #114: Describe day of week is incorrect.
     */
    @Test
    public void descriptionForExpressionTellsWrongDoW() {
        final CronDescriptor descriptor = CronDescriptor.instance();
        final Cron quartzCron = parser.parse("0 0 8 ? * SUN *");
        assertEquals("at 08:00 at Sunday day", descriptor.describe(quartzCron));
    }

    /**
     * Issue #117: Last Day of month Skipped on Quartz Expression: 0 * * ? * *.
     */
    @Test
    public void noSpecificDayOfMonth() {
        final Cron cron = parser.parse("0 * * ? * *");
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        final ZonedDateTime now = ZonedDateTime.of(2016, 8, 30, 23, 59, 0, 0, ZoneId.of("UTC"));
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        if (nextExecution.isPresent()) {
            final ZonedDateTime nextRun = nextExecution.get();
            assertEquals(ZonedDateTime.of(2016, 8, 31, 0, 0, 0, 0, ZoneId.of("UTC")), nextRun);
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #123:
     * https://github.com/jmrozanec/cron-utils/issues/123
     * Reported case: next execution time is set improperly
     * Potential duplicate: https://github.com/jmrozanec/cron-utils/issues/124
     */
    @Test
    public void testNextExecutionTimeProperlySet() {
        final CronParser quartzCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
        final String quartzCronExpression2 = "0 5/15 * * * ? *";
        final Cron parsedQuartzCronExpression = quartzCronParser.parse(quartzCronExpression2);

        final ExecutionTime executionTime = ExecutionTime.forCron(parsedQuartzCronExpression);

        final ZonedDateTime zonedDateTime = LocalDateTime.of(2016, 7, 30, 15, 0, 0, 527).atZone(ZoneOffset.UTC);
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(zonedDateTime);
        final Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(zonedDateTime);
        if (nextExecution.isPresent() && lastExecution.isPresent()) {
            final ZonedDateTime nextExecutionTime = nextExecution.get();
            final ZonedDateTime lastExecutionTime = lastExecution.get();

            assertEquals("2016-07-30T14:50Z", lastExecutionTime.toString());
            assertEquals("2016-07-30T15:05Z", nextExecutionTime.toString());
        } else {
            fail(ASSERTED_EXECUTION_NOT_PRESENT);
        }
    }

    /**
     * Issue #124:
     * https://github.com/jmrozanec/cron-utils/issues/124
     * Reported case: next execution time is set improperly
     * Potential duplicate: https://github.com/jmrozanec/cron-utils/issues/123
     */
    @Test
    public void testNextExecutionTimeProperlySet2() {
        final CronParser quartzCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
        final String quartzCronExpression2 = "0 3/27 10-14 * * ? *";
        final Cron parsedQuartzCronExpression = quartzCronParser.parse(quartzCronExpression2);

        final ExecutionTime executionTime = ExecutionTime.forCron(parsedQuartzCronExpression);

        final ZonedDateTime zonedDateTime = LocalDateTime.of(2016, 1, 1, 10, 0, 0, 0).atZone(ZoneOffset.UTC);

        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(zonedDateTime);
        if (nextExecution.isPresent()) {
            final ZonedDateTime nextExecutionTime = nextExecution.get();
            assertEquals("2016-01-01T10:03Z", nextExecutionTime.toString());
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #133:
     * https://github.com/jmrozanec/cron-utils/issues/133
     * Reported case: QUARTZ cron definition: 31 not supported on the day-of-month field
     */
    @Test
    public void validate31IsSupportedForDoM() {
        parser.parse("0 0 0 31 * ?");
    }

    /**
     * Issue #136: Bug exposed at PR #136
     * https://github.com/jmrozanec/cron-utils/pull/136
     * Reported case: when executing isMatch for a given range of dates,
     * if date is invalid, we get an exception, not a boolean as response.
     */
    @Test
    public void validateIsMatchForRangeOfDates() {
        final Cron cron = parser.parse("* * * 05 05 ? 2004");
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime start = ZonedDateTime.of(2004, 5, 5, 23, 55, 0, 0, ZoneId.of("UTC"));
        final ZonedDateTime end = ZonedDateTime.of(2004, 5, 6, 1, 0, 0, 0, ZoneId.of("UTC"));
        while (start.compareTo(end) < 0) {
            executionTime.isMatch(start);
            start = start.plusMinutes(1);
        }
    }

    /**
     * Issue #140: https://github.com/jmrozanec/cron-utils/pull/140
     * IllegalArgumentException: Values must not be empty
     */
    @Test
    public void nextExecutionNotFail() {
        final Cron parsed = parser.parse("0 0 10 ? * SAT-SUN");
        final ExecutionTime executionTime = ExecutionTime.forCron(parsed);
        executionTime.nextExecution(ZonedDateTime.now());
    }

    /**
     * Issue #142: https://github.com/jmrozanec/cron-utils/pull/142
     * Special Character L for day of week behaves differently in Quartz
     * @throws ParseException in case the CronExpression can not be created
     */
    @Test
    public void lastDayOfTheWeek() throws ParseException {
        // L (“last”) - If used in the day-of-week field by itself, it simply means “7” or “SAT”.
        final Cron cron = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ)).parse("0 0 0 ? * L *");

        final ZoneId utc = ZoneId.of("UTC");
        final ZonedDateTime date = LocalDate.parse("2016-12-22").atStartOfDay(utc);   // Thursday
        final ZonedDateTime expected = date.plusDays(2);   // Saturday

        final Optional<ZonedDateTime> nextExecution = ExecutionTime.forCron(cron).nextExecution(date);
        if (nextExecution.isPresent()) {
            final ZonedDateTime cronUtilsNextTime = nextExecution.get();// 2016-12-30T00:00:00Z

            final org.quartz.CronExpression cronExpression = new org.quartz.CronExpression(cron.asString());
            cronExpression.setTimeZone(TimeZone.getTimeZone(utc));
            final Date quartzNextTime = cronExpression.getNextValidTimeAfter(Date.from(date.toInstant()));// 2016-12-24T00:00:00Z

            assertEquals(expected.toInstant(), quartzNextTime.toInstant());    // test the reference implementation
            assertEquals(expected.toInstant(), cronUtilsNextTime.toInstant()); // and compare with cronUtils
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #143: https://github.com/jmrozanec/cron-utils/pull/143
     * ExecutionTime.lastExecution() throws Exception when cron defines at 31 Dec
     */
    @Test
    public void lastExecutionDec31NotFail() {
        final ExecutionTime et = ExecutionTime.forCron(parser.parse("0 0 12 31 12 ? *"));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(et.lastExecution(ZonedDateTime.now()).toString());
        }
    }

    /**
     * Issue #144
     * https://github.com/jmrozanec/cron-utils/issues/144
     * Reported case: periodic incremental hours does not start and end
     * at beginning and end of given period
     */
    @Test
    public void testPeriodicIncrementalHoursIgnorePeriodBounds() {
        final Cron cron = parser.parse("0 0 16-19/2 * * ?");
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime start = ZonedDateTime.of(2016, 12, 27, 8, 15, 0, 0, ZoneId.of("UTC"));
        final ZonedDateTime[] expectedDateTimes = new ZonedDateTime[] {
                ZonedDateTime.of(2016, 12, 27, 16, 0, 0, 0, ZoneId.of("UTC")),
                ZonedDateTime.of(2016, 12, 27, 18, 0, 0, 0, ZoneId.of("UTC")),
                ZonedDateTime.of(2016, 12, 28, 16, 0, 0, 0, ZoneId.of("UTC")),
                ZonedDateTime.of(2016, 12, 28, 18, 0, 0, 0, ZoneId.of("UTC")),
                ZonedDateTime.of(2016, 12, 29, 16, 0, 0, 0, ZoneId.of("UTC")),
        };
        for (final ZonedDateTime expectedDateTime : expectedDateTimes) {
            final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(start);
            assert nextExecution.isPresent();
            final ZonedDateTime next = nextExecution.get();
            start = next;
            assertEquals(expectedDateTime, next);
        }
        start = start.plusSeconds(1);
        for (int i = expectedDateTimes.length - 1; i >= 0; i--) {
            final Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(start);
            assert lastExecution.isPresent();
            final ZonedDateTime last = lastExecution.get();
            start = last;
            assertEquals(expectedDateTimes[i], last);
        }
    }

    /**
     * Issue #153
     * https://github.com/jmrozanec/cron-utils/issues/153
     * Reported case: executionTime.nextExecution fails to find when current month does not have desired day
     */
    @Test
    public void mustJumpToNextMonthIfCurrentMonthDoesNotHaveDesiredDay() {
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0 8 31 * ?"));//8:00 on every 31th of Month
        final ZonedDateTime start = ZonedDateTime.of(2017, 04, 10, 0, 0, 0, 0, ZoneId.systemDefault());
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(start);
        if (nextExecution.isPresent()) {
            final ZonedDateTime next = nextExecution.get();
            final ZonedDateTime expected = ZonedDateTime.of(2017, 05, 31, 8, 0, 0, 0, ZoneId.systemDefault());
            assertEquals(expected, next);
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    /**
     * Issue #153
     * https://github.com/jmrozanec/cron-utils/issues/153
     * Reported case: executionTime.nextExecution fails to find when current month does not have desired day
     */
    @Test
    public void mustJumpToEndOfMonthIfCurrentMonthHasDesiredDay() {
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0 8 31 * ?"));//8:00 on every 31th of Month
        final ZonedDateTime start = ZonedDateTime.of(2017, 01, 10, 0, 0, 0, 0, ZoneId.systemDefault());
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(start);
        if (nextExecution.isPresent()) {
            final ZonedDateTime next = nextExecution.get();
            final ZonedDateTime expected = ZonedDateTime.of(2017, 01, 31, 8, 0, 0, 0, ZoneId.systemDefault());
            assertEquals(expected, next);
        } else {
            fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    //FIXME fix this test
    @Ignore("bug fix pending")
    @Test //#192
    public void mustMatchLowerBoundDateMatchingCronExpressionRequirements() {
        final ZonedDateTime start = ZonedDateTime.of(2017, 01, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0 0 1 * ?")); // every 1st of Month 1970-2099
        final ExecutionTime constraintExecutionTime = ExecutionTime.forCron(parser.parse("0 0 0 1 * ? 2017")); // every 1st of Month for 2017
        assertEquals("year constraint shouldn't have an impact on next execution", executionTime.nextExecution(start.minusSeconds(1)),
                constraintExecutionTime.nextExecution(start.minusSeconds(1)));
        assertEquals("year constraint shouldn't have an impact on match result", executionTime.isMatch(start), constraintExecutionTime.isMatch(start));
    }

    /**
     * Issue #312
     * https://github.com/jmrozanec/cron-utils/issues/312
     */
    @Test
    public void testLastExecutionIssue312() {
        // Every day at 20:00
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0 20 * * ? *"));
        ZonedDateTime time = ZonedDateTime.now();
        Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(time);
        assertTrue("There was no last execution", lastExecution.isPresent());
        assertEquals("Last execution did not happen at 20:00 hours", LocalTime.of(20, 0), lastExecution.get().toLocalTime());
        if (time.toLocalTime().isBefore(LocalTime.of(20, 0))) {
            assertEquals("The last execution should be on the previous day", lastExecution.get().toLocalDate(), time.toLocalDate().minusDays(1));
        } else {
            assertEquals("The last execution should be on the same day", lastExecution.get().toLocalDate(), time.toLocalDate());
        }
    }

    private Duration getMinimumInterval(final String quartzPattern) {
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(quartzPattern));
        final ZonedDateTime coolDay = ZonedDateTime.of(2016, 1, 1, 0, 0, 0, 0, UTC);
        // Find next execution time #1
        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(coolDay);
        if (nextExecution.isPresent()) {
            final ZonedDateTime time = nextExecution.get();
            // Find next execution time #2 right after #1, the interval between them is minimum
            final Optional<Duration> duration = executionTime.timeToNextExecution(time);
            if (duration.isPresent()) {
                return duration.get();
            }
            throw new NullPointerException(DURATION_NOT_PRESENT_ERROR);
        }
        throw new NullPointerException(NEXT_EXECUTION_NOT_PRESENT_ERROR);
    }

    private ZonedDateTime truncateToSeconds(final ZonedDateTime dateTime) {
        return dateTime.truncatedTo(ChronoUnit.SECONDS);
    }

    private void assertExpectedNextExecution(final String cronExpression, final ZonedDateTime lastRun,
            final ZonedDateTime expectedNextRun) {

        final String testCaseDescription = "cron expression '" + cronExpression + "' with zdt " + lastRun;
        LOGGER.debug("TESTING: " + testCaseDescription);
        final Cron cron = parser.parse(cronExpression);
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        try {
            final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(lastRun);
            if (nextExecution.isPresent()) {
                final ZonedDateTime nextRun = nextExecution.get();
                assertEquals(testCaseDescription, expectedNextRun, nextRun);
            } else {
                fail(NEXT_EXECUTION_NOT_PRESENT_ERROR);
            }
        } catch (final DateTimeException e) {
            fail("Issue #110: " + testCaseDescription + " led to " + e);
        }
    }
}
