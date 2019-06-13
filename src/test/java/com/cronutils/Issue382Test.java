package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import java.time.ZonedDateTime;

import static java.time.Duration.ofMillis;
import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertEquals;

public class Issue382Test {

    @Test
    public void testLastExecutionWithMillis() {
        CronParser cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        String cronString = "0 0 * * WED";
        Cron cron = cronParser.parse(cronString);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        ZonedDateTime date = ZonedDateTime.of(2019, 6, 12, 0, 0, 0, 0, UTC);
        ZonedDateTime lastExecution = executionTime.lastExecution(date.plus(ofMillis(300))).get();
        assertEquals(date, lastExecution);
    }
}
