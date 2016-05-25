package com.cronutils.model.time.generator;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExecutionTimeUnixIntegrationTest {

    @Test
    public void testIsMatchForUnix01(){
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        String crontab = "* * * * *";//m,h,dom,M,dow
        Cron cron = parser.parse(crontab);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        DateTime scanTime = DateTime.parse("2016-02-29T11:00:00.000-06:00");
        assertTrue(executionTime.isMatch(scanTime));
    }

    @Test
    public void testIsMatchForUnix02(){
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        String crontab = "0 * * * 1-5";//m,h,dom,M,dow
        Cron cron = parser.parse(crontab);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        DateTime scanTime = DateTime.parse("2016-03-04T11:00:00.000-06:00");
        assertTrue(executionTime.isMatch(scanTime));
    }

    /**
     * Issue #37: for pattern "every 10 minutes", nextExecution returns a date from past.
     */
    @Test
    public void testEveryTenMinutesNextExecution(){
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("*/10 * * * *"));
        DateTime time = DateTime.parse("2015-09-05T13:43:00.000-07:00");
        assertEquals(DateTime.parse("2015-09-05T13:50:00.000-07:00"), executionTime.nextExecution(time));
    }

    /**
     * Issue #38: every 2 min schedule doesn't roll over to next hour
     */
    @Test
    public void testEveryTwoMinRollsOverHour(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        Cron cron = new CronParser(cronDefinition).parse("*/2 * * * *");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        DateTime time = DateTime.parse("2015-09-05T13:56:00.000-07:00");
        DateTime next = executionTime.nextExecution(time);
        DateTime shouldBeInNextHour = executionTime.nextExecution(next);
        assertEquals(next.plusMinutes(2), shouldBeInNextHour);
    }

    /**
     * Issue #41: for everything other than a dayOfWeek value == 1, nextExecution and lastExecution do not return correct results
     */
    @Test
    public void testEveryTuesdayAtThirdHourOfDayNextExecution(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron myCron = parser.parse("0 3 * * 3");
        DateTime time = DateTime.parse("2015-09-17T00:00:00.000-07:00");
        assertEquals(DateTime.parse("2015-09-23T03:00:00.000-07:00"), ExecutionTime.forCron(myCron).nextExecution(time));
    }

    /**
     * Issue #41: for everything other than a dayOfWeek value == 1, nextExecution and lastExecution do not return correct results
     */
    @Test
    public void testEveryTuesdayAtThirdHourOfDayLastExecution(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron myCron = parser.parse("0 3 * * 3");
        DateTime time = DateTime.parse("2015-09-17T00:00:00.000-07:00");
        assertEquals(DateTime.parse("2015-09-16T03:00:00.000-07:00"), ExecutionTime.forCron(myCron).lastExecution(time));
    }

    /**
     * Issue #45: last execution does not match expected date. Result is not in same timezone as reference date.
     */
    @Test
    public void testMondayWeekdayLastExecution(){
        String crontab = "* * * * 1";
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(crontab);
        DateTime date = DateTime.parse("2015-10-13T17:26:54.468-07:00");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        assertEquals(DateTime.parse("2015-10-12T23:59:00.000-07:00"), executionTime.lastExecution(date));
    }

    /**
     * Issue #45: next execution does not match expected date. Result is not in same timezone as reference date.
     */
    @Test
    public void testMondayWeekdayNextExecution(){
        String crontab = "* * * * 1";
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(crontab);
        DateTime date = DateTime.parse("2015-10-13T17:26:54.468-07:00");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        assertEquals(DateTime.parse("2015-10-19T00:00:00.000-07:00"), executionTime.nextExecution(date));
    }

    /**
     * Issue #50: last execution does not match expected date when cron specifies day of week and last execution is in previous month.
     */
    @Test
    public void testLastExecutionDaysOfWeekOverMonthBoundary(){
        String crontab = "0 11 * * 1";
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(crontab);
        DateTime date = DateTime.parse("2015-11-02T00:10:00.000");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        assertEquals(DateTime.parse("2015-10-26T11:00:00.000"), executionTime.lastExecution(date));
    }

    /**
      * Issue #52: "And" doesn't work for day of the week
      * 1,2 should be Monday and Tuesday, but instead it is treated as 1st/2nd of month.
      */
    @Test
    public void testWeekdayAndLastExecution() {
        String crontab = "* * * * 1,2";
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(crontab);
        DateTime date = DateTime.parse("2015-11-10T17:01:00Z");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        assertEquals(DateTime.parse("2015-11-10T17:00:00Z"), executionTime.lastExecution(date));
    }

    /**
     * Isue #52: Additional test to ensure after fix that "And" and "Between" can both be used
     * 1,2-3 should be Monday, Tuesday and Wednesday.
     */
    @Test
    public void testWeekdayAndWithMixOfOnAndBetweenLastExecution() {
        String crontab = "* * * * 1,2-3";
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(crontab);
        DateTime date = DateTime.parse("2015-11-10T17:01:00Z");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        assertEquals(DateTime.parse("2015-11-10T17:00:00Z"), executionTime.lastExecution(date));
    }

    /**
     * Issue #59: Incorrect next execution time for "month" and "day of week"
     * Considers Month in range 0-11 instead of 1-12
     */
    @Test
    public void testCorrectMonthScaleForNextExecution1(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        String crontab = "* * */3 */4 */5";//m,h,dom,M,dow
        Cron cron = parser.parse(crontab);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        DateTime scanTime = DateTime.parse("2015-12-10T16:32:56.586-08:00");
        DateTime nextExecutionTime = executionTime.nextExecution(scanTime);
        //DoW: 0-6 -> 0, 5 (sunday, friday)
        //DoM: 1-31 -> 1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31
        //M: 1-12 -> 1, 5, 9
        assertEquals(DateTime.parse("2016-01-01T00:00:00.000-08:00"), nextExecutionTime);
    }

    /**
     * Issue #59: Incorrect next execution time for "day of month" in "time" situation
     * dom "* / 4" should mean 1, 5, 9, 13, 17th... of month instead of 4, 8, 12, 16th...
     */
    @Test
    public void testCorrectMonthScaleForNextExecution2(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        String crontab = "* * */4 * *";//m,h,dom,M,dow
        Cron cron = parser.parse(crontab);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        DateTime scanTime = DateTime.parse("2015-12-10T16:32:56.586-08:00");
        DateTime nextExecutionTime = executionTime.nextExecution(scanTime);
        assertEquals(DateTime.parse("2015-12-13T00:00:00.000-08:00"), nextExecutionTime);
    }

    /**
     * Issue #59: Incorrect next execution time for "month" and "day of week"
     * Considers bad DoW
     */
    @Test
    public void testCorrectNextExecutionDoW(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        String crontab = "* * * * */4";//m,h,dom,M,dow
        //DoW: 0-6 -> 0, 4 (sunday, thursday)
        Cron cron = parser.parse(crontab);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        DateTime scanTime = DateTime.parse("2016-01-28T16:32:56.586-08:00");
        DateTime nextExecutionTime = executionTime.nextExecution(scanTime);
        assertEquals(DateTime.parse("2016-02-04T00:00:00.000-08:00"), nextExecutionTime);
    }

    /**
     * Issue #69: Getting next execution fails on leap-year when using day-of-week
     */
    @Test
    public void testCorrectNextExecutionDoWForLeapYear(){
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        String crontab = "0 * * * 1-5";//m,h,dom,M,dow
        //DoW: 0-6 -> 1, 2, 3, 4, 5 -> in this year:
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(crontab));
        DateTime scanTime = DateTime.parse("2016-02-29T11:00:00.000-06:00");
        DateTime nextExecutionTime = executionTime.nextExecution(scanTime);
        assertEquals(DateTime.parse("2016-02-29T12:00:00.000-06:00"), nextExecutionTime);
    }

    /**
     * Issue #61: nextExecution over daylight savings is wrong
     */
    @Test
    public void testNextExecutionDaylightSaving() {
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 17 * * *"));// daily at 17:00
        // Daylight savings for New York 2016 is Mar 13 at 2am
        DateTime last = new DateTime(2016, 3, 12, 17, 0, DateTimeZone.forID("America/New_York"));
        DateTime next = executionTime.nextExecution(last);
        long millis = next.getMillis() - last.getMillis();
        assertEquals(23, (millis / 3600000));
        assertEquals(last.getZone(), next.getZone());
    }

    /**
     * Issue #61: lastExecution over daylight savings is wrong
     */
    @Test
    public void testLastExecutionDaylightSaving(){
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 17 * * *"));// daily at 17:00
        // Daylight savings for New York 2016 is Mar 13 at 2am
        DateTime now = new DateTime(2016, 3, 12, 17, 0, DateTimeZone.forID("America/Phoenix"));
        DateTime last = executionTime.lastExecution(now);
        long millis = now.getMillis() - last.getMillis();
        assertEquals(24, (millis / 3600000));
        assertEquals(now.getZone(), last.getZone());
    }

    /**
     * Issue #79: Next execution skipping valid date
     */
    public void testNextExecution2014() {
        String crontab = "0 8 * * 1";//m,h,dom,m,dow ; every monday at 8AM
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(crontab);
        DateTime date = DateTime.parse("2014-11-30T00:00:00Z");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        assertEquals(DateTime.parse("2014-12-01T08:00:00Z"), executionTime.nextExecution(date));
    }
}
