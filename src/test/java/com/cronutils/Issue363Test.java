package com.cronutils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Issue363Test {

    //@Test
    public void quartzNextExecutionTime() {
        String cronExpression = "* 1 * * * ?";
        ZonedDateTime now = ZonedDateTime.of(2019, 1, 1, 0, 1, 0, 0, ZoneOffset.UTC);
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

        Optional<ZonedDateTime> nextExecution = ExecutionTime.forCron(parser.parse(cronExpression)).nextExecution(now);

        assertTrue(nextExecution.isPresent());

        ZonedDateTime expectedTime = ZonedDateTime.of(2019, 1, 1, 1, 1, 0, 0, ZoneOffset.UTC);
        assertEquals(expectedTime, nextExecution.get());
    }
}
