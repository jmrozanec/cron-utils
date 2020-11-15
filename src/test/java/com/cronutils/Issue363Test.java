package com.cronutils;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Issue363Test {

    @Test
    public void everySecondOfMinute01Test() {
        // every second of the first minute of every hour/day/year
        String cronExpression = "* 1 * * * ?";
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(cronExpression));

        ZonedDateTime now = ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);

        assertTrue(nextExecution.isPresent());
        // First match should be 2019-01-01T00:01:00Z
        ZonedDateTime expectedTime = ZonedDateTime.of(2019, 1, 1, 0, 1, 0, 0, ZoneOffset.UTC);
        assertEquals(expectedTime, nextExecution.get());

        // Should also match the next 59 seconds
        for (int i = 1; i <= 59; i++) {
            nextExecution = executionTime.nextExecution(nextExecution.get());
            assertTrue(nextExecution.isPresent());
            assertEquals(expectedTime.plusSeconds(i), nextExecution.get());
        }

        // After the every second of 00:01, it the next execution should be at 01:01:00Z
        nextExecution = executionTime.nextExecution(ZonedDateTime.of(2019, 1, 1, 0, 1, 59, 0, ZoneOffset.UTC));
        assertTrue(nextExecution.isPresent());
        expectedTime = ZonedDateTime.of(2019, 1, 1, 1, 1, 0, 0, ZoneOffset.UTC);
        assertEquals(expectedTime, nextExecution.get());
    }

    @Test
    public void everyMinute01Test() {
        // every minute 1 of every hour/day/year
        String cronExpression = "0 1 * * * ?";
        ZonedDateTime now = ZonedDateTime.of(2019, 1, 1, 0, 1, 0, 0, ZoneOffset.UTC);
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

        Optional<ZonedDateTime> nextExecution = ExecutionTime.forCron(parser.parse(cronExpression)).nextExecution(now);

        assertTrue(nextExecution.isPresent());

        ZonedDateTime expectedTime = ZonedDateTime.of(2019, 1, 1, 1, 1, 0, 0, ZoneOffset.UTC);
        assertEquals(expectedTime, nextExecution.get());
    }
}
