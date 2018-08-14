package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.model.time.generator.NoSuchValueException;
import com.cronutils.parser.CronParser;
import org.junit.Ignore;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Issue332Test {

    @Test
    public void testIsMatchDailightSavingsChange_loop() {
        CronParser cronparser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        ZonedDateTime date = ZonedDateTime.of(2018, 8, 12, 3, 0, 0, 0, ZoneId.of("America/Santiago"));
        Cron cron = cronparser.parse("0 6 * * *");
        ExecutionTime exectime = ExecutionTime.forCron(cron);
        exectime.isMatch(date);
    }
}
