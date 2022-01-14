package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class Issue444Test {

    @Test
    public void testCase1() {
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

        Cron cron = parser.parse("0 40 08 ? * *");

        Clock clock = Clock.fixed(Instant.ofEpochMilli(1601426291898L), ZoneId.of("UTC"));
        ZonedDateTime startDate = ZonedDateTime.now(clock);
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);

        Optional<ZonedDateTime> date = executionTime.nextExecution(startDate);
        assertEquals(ZonedDateTime.of(2020, 9, 30, 8, 40, 0, 0, ZoneId.of("UTC")), date.get());
    }
}
