package com.cronutils;

import java.time.ZonedDateTime;

import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import static com.cronutils.model.definition.CronDefinitionBuilder.defineCron;

/**
 * @author minidmnv
 */
public class Issue218Test {
    /**
     * Issue #218 - isMatch() method should return true/false rather then throwing exception
     */

    private final String CRON_EXPRESSION = "0-59 7-16 MON-FRI";

    @Test
    public void testCronDefinitionExecutionTimeGenerator() {
        CronDefinition cronDefinition = defineCron().withMinutes().and()
                .withHours().and()
                .withDayOfWeek()
                .optional()
                .and()
                .instance();
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(CRON_EXPRESSION);
        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        executionTime.isMatch(ZonedDateTime.now());
    }
}
