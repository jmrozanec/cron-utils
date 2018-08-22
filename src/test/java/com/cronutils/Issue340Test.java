package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static com.cronutils.model.CronType.QUARTZ;
@Ignore
public class Issue340Test {

    @Test
    // This test will enter a (bounded) infinite loop searching for last execution time
    public void testGetTimeFromLastExecutionForScheduleWithDayOfWeekRangeCrossingLastDayOfWeekBoundary() {
        String schedule = "0 0 * ? * MON-SUN *";
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        Cron quartzCron = parser.parse(schedule);

        ZonedDateTime time = ZonedDateTime.now();
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCron);

        // Time from last execution to now
        Duration timeFromLastExecution = executionTime.timeFromLastExecution(time).get();
        Assert.assertTrue(timeFromLastExecution.toMinutes() <= 60 );
    }

}
