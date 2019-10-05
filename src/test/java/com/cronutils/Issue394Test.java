package com.cronutils;

import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.cronutils.model.CronType.SPRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Issue394Test {

    @Test
    public void testEveryMondayAt0900hours() {
        String cron = "0 0 9 * * MON";

        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(SPRING);
        CronParser parser = new CronParser(cronDefinition);
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(cron));
        ZonedDateTime now = ZonedDateTime.now();
        Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
        assertTrue(nextExecution.isPresent());
        // Should be the next Monday at 0900 hours
        assertEquals(DayOfWeek.MONDAY, nextExecution.get().getDayOfWeek());
        assertEquals(9, nextExecution.get().getHour());
        // The next execution after that should also be a Monday at 0900 hours, the following week
        ZonedDateTime nextExpectedExecution = nextExecution.get().plusWeeks(1);
        nextExecution = executionTime.nextExecution(nextExecution.get());
        assertTrue(nextExecution.isPresent());
        assertEquals(DayOfWeek.MONDAY, nextExecution.get().getDayOfWeek());
        assertEquals(9, nextExecution.get().getHour());
        assertEquals(nextExpectedExecution, nextExecution.get());
    }
}
