package com.cronutils.model.time;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExecutionTimeIntegrationTest {
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
        DateTime now = DateTime.now();
        DateTime expected = truncateToSeconds(now.plusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(expected, executionTime.nextExecution(now));
    }

    @Test
    public void testTimeToNextExecution() throws Exception {
        DateTime now = DateTime.now();
        DateTime expected = truncateToSeconds(now.plusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(new Interval(now, expected).toDuration(), executionTime.timeToNextExecution(now));
    }

    @Test
    public void testLastExecution() throws Exception {
        DateTime now = DateTime.now();
        DateTime expected = truncateToSeconds(now.minusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(expected, executionTime.lastExecution(now));
    }

    @Test
    public void testTimeFromLastExecution() throws Exception {
        DateTime now = DateTime.now();
        DateTime expected = truncateToSeconds(now.minusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(new Interval(expected, now).toDuration(), executionTime.timeFromLastExecution(now));
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