package com.cronutils.model.time;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;

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
public class ExecutionTimeQuartzIntegrationTest {
    private CronParser quartzCronParser;
    private static final String EVERY_SECOND = "* * * * * * *";

    @Before
    public void setUp(){
        quartzCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    @Test
    public void testForCron() throws Exception {
        assertEquals(ExecutionTime.class, ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND)).getClass());
    }

    @Test
    public void testNextExecutionEverySecond() throws Exception {
        DateTime now = truncateToSeconds(DateTime.now());
        DateTime expected = truncateToSeconds(now.plusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(expected, executionTime.nextExecution(now));
    }

    @Test
    public void testTimeToNextExecution() throws Exception {
        DateTime now = truncateToSeconds(DateTime.now());
        DateTime expected = truncateToSeconds(now.plusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(new Interval(now, expected).toDuration(), executionTime.timeToNextExecution(now));
    }

    @Test
    public void testLastExecution() throws Exception {
        DateTime now = truncateToSeconds(DateTime.now());
        DateTime expected = truncateToSeconds(now.minusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(expected, executionTime.lastExecution(now));
    }

    @Test
    public void testTimeFromLastExecution() throws Exception {
        DateTime now = truncateToSeconds(DateTime.now());
        DateTime expected = truncateToSeconds(now.minusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(new Interval(expected, now).toDuration(), executionTime.timeFromLastExecution(now));
    }

    /**
     * Test for issue #9
     * https://github.com/jmrozanec/cron-utils/issues/9
     * Reported case: If you write a cron expression that contains a month or day of week, nextExection() ignores it.
     * Expected: should not ignore month or day of week field
     */
    @Test
    public void testDoesNotIgnoreMonthOrDayOfWeek(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser cronParser = new CronParser(cronDefinition);
        //seconds, minutes, hours, dayOfMonth, month, dayOfWeek
        ExecutionTime executionTime = ExecutionTime.forCron(cronParser.parse("0 11 11 11 11 ?"));
        DateTime now = new DateTime(2015, 4, 15, 0, 0, 0);
        DateTime whenToExecuteNext = executionTime.nextExecution(now);
        assertEquals(2015, whenToExecuteNext.getYear());
        assertEquals(11, whenToExecuteNext.getMonthOfYear());
        assertEquals(11, whenToExecuteNext.getDayOfMonth());
        assertEquals(11, whenToExecuteNext.getHourOfDay());
        assertEquals(11, whenToExecuteNext.getMinuteOfHour());
        assertEquals(0, whenToExecuteNext.getSecondOfMinute());
    }

    /**
     * Test for issue #18
     * @throws Exception
     */
    @Test
    public void testHourlyIntervalTimeFromLastExecution() throws Exception {
        DateTime now = DateTime.now();
        DateTime previousHour = now.minusHours(1);
        String quartzCronExpression = String.format("0 0 %s * * ?", previousHour.getHourOfDay());
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(quartzCronExpression));

        assertTrue(executionTime.timeFromLastExecution(now).getStandardMinutes() <= 120);
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
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(expression));

        DateTime now = new DateTime().withTime(23, 59, 59, 0);
        DateTime expected = now.plusSeconds(1);
        DateTime nextExecution = executionTime.nextExecution(now);

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
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(expression));

        DateTime now = new DateTime(2015, 1, 31, 23, 59, 59, 0);
        DateTime expected = now.plusSeconds(1);
        DateTime nextExecution = executionTime.nextExecution(now);

        assertEquals(expected, nextExecution);
    }

    /**
     * Issue #24: next execution not properly calculated
     */
    @Test
    public void testTimeShiftingProperlyDone() throws Exception {
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse("0 0/10 22 * * *"));
        DateTime nextExecution =
                executionTime.nextExecution(
                        DateTime.now()
                                .withHourOfDay(15)
                                .withMinuteOfHour(27)
                );
        assertEquals(22, nextExecution.getHourOfDay());
        assertEquals(0, nextExecution.getMinuteOfHour());
    }

    /**
     * Issue #27: execution time properly calculated
     */
    @Test
    public void testMonthRangeExecutionTime(){
        ExecutionTime.forCron(quartzCronParser.parse("0 0 0 * JUL-AUG * *"));
    }

    /**
     * Issue #30: execution time properly calculated
     */
    @Test
    public void testSaturdayExecutionTime(){
        DateTime now = DateTime.now();
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0 3 ? * 6"));
        DateTime last = executionTime.lastExecution(now);
        DateTime next = executionTime.nextExecution(now);
        assertNotEquals(last, next);
    }

    /**
     * Issue: execution time properly calculated
     */
    @Test
    public void testWeekdayExecutionTime(){
        DateTime now = DateTime.now();
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0 3 ? * *"));
        DateTime last = executionTime.lastExecution(now);
        DateTime next = executionTime.nextExecution(now);
        assertNotEquals(last, next);
    }

    /**
     * Issue #64: Incorrect next execution time for ranges
     */
    @Test
    public void testExecutionTimeForRanges(){
        final CronParser quartzParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzParser.parse("* 10-20 * * * * 2099"));
        DateTime scanTime = DateTime.parse("2016-02-29T11:00:00.000-06:00");
        DateTime nextTime = executionTime.nextExecution(scanTime);
        assertNotNull(nextTime);
        assertEquals(10, nextTime.getMinuteOfHour());
    }

    /**
     * Issue #65: Incorrect last execution time for fixed month
     */
    @Test
    public void testLastExecutionTimeForFixedMonth(){
        final CronParser quartzParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzParser.parse("0 30 12 1 9 * 2010"));
        DateTime scanTime = DateTime.parse("2016-01-08T11:00:00.000-06:00");
        DateTime lastTime = executionTime.lastExecution(scanTime);
        assertNotNull(lastTime);
        assertEquals(9, lastTime.getMonthOfYear());
    }

    /**
     * Issue #66: Incorrect Day Of Week processing for Quartz when Month or Year isn't '*'.
     */
    @Test
    public void testNextExecutionRightDoWForFixedMonth(){
        //cron format: s,m,H,DoM,M,DoW,Y
        final CronType cronType = CronType.QUARTZ;
        final CronParser quartzParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(cronType));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzParser.parse("0 * * ? 5 1 *"));
        DateTime scanTime = DateTime.parse("2016-03-06T20:17:28.000-03:00");
        DateTime nextTime = executionTime.nextExecution(scanTime);
        assertNotNull(nextTime);
        assertEquals(DateTimeConstants.SUNDAY, nextTime.getDayOfWeek());
    }

    /**
     * Issue #66: Incorrect Day Of Week processing for Quartz when Month or Year isn't '*'.
     */
    @Test
    public void testNextExecutionRightDoWForFixedYear(){
        //cron format: s,m,H,DoM,M,DoW,Y
        final CronType cronType = CronType.QUARTZ;
        final CronParser quartzParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(cronType));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzParser.parse("0 * * ? * 1 2099"));
        DateTime scanTime = DateTime.parse("2016-03-06T20:17:28.000-03:00");
        DateTime nextTime = executionTime.nextExecution(scanTime);
        assertNotNull(nextTime);
        assertEquals(DateTimeConstants.SUNDAY, nextTime.getDayOfWeek());
    }

    /**
     * Issue #70: Illegal question mark value on cron pattern assumed valid.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalQuestionMarkValue(){
        final CronParser quartzParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        ExecutionTime.forCron(quartzParser.parse("0 0 12 1W ? *"));//s,m,H,DoM,M,DoW
    }

    private DateTime truncateToSeconds(DateTime dateTime){
        return new DateTime(
                dateTime.getYear(),
                dateTime.getMonthOfYear(),
                dateTime.getDayOfMonth(),
                dateTime.getHourOfDay(),
                dateTime.getMinuteOfHour(),
                dateTime.getSecondOfMinute()
        );
    }
}