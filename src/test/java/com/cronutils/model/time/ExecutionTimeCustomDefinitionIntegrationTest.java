package com.cronutils.model.time;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExecutionTimeCustomDefinitionIntegrationTest {

    @Test
    public void testCronExpressionAfterHalf() {
        CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).and()
                .instance();

        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse("*/30 * * * * *");

        DateTime startDateTime = new DateTime(2015, 8, 28, 12, 5, 44, 0);
        DateTime expectedDateTime = new DateTime(2015, 8, 28, 12, 6, 0, 0);

        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        DateTime nextExecutionDateTime = executionTime.nextExecution(startDateTime);
        assertEquals(expectedDateTime, nextExecutionDateTime);
    }

    @Test
    public void testCronExpressionBeforeHalf() {

        CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).and()
                .instance();

        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse("0/30 * * * * *");

        MutableDateTime mutableDateTime = new MutableDateTime();
        mutableDateTime.setDateTime(2015, 8, 28, 12, 5, 14, 0);

        DateTime startDateTime = mutableDateTime.toDateTime();

        mutableDateTime = new MutableDateTime();
        mutableDateTime.setDateTime(2015, 8, 28, 12, 5, 30, 0);

        DateTime expectedDateTime = mutableDateTime.toDateTime();

        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        DateTime nextExecutionDateTime = executionTime.nextExecution(startDateTime);
        assertEquals(expectedDateTime, nextExecutionDateTime);
    }
}
