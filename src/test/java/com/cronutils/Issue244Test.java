package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Test;
import org.threeten.bp.ZonedDateTime;

import static org.junit.Assert.assertEquals;

public class Issue244Test {

    private CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
            .withMinutes().and()
            .withHours().and()
            .withDayOfMonth()
            .supportsHash().supportsL().supportsW().and()
            .withMonth().and()
            .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0) //we support non-standard non-zero-based numbers!
            .supportsHash().supportsL().supportsW().and()
            .withYear().optional().and()
            .matchDayOfWeekAndDayOfMonth() // the regular UNIX cron definition permits matching either DoW or DoM
            .instance();

    @Test
    public void testLastDayOfMonth() {
        CronParser parser = new CronParser(cronDefinition);

        // This is 9am on last day of the Month
        Cron myCron = parser.parse("* 9 * * 1L");
        ZonedDateTime time = ZonedDateTime.parse("2017-10-03T14:46:01.166-07:00");
        assertEquals(ZonedDateTime.parse("2017-10-31T09:00-07:00"), ExecutionTime.forCron(myCron).nextExecution(time).get());
    }
}
