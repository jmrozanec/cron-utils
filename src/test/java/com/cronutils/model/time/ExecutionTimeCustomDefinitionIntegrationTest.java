package com.cronutils.model.time;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    /**
     * Test for issue #38
     * https://github.com/jmrozanec/cron-utils/issues/38
     * Reported case: lastExecution and nextExecution do not work properly
     * Expected: should return expected date
     */
    @Test
    public void testCronExpressionEveryTwoHoursAsteriskSlash2() {
        CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).and()
                .instance();

        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse("0 0 */2 * * *");
        DateTime startDateTime = DateTime.parse("2015-08-28T12:05:14.000-03:00");

        assertTrue(DateTime.parse("2015-08-28T14:00:00.000-03:00").compareTo(ExecutionTime.forCron(cron).nextExecution(startDateTime)) == 0);
    }

    /**
     * Test for issue #38
     * https://github.com/jmrozanec/cron-utils/issues/38
     * Reported case: lastExecution and nextExecution do not work properly
     * Expected: should return expected date
     */
    @Test
    public void testCronExpressionEveryTwoHoursSlash2() {
        CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).and()
                .instance();

        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse("0 0 /2 * * *");
        DateTime startDateTime = DateTime.parse("2015-08-28T12:05:14.000-03:00");

        assertTrue(DateTime.parse("2015-08-28T14:00:00.000-03:00").compareTo(ExecutionTime.forCron(cron).nextExecution(startDateTime)) == 0);
    }

    /**
     * Test for issue #57
     * https://github.com/jmrozanec/cron-utils/issues/57
     * Reported case: BetweenDayOfWeekValueGenerator does not work for the first day of a month in some cases.
     * Expected: first day of month should be returned ok
     */
    public void testCronExpressionBetweenDayOfWeekValueGeneratorCorrectFirstDayOfMonth() {
        CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth()
                .supportsL().supportsW()
                .and()
                .withMonth().and()
                .withDayOfWeek()
                .withMondayDoWValue(1)
                .withValidRange(1, 7)
                .supportsHash().supportsL()
                .and()
                .withYear().and()
                .lastFieldOptional().instance();

        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse("30 3 * * MON-FRI");
        DateTime sameDayBeforeEventStartDateTime = DateTime.parse("1970-01-01T00:00:00.000-03:00");
        System.out.println(ExecutionTime.forCron(cron).nextExecution(sameDayBeforeEventStartDateTime));
        assertEquals(1, ExecutionTime.forCron(cron).nextExecution(sameDayBeforeEventStartDateTime).getDayOfMonth());
        DateTime sameDayAfterEventStartDateTime = DateTime.parse("1970-01-01T12:00:00.000-03:00");
        assertEquals(2, ExecutionTime.forCron(cron).nextExecution(sameDayAfterEventStartDateTime).getDayOfMonth());
    }
}
