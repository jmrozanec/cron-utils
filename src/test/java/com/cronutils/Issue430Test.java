package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class Issue430Test {
    @Test
    public void test() {
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse("0 0 12 30 6 ? 2020/10");
        ExecutionTime execution = ExecutionTime.forCron(cron);
        ZonedDateTime dateTime = ZonedDateTime.of(2020, 6, 30, 12, 0, 0, 0, ZoneOffset.UTC);
        // The cron starts from 2020, so no last execution date should be returned.
        Assert.assertNull(execution.lastExecution(dateTime).orElse(null));
        Assert.assertEquals(dateTime.plusYears(10), execution.nextExecution(dateTime).orElse(null));
    }
}
