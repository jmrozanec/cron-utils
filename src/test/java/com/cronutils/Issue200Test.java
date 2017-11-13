package com.cronutils;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import static com.cronutils.model.CronType.QUARTZ;
import static org.junit.Assert.assertTrue;

/**
 * Created by johnpatrick.manalo on 6/19/17.
 */
public class Issue200Test {

    @Test
    public void testMustMatchCronEvenIfNanoSecondsVaries() {
        CronDefinition cronDefinition =
                CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);

        CronParser parser = new CronParser(cronDefinition);
        Cron quartzCron = parser.parse("00 00 10 * * ?");

        quartzCron.validate();

        // NOTE: Off by 3 nano seconds
        ZonedDateTime zdt = ZonedDateTime.of(1999, 07, 18, 10, 00, 00, 03, ZoneId.systemDefault());

        // Must be true
        assertTrue("Nano seconds must not affect matching of Cron Expressions", ExecutionTime.forCron(quartzCron).isMatch(zdt));
    }

    // Nano second-perfect (passes, no surprises here)
    @Test
    public void testMatchExact() {
        CronDefinition cronDefinition =
                CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);

        CronParser parser = new CronParser(cronDefinition);
        Cron quartzCron = parser.parse("00 00 10 * * ?");

        quartzCron.validate();

        ZonedDateTime zdt = ZonedDateTime.of(1999, 07, 18, 10, 00, 00, 00, ZoneId.systemDefault());

        assertTrue("Nano seconds must not affect matching of Cron Expressions", ExecutionTime.forCron(quartzCron).isMatch(zdt));
    }
}
