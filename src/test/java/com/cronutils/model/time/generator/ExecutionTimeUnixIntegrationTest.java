package com.cronutils.model.time.generator;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.joda.time.DateTime;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ExecutionTimeUnixIntegrationTest {

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
}
