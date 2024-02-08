package com.cronutils.model.time;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExecutionTimeNanosTest {
    @Test
    public void timeWithNanosMatchingSecondsTest() {
        CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .instance();
        Cron cron = new CronParser(cronDefinition)
                .parse("0,1,3/1 * * * *").validate();
        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        ZonedDateTime now0 = ZonedDateTime.of(2024, 2, 7, 13, 46, 0, 999999999, ZoneOffset.UTC);
        ZonedDateTime expected0 = ZonedDateTime.of(2024, 2, 7, 13, 46, 1, 0, ZoneOffset.UTC);
        Optional<ZonedDateTime> nextTime0 = executionTime.nextExecution(now0);
        assertTrue(nextTime0.isPresent());
        assertEquals(expected0, nextTime0.get());

        ZonedDateTime now1 = ZonedDateTime.of(2024, 2, 7, 13, 46, 1, 123456789, ZoneOffset.UTC);
        ZonedDateTime expected1 = ZonedDateTime.of(2024, 2, 7, 13, 46, 3, 0, ZoneOffset.UTC);
        Optional<ZonedDateTime> nextTime1 = executionTime.nextExecution(now1);
        assertTrue(nextTime1.isPresent());
        assertEquals(expected1, nextTime1.get());

        ZonedDateTime now2 = ZonedDateTime.of(2024, 2, 7, 13, 46, 2, 123456789, ZoneOffset.UTC);
        ZonedDateTime expected2 = ZonedDateTime.of(2024, 2, 7, 13, 46, 3, 0, ZoneOffset.UTC);
        Optional<ZonedDateTime> nextTime2 = executionTime.nextExecution(now2);
        assertTrue(nextTime2.isPresent());
        assertEquals(expected2, nextTime2.get());
    }

    @Test
    public void minutimeWithNanosMatchingMinutesTest() {
        CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .instance();
        Cron cron = new CronParser(cronDefinition)
                .parse("0,1,3/1 * * * ");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        ZonedDateTime now0 = ZonedDateTime.of(2024, 2, 7, 13, 0, 7, 123456789, ZoneOffset.UTC);
        ZonedDateTime expected0 = ZonedDateTime.of(2024, 2, 7, 13, 1, 0, 0, ZoneOffset.UTC);
        Optional<ZonedDateTime> nextTime0 = executionTime.nextExecution(now0);
        assertTrue(nextTime0.isPresent());
        assertEquals(expected0, nextTime0.get());

        ZonedDateTime now1 = ZonedDateTime.of(2024, 2, 7, 13, 1, 7, 123456789, ZoneOffset.UTC);
        ZonedDateTime expected1 = ZonedDateTime.of(2024, 2, 7, 13, 3, 0, 0, ZoneOffset.UTC);
        Optional<ZonedDateTime> nextTime1 = executionTime.nextExecution(now1);
        assertTrue(nextTime1.isPresent());
        assertEquals(expected1, nextTime1.get());

        ZonedDateTime now2 = ZonedDateTime.of(2024, 2, 7, 13, 2, 7, 123456789, ZoneOffset.UTC);
        ZonedDateTime expected2 = ZonedDateTime.of(2024, 2, 7, 13, 3, 0, 0, ZoneOffset.UTC);
        Optional<ZonedDateTime> nextTime2 = executionTime.nextExecution(now2);
        assertTrue(nextTime2.isPresent());
        assertEquals(expected2, nextTime2.get());
    }
}
