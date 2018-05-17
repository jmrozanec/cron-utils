package com.cronutils;

import java.time.ZonedDateTime;

import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

public class Issue329Test {
    @Test
    public void infiniteLoop() {
        Cron cron = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX))
                .parse("0 0 30 2 *");//m H DoM M DoW
        ExecutionTime.forCron(cron).nextExecution(ZonedDateTime.now());
    }
}
