package com.cronutils.model.time;

import android.support.test.runner.AndroidJUnit4;

import com.cronutils.BaseAndroidTest;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.google.common.base.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.threeten.bp.*;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;
import java.util.*;

import static com.cronutils.model.CronType.QUARTZ;
import static org.threeten.bp.ZoneOffset.UTC;
import static org.junit.Assert.*;

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
@RunWith(AndroidJUnit4.class)
public class ExecutionTimeQuartzIntegrationTest extends BaseAndroidTest {
    private CronParser parser;
    private static final String EVERY_SECOND = "* * * * * ? *";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
    }

    @Test
    public void testForCron() throws Exception {
        assertEquals(ExecutionTime.class, ExecutionTime.forCron(parser.parse(EVERY_SECOND)).getClass());
    }

    @Test
    public void testNextExecutionEverySecond() throws Exception {
        ZonedDateTime now = truncateToSeconds(ZonedDateTime.now());
        ZonedDateTime expected = truncateToSeconds(now.plusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(EVERY_SECOND));
        assertEquals(expected, executionTime.nextExecution(now).get());
    }

    @Test
    public void testTimeToNextExecution() throws Exception {
        ZonedDateTime now = truncateToSeconds(ZonedDateTime.now());
        ZonedDateTime expected = truncateToSeconds(now.plusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(EVERY_SECOND));
        assertEquals(Duration.between(now, expected), executionTime.timeToNextExecution(now).get());
    }

    @Test
    public void testLastExecution() throws Exception {
        ZonedDateTime now = truncateToSeconds(ZonedDateTime.now());
        ZonedDateTime expected = truncateToSeconds(now.minusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(EVERY_SECOND));
        assertEquals(expected, executionTime.lastExecution(now).get());
    }

    @Test
    public void testTimeFromLastExecution() throws Exception {
        ZonedDateTime now = truncateToSeconds(ZonedDateTime.now());
        ZonedDateTime expected = truncateToSeconds(now.minusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(EVERY_SECOND));
        assertEquals(Duration.between(expected, now), executionTime.timeToNextExecution(now).get());
    }

    /**
     * Test for issue #9
     * https://github.com/jmrozanec/cron-utils/issues/9
     * Reported case: If you write a cron expression that contains a month or day of week, nextExection() ignores it.
     * Expected: should not ignore month or day of week field
     */
    @Test
    public void testDoesNotIgnoreMonthOrDayOfWeek(){
        //seconds, minutes, hours, dayOfMonth, month, dayOfWeek
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 11 11 11 11 ?"));
        ZonedDateTime now = ZonedDateTime.of(2015, 4, 15, 0, 0, 0, 0, UTC);
        ZonedDateTime whenToExecuteNext = executionTime.nextExecution(now).get();
        assertEquals(2015, whenToExecuteNext.getYear());
        assertEquals(11, whenToExecuteNext.getMonthValue());
        assertEquals(11, whenToExecuteNext.getDayOfMonth());
        assertEquals(11, whenToExecuteNext.getHour());
        assertEquals(11, whenToExecuteNext.getMinute());
        assertEquals(0, whenToExecuteNext.getSecond());
    }

    /**
     * Test for issue #18
     * @throws Exception
     */
    @Test
    public void testHourlyIntervalTimeFromLastExecution() throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime previousHour = now.minusHours(1);
        String quartzCronExpression = String.format("0 0 %s * * ?", previousHour.getHour());
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(quartzCronExpression));

        assertTrue(executionTime.timeFromLastExecution(now).get().toMinutes() <= 120);
    }

    /**
     * Test for issue #19
     * https://github.com/jmrozanec/cron-utils/issues/19
     * Reported case: When nextExecution shifts to the 24th hour (e.g. 23:59:59 + 00:00:01), JodaTime will throw an exception
     * Expected: should shift one day
     */
    @Test
    public void testShiftTo24thHour() {
        String expression = "0/1 * * 1/1 * ? *";  // every second every day
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(expression));

        ZonedDateTime now = ZonedDateTime.of(LocalDate.of(2016, 8, 5), LocalTime.of(23, 59, 59, 0), UTC);
        ZonedDateTime expected = now.plusSeconds(1);
        ZonedDateTime nextExecution = executionTime.nextExecution(now).get();

        assertEquals(expected, nextExecution);
    }

    /**
     * Test for issue #19
     * https://github.com/jmrozanec/cron-utils/issues/19
     * Reported case: When nextExecution shifts to 32nd day (e.g. 2015-01-31 23:59:59 + 00:00:01), JodaTime will throw an exception
     * Expected: should shift one month
     */
    @Test
    public void testShiftTo32ndDay() {
        String expression = "0/1 * * 1/1 * ? *";  // every second every day
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(expression));

        ZonedDateTime now = ZonedDateTime.of(2015, 1, 31, 23, 59, 59, 0, UTC);
        ZonedDateTime expected = now.plusSeconds(1);
        ZonedDateTime nextExecution = executionTime.nextExecution(now).get();

        assertEquals(expected, nextExecution);
    }

    /**
     * Issue #24: next execution not properly calculated
     */
    @Test
    public void testTimeShiftingProperlyDone() throws Exception {
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0/10 22 ? * *"));
        ZonedDateTime nextExecution = executionTime.nextExecution(ZonedDateTime.now().withHour(15).withMinute(27)).get();
        assertEquals(22, nextExecution.getHour());
        assertEquals(0, nextExecution.getMinute());
    }

    /**
     * Issue #27: execution time properly calculated
     */
    @Test
    public void testMonthRangeExecutionTime(){
        assertNotNull(ExecutionTime.forCron(parser.parse("0 0 0 * JUL-AUG ? *")));
    }

    /**
     * Issue #30: execution time properly calculated
     */
    @Test
    public void testSaturdayExecutionTime(){
        ZonedDateTime now = ZonedDateTime.now();
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0 3 ? * 6"));
        ZonedDateTime last = executionTime.lastExecution(now).get();
        ZonedDateTime next = executionTime.nextExecution(now).get();
        assertNotEquals(last, next);
    }

    /**
     * Issue: execution time properly calculated
     */
    @Test
    public void testWeekdayExecutionTime(){
        ZonedDateTime now = ZonedDateTime.now();
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0 3 ? * *"));
        ZonedDateTime last = executionTime.lastExecution(now).get();
        ZonedDateTime next = executionTime.nextExecution(now).get();
        assertNotEquals(last, next);
    }

    /**
     * Issue #64: Incorrect next execution time for ranges
     */
    @Test
    public void testExecutionTimeForRanges(){
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("* 10-20 * * * ? 2099"));
        ZonedDateTime scanTime = ZonedDateTime.parse("2016-02-29T11:00:00.000-06:00");
        ZonedDateTime nextTime = executionTime.nextExecution(scanTime).get();
        assertNotNull(nextTime);
        assertEquals(10, nextTime.getMinute());
    }

    /**
     * Issue #65: Incorrect last execution time for fixed month
     */
    @Test
    public void testLastExecutionTimeForFixedMonth(){
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 30 12 1 9 ? 2010"));
        ZonedDateTime scanTime = ZonedDateTime.parse("2016-01-08T11:00:00.000-06:00");
        ZonedDateTime lastTime = executionTime.lastExecution(scanTime).get();
        assertNotNull(lastTime);
        assertEquals(9, lastTime.getMonthValue());
    }

    /**
     * Issue #66: Incorrect Day Of Week processing for Quartz when Month or Year isn't '*'.
     */
    @Test
    public void testNextExecutionRightDoWForFixedMonth(){
        //cron format: s,m,H,DoM,M,DoW,Y
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 * * ? 5 1 *"));
        ZonedDateTime scanTime = ZonedDateTime.parse("2016-03-06T20:17:28.000-03:00");
        ZonedDateTime nextTime = executionTime.nextExecution(scanTime).get();
        assertNotNull(nextTime);
        assertEquals(DayOfWeek.SUNDAY, nextTime.getDayOfWeek());
    }

    /**
     * Issue #66: Incorrect Day Of Week processing for Quartz when Month or Year isn't '*'.
     */
    @Test
    public void testNextExecutionRightDoWForFixedYear(){
        //cron format: s,m,H,DoM,M,DoW,Y
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 * * ? * 1 2099"));
        ZonedDateTime scanTime = ZonedDateTime.parse("2016-03-06T20:17:28.000-03:00");
        ZonedDateTime nextTime = executionTime.nextExecution(scanTime).get();
        assertNotNull(nextTime);
        assertEquals(DayOfWeek.SUNDAY, nextTime.getDayOfWeek());
    }

    /**
     * Issue #70: Illegal question mark value on cron pattern assumed valid.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalQuestionMarkValue(){
        ExecutionTime.forCron(parser.parse("0 0 12 1W ? *"));//s,m,H,DoM,M,DoW
    }

    /**
     * Issue #72: Stacktrace printed.
     * TODO: Although test is passing, there is some stacktrace printed indicating there may be something wrong.
     * TODO: We should analyze it and fix the eventual issue.
     */
    @Test//TODO
    public void testNextExecutionProducingInvalidPrintln(){
        String cronText = "0 0/15 * * * ?";
        Cron cron = parser.parse(cronText);
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
    }

    /**
     * Issue #73: NextExecution not working as expected
     */
    @Test
    public void testNextExecutionProducingInvalidValues(){
        String cronText = "0 0 18 ? * MON";
        Cron cron = parser.parse(cronText);
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime now = ZonedDateTime.parse("2016-03-18T19:02:51.424+09:00");
        ZonedDateTime next = executionTime.nextExecution(now).get();
        ZonedDateTime nextNext = executionTime.nextExecution(next).get();
        assertEquals(DayOfWeek.MONDAY, next.getDayOfWeek());
        assertEquals(DayOfWeek.MONDAY, nextNext.getDayOfWeek());
        assertEquals(18, next.getHour());
        assertEquals(18, nextNext.getHour());
    }

    /**
     * Test for issue #83
     * https://github.com/jmrozanec/cron-utils/issues/83
     * Reported case: Candidate values are false when combining range and multiple patterns
     * Expected: Candidate values should be correctly identified
     * @throws Exception
     */
    @Test
    public void testMultipleMinuteIntervalTimeFromLastExecution() {
        String expression = "* 8-10,23-25,38-40,53-55 * * * ? *"; // every second for intervals of minutes
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(expression));

        assertEquals(301, executionTime.timeFromLastExecution(ZonedDateTime.of(LocalDate.now(), LocalTime.of(3, 1, 0, 0), UTC)).get().getSeconds());
        assertEquals(1, executionTime.timeFromLastExecution(ZonedDateTime.of(LocalDate.now(), LocalTime.of(13, 8, 4, 0), UTC)).get().getSeconds());
        assertEquals(1, executionTime.timeFromLastExecution(ZonedDateTime.of(LocalDate.now(), LocalTime.of(13, 11, 0, 0), UTC)).get().getSeconds());
        assertEquals(63, executionTime.timeFromLastExecution(ZonedDateTime.of(LocalDate.now(), LocalTime.of(13, 12, 2, 0), UTC)).get().getSeconds());
    }

    /**
     * Test for issue #83
     * https://github.com/jmrozanec/cron-utils/issues/83
     * Reported case: Candidate values are false when combining range and multiple patterns
     * Expected: Candidate values should be correctly identified
     * @throws Exception
     */
    @Test
    public void testMultipleMinuteIntervalMatch() {
        assertEquals(ExecutionTime.forCron(parser.parse("* * 21-23,0-4 * * ?")).isMatch(ZonedDateTime.of(2014, 9, 20, 20, 0, 0, 0, UTC)), false);
        assertEquals(ExecutionTime.forCron(parser.parse("* * 21-23,0-4 * * ?")).isMatch(ZonedDateTime.of(2014, 9, 20, 21, 0, 0, 0, UTC)), true);
        assertEquals(ExecutionTime.forCron(parser.parse("* * 21-23,0-4 * * ?")).isMatch(ZonedDateTime.of(2014, 9, 20, 0, 0, 0, 0, UTC)), true);
        assertEquals(ExecutionTime.forCron(parser.parse("* * 21-23,0-4 * * ?")).isMatch(ZonedDateTime.of(2014, 9, 20, 4, 0, 0, 0, UTC)), true);
        assertEquals(ExecutionTime.forCron(parser.parse("* * 21-23,0-4 * * ?")).isMatch(ZonedDateTime.of(2014, 9, 20, 5, 0, 0, 0, UTC)), false);
    }

    @Test
    public void testDayLightSavingsSwitch() {
        //every 2 minutes
        String expression = "* 0/2 * * * ?";
        Cron cron = parser.parse(expression);

        // SIMULATE SCHEDULE JUST PRIOR TO DST
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss")
                .withZone(ZoneId.of("America/Denver"));
        ZonedDateTime prevRun = ZonedDateTime.parse("2016 03 13 01:59:59", formatter);

        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime nextRun = executionTime.nextExecution(prevRun).get();
        // Assert we got 3:00am
        assertEquals("Incorrect Hour", 3, nextRun.getHour());
        assertEquals("Incorrect Minute", 0, nextRun.getMinute());

        // SIMULATE SCHEDULE POST DST - simulate a schedule after DST 3:01 with the same cron, expect 3:02
        nextRun = nextRun.plusMinutes(1);
        nextRun = executionTime.nextExecution(nextRun).get();
        assertEquals("Incorrect Hour", 3, nextRun.getHour());
        assertEquals("Incorrect Minute", 2, nextRun.getMinute());

        // SIMULATE SCHEDULE NEXT DAY DST - verify after midnight on DST switch things still work as expected
        prevRun = ZonedDateTime.parse("2016-03-14T00:00:59Z");
        nextRun = executionTime.nextExecution(prevRun).get();
        assertEquals("incorrect hour", nextRun.getHour(), 0);
        assertEquals("incorrect minute", nextRun.getMinute(), 2);
    }

    @Test
    public void bigNumbersOnDayOfMonthField(){
        Cron cron = parser.parse("0 0 0 31 * ?");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime now = ZonedDateTime.of(2016, 11, 1, 0, 0, 0, 0, ZoneId.of("UTC"));

        //nextRun expected to be  2016-12-31 00:00:00 000
        //quartz-2.2.3 return the right date
        ZonedDateTime nextRun = executionTime.nextExecution(now).get();

        assertEquals(ZonedDateTime.of(2016, 12, 31, 0, 0, 0, 0, ZoneId.of("UTC")), nextRun);
    }

    @Test
    public void noSpecificDayOfMonthEvaluatedOnLastDay() {
        Cron cron = parser.parse("0 * * ? * *");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime now = ZonedDateTime.of(2016, 8, 31, 10, 10, 0,0,ZoneId.of("UTC"));
        ZonedDateTime nextRun = executionTime.nextExecution(now).get();

        assertEquals(ZonedDateTime.of(2016, 8, 31, 10, 11, 0, 0, ZoneId.of("UTC")), nextRun);
    }

    /**
     * Issue #75: W flag not behaving as expected: did not return first workday of month, but an exception
     */
    @Test
    public void testCronWithFirstWorkDayOfWeek() {
        String cronText = "0 0 12 1W * ? *";
        Cron cron = parser.parse(cronText);
        ZonedDateTime dt = ZonedDateTime.parse("2016-03-29T00:00:59Z");

        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime nextRun = executionTime.nextExecution(dt).get();
        assertEquals("incorrect Day", nextRun.getDayOfMonth(), 1); // should be April 1st (Friday)
    }

    /**
     * Issue #81: MON-SUN flags are not mapped correctly to 1-7 number representations
     * Fixed by adding shifting function when changing monday position.
     */
    @Test
    public void testDayOfWeekMapping() {
        ZonedDateTime fridayMorning = ZonedDateTime.of(2016, 4, 22, 0, 0, 0, 0, UTC);
        ExecutionTime numberExec = ExecutionTime.forCron(parser.parse("0 0 12 ? * 2,3,4,5,6 *"));
        ExecutionTime nameExec = ExecutionTime.forCron(parser.parse("0 0 12 ? * MON,TUE,WED,THU,FRI *"));
        assertEquals("same generated dates", numberExec.nextExecution(fridayMorning),
                nameExec.nextExecution(fridayMorning));
    }

    /**
     * Issue #91: Calculating the minimum interval for a cron expression.
     */
    @Test
    public void testMinimumInterval() {
        Duration s1 = Duration.ofSeconds(1);
        assertEquals(getMinimumInterval("* * * * * ?"), s1);
        assertEquals("Should ignore whitespace", getMinimumInterval("*   *    *  *       * ?"), s1);
        assertEquals(getMinimumInterval("0/1 * * * * ?"), s1);
        assertEquals(getMinimumInterval("*/1 * * * * ?"), s1);

        Duration s60 = Duration.ofSeconds(60);
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
        ZonedDateTime wednesdayNov9 = ZonedDateTime.of(2016, 11, 9, 1, 1, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime startOfThursdayNov10 = wednesdayNov9.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime thursdayOct27 = ZonedDateTime.of(2016, 10, 27, 23, 55, 0, 0, ZoneId.of("UTC"));
        String[] cronExpressionsExcludingWednesdayAndIncludingThursday = {
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
        for(String cronExpression : cronExpressionsExcludingWednesdayAndIncludingThursday) {
            assertExpectedNextExecution(cronExpression, wednesdayNov9, startOfThursdayNov10);
            assertExpectedNextExecution(cronExpression, thursdayOct27, thursdayOct27.plusMinutes(1));
        }
        ZonedDateTime endOfThursdayNov3 = ZonedDateTime.of(2016, 11, 3, 23, 59, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime endOfFridayNov4 = endOfThursdayNov3.plusDays(1);
        ZonedDateTime endOfSaturdayNov5 = endOfThursdayNov3.plusDays(2);
        ZonedDateTime endOfMondayNov7 = endOfThursdayNov3.plusDays(4);
        assertExpectedNextExecution("0 0/1 * ? * 5", endOfThursdayNov3, startOfThursdayNov10);
        assertExpectedNextExecution("0 0/1 * ? * 2,5", endOfMondayNov7, startOfThursdayNov10);
        assertExpectedNextExecution("0 0/1 * ? * THU", endOfThursdayNov3, startOfThursdayNov10);
        assertExpectedNextExecution("0 0/1 * ? * THU,SAT", endOfSaturdayNov5, startOfThursdayNov10);
        assertExpectedNextExecution("0 0/1 * ? * 5-6", endOfFridayNov4, startOfThursdayNov10); //110
        assertExpectedNextExecution("0 0/1 * ? * THU-FRI", endOfFridayNov4, startOfThursdayNov10); //110
    }

    /**
     * Issue #114: Describe day of week is incorrect
     */
    @Test
    public void descriptionForExpressionTellsWrongDoW(){
        CronDescriptor descriptor = CronDescriptor.instance();
        Cron quartzCron = parser.parse("0 0 8 ? * SUN *");
        //TODO enable: assertEquals("at 08:00 at Sunday day", descriptor.describe(quartzCron));
    }

    /**
     * Issue #117: Last Day of month Skipped on Quartz Expression: 0 * * ? * *
     */
    @Test
    public void noSpecificDayOfMonth() {
        Cron cron = parser.parse("0 * * ? * *");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime now = ZonedDateTime.of(2016, 8, 30, 23, 59, 0,0,ZoneId.of("UTC"));
        ZonedDateTime nextRun = executionTime.nextExecution(now).get();

        assertEquals(ZonedDateTime.of(2016, 8, 31, 0, 0, 0,0, ZoneId.of("UTC")), nextRun);
    }

    /**
     * Issue #123:
     * https://github.com/jmrozanec/cron-utils/issues/123
     * Reported case: next execution time is set improperly
     * Potential duplicate: https://github.com/jmrozanec/cron-utils/issues/124
     */
    @Test
    public void testNextExecutionTimeProperlySet(){
        CronParser quartzCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
        String quartzCronExpression2 = "0 5/15 * * * ? *";
        Cron parsedQuartzCronExpression = quartzCronParser.parse(quartzCronExpression2);

        ExecutionTime executionTime = ExecutionTime.forCron(parsedQuartzCronExpression);

        ZonedDateTime zonedDateTime = LocalDateTime.of(2016, 7, 30, 15, 0, 0, 527).atZone(ZoneOffset.UTC);

        ZonedDateTime nextExecution = executionTime.nextExecution(zonedDateTime).get();
        ZonedDateTime lastExecution = executionTime.lastExecution(zonedDateTime).get();

        assertEquals("2016-07-30T14:50Z", lastExecution.toString());
        assertEquals("2016-07-30T15:05Z", nextExecution.toString());
    }

    /**
     * Issue #124:
     * https://github.com/jmrozanec/cron-utils/issues/124
     * Reported case: next execution time is set improperly
     * Potential duplicate: https://github.com/jmrozanec/cron-utils/issues/123
     */
    @Test
    public void testNextExecutionTimeProperlySet2(){
        CronParser quartzCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
        String quartzCronExpression2 = "0 3/27 10-14 * * ? *";
        Cron parsedQuartzCronExpression = quartzCronParser.parse(quartzCronExpression2);

        ExecutionTime executionTime = ExecutionTime.forCron(parsedQuartzCronExpression);

        ZonedDateTime zonedDateTime = LocalDateTime.of(2016, 1, 1, 10, 0, 0, 0).atZone(ZoneOffset.UTC);

        ZonedDateTime nextExecution = executionTime.nextExecution(zonedDateTime).get();

        assertEquals("2016-01-01T10:03Z", nextExecution.toString());
    }

    /**
     * Issue #133:
     * https://github.com/jmrozanec/cron-utils/issues/133
     * Reported case: QUARTZ cron definition: 31 not supported on the day-of-month field
     */
    @Test
    public void validate31IsSupportedForDoM(){
        parser.parse("0 0 0 31 * ?");
    }

    /**
     * Issue #136: Bug exposed at PR #136
     * https://github.com/jmrozanec/cron-utils/pull/136
     * Reported case: when executing isMatch for a given range of dates,
     * if date is invalid, we get an exception, not a boolean as response.
     */
    @Test
    public void validateIsMatchForRangeOfDates(){
        Cron cron = parser.parse("* * * 05 05 ? 2004");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime start = ZonedDateTime.of(2004, 5, 5, 23, 55, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime end = ZonedDateTime.of(2004, 5, 6, 1, 0, 0, 0, ZoneId.of("UTC"));
        while(start.compareTo(end)<0){
            executionTime.isMatch(start);
            start = start.plusMinutes(1);
        }
    }

    /**
     * Issue #140: https://github.com/jmrozanec/cron-utils/pull/140
     * IllegalArgumentException: Values must not be empty
     */
    @Test
    public void nextExecutionNotFail(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        Cron parsed = parser.parse("0 0 10 ? * SAT-SUN");
        ExecutionTime executionTime = ExecutionTime.forCron(parsed);
        Optional<ZonedDateTime> next = executionTime.nextExecution(ZonedDateTime.now());
    }

    /**
     * Issue #142: https://github.com/jmrozanec/cron-utils/pull/142
     * Special Character L for day of week behaves differently in Quartz
     */
//    @Test //TODO
    public void lastDayOfTheWeek() throws Exception {
        Cron cron = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ)).parse("0 0 0 ? * L *");

        ZoneId utc = ZoneId.of("UTC");
        ZonedDateTime date = LocalDate.parse("2016-12-22").atStartOfDay(utc);

        ZonedDateTime cronUtilsNextTime = ExecutionTime.forCron(cron).nextExecution(date).get();// 2016-12-30T00:00:00Z

        org.quartz.CronExpression cronExpression = new org.quartz.CronExpression(cron.asString());
        cronExpression.setTimeZone(DateTimeUtils.toTimeZone(utc));
        Date quartzNextTime = cronExpression.getNextValidTimeAfter(DateTimeUtils.toDate(date.toInstant()));// 2016-12-24T00:00:00Z

        assertEquals(DateTimeUtils.toInstant(quartzNextTime), cronUtilsNextTime.toInstant()); // false
    }

    /**
     * Issue #143: https://github.com/jmrozanec/cron-utils/pull/143
     * ExecutionTime.lastExecution() throws Exception when cron defines at 31 Dec
     */
    @Test
    public void lastExecutionDec31NotFail(){
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
        ExecutionTime et = ExecutionTime.forCron(parser.parse("0 0 12 31 12 ? *"));
        System.out.println(et.lastExecution(ZonedDateTime.now()));
    }

    /**
     * Issue #144
     * https://github.com/jmrozanec/cron-utils/issues/144
     * Reported case: periodic incremental hours does not start and end
     * at beginning and end of given period
     */
//    @Test//TODO
    public void testPeriodicIncrementalHoursIgnorePeriodBounds() {
        Cron cron = parser.parse("0 0 16-19/2 * * ?");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime start = ZonedDateTime.of(2016, 12, 27, 8, 15, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime[] expected = new ZonedDateTime[]{
            ZonedDateTime.of(2016, 12, 27, 16, 0, 0, 0, ZoneId.of("UTC")),
            ZonedDateTime.of(2016, 12, 27, 18, 0, 0, 0, ZoneId.of("UTC")),
            ZonedDateTime.of(2016, 12, 28, 16, 0, 0, 0, ZoneId.of("UTC")),
            ZonedDateTime.of(2016, 12, 28, 18, 0, 0, 0, ZoneId.of("UTC")),
            ZonedDateTime.of(2016, 12, 29, 16, 0, 0, 0, ZoneId.of("UTC")),
        };

        List<ZonedDateTime> actualList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ZonedDateTime next = executionTime.nextExecution(start).get();
            start = next;
            actualList.add(next);
        }
        Object[] actual = actualList.toArray();

        assertArrayEquals(expected, actual);
    }

    /**
     * Issue #153
     * https://github.com/jmrozanec/cron-utils/issues/153
     * Reported case: executionTime.nextExecution fails to find when current month does not have desired day
     */
    @Test
    public void mustJumpToNextMonthIfCurrentMonthDoesNotHaveDesiredDay() {
        CronParser parser = new CronParser( CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
        ExecutionTime executionTime = ExecutionTime.forCron( parser.parse( "0 0 8 31 * ?" ) );//8:00 on every 31th of Month
        ZonedDateTime start = ZonedDateTime.of(2017, 04, 10, 0, 0, 0, 0, ZoneId.systemDefault() );
        ZonedDateTime next = executionTime.nextExecution(start).get();
        ZonedDateTime expected = ZonedDateTime.of(2017, 05, 31, 8, 0, 0, 0, ZoneId.systemDefault() );
        assertEquals( expected, next );
    }

    /**
     * Issue #153
     * https://github.com/jmrozanec/cron-utils/issues/153
     * Reported case: executionTime.nextExecution fails to find when current month does not have desired day
     */
    @Test
    public void mustJumpToEndOfMonthIfCurrentMonthHasDesiredDay() {
        CronParser parser = new CronParser( CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
        ExecutionTime executionTime = ExecutionTime.forCron( parser.parse( "0 0 8 31 * ?" ) );//8:00 on every 31th of Month
        ZonedDateTime start = ZonedDateTime.of( 2017, 01, 10, 0, 0, 0, 0, ZoneId.systemDefault() );
        ZonedDateTime next = executionTime.nextExecution(start).get();
        ZonedDateTime expected = ZonedDateTime.of( 2017, 01, 31, 8, 0, 0, 0, ZoneId.systemDefault() );
        assertEquals( expected, next );
    }

    private Duration getMinimumInterval(String quartzPattern) {
        ExecutionTime et = ExecutionTime.forCron(parser.parse(quartzPattern));
        ZonedDateTime coolDay = ZonedDateTime.of(2016, 1, 1, 0, 0, 0, 0, UTC);
        // Find next execution time #1
        ZonedDateTime t1 = et.nextExecution(coolDay).get();
        // Find next execution time #2 right after #1, the interval between them is minimum
        return et.timeToNextExecution(t1).get();
    }

    private ZonedDateTime truncateToSeconds(ZonedDateTime dateTime){
        return dateTime.truncatedTo(ChronoUnit.SECONDS);
    }

    private void assertExpectedNextExecution(String cronExpression, ZonedDateTime lastRun,
                                             ZonedDateTime expectedNextRun) {

        String testCaseDescription = "cron expression '" + cronExpression + "' with zdt " + lastRun;
        System.out.println("TESTING: " + testCaseDescription);
        CronDefinition cronDef = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
        CronParser parser = new CronParser(cronDef);
        Cron cron = parser.parse(cronExpression);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        try {
            ZonedDateTime nextRun = executionTime.nextExecution(lastRun).get();
            assertEquals(testCaseDescription, expectedNextRun, nextRun);
        }
        catch(DateTimeException e) {
            fail("Issue #110: " + testCaseDescription + " led to " + e);
        }
    }
}
