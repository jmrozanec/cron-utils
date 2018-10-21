package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

import static com.cronutils.model.CronType.QUARTZ;

public class Issue340Test {

    @Test
    public void testGetTimeFromLastExecutionForScheduleWithDayOfWeekRangeCrossingLastDayOfWeekBoundary() {
        String schedule = "0 0 * ? * MON-SUN *";
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        Cron quartzCron = parser.parse(schedule);

        ZonedDateTime time = ZonedDateTime.now();
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCron);

        // Time from last execution to now
        Optional<Duration> timeFromLastExecution = executionTime.timeFromLastExecution(time);
        Assert.assertTrue(timeFromLastExecution.isPresent());
        Assert.assertTrue(timeFromLastExecution.get().toMinutes() <= 60);
    }

    @Test
    public void testDayOfWeekRollover() {
        // Every Friday to Tuesday (Fri, Sat, Sun, Mon, Tue) at 5 AM
        String schedule = "0 0 5 ? * FRI-TUE *";
        // Java DayOfWeek is MON (1) to SUN (7)
        Set<Integer> validDaysOfWeek = Sets.newSet(1, 2, 5, 6, 7);
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        Cron quartzCron = parser.parse(schedule);

        ZonedDateTime time = ZonedDateTime.now();
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCron);

        // Check the next 100 execution times
        for (int i = 0; i < 100; i++) {
            Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(time);
            Assert.assertTrue(nextExecution.isPresent());
            time = nextExecution.get();
            int dayOfWeek = time.getDayOfWeek().getValue();
            Assert.assertTrue(validDaysOfWeek.contains(dayOfWeek));
            Assert.assertEquals(5, time.getHour());
            Assert.assertEquals(0, time.getMinute());
            Assert.assertEquals(0, time.getSecond());
        }
    }

}
