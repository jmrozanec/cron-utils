package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Issue413Test {

    @Test
    public void testFridayToSaturdayQuartz() {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        final Cron quartzCron = parser.parse("* * * */12 *");
        try {
            quartzCron.validate();
            // expected to fail.
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Failed to parse '* * * */12 *'. Period 12 not in range (1, 12]", expected.getMessage());
        }
    }
}
