package com.cronutils.model.time;

import android.support.test.runner.AndroidJUnit4;

import com.cronutils.BaseAndroidTest;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.threeten.bp.ZoneOffset.UTC;

@RunWith(AndroidJUnit4.class)
public class ExecutionTimeCustomDefinitionIntegrationTest extends BaseAndroidTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

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

        ZonedDateTime startDateTime = ZonedDateTime.of(2015, 8, 28, 12, 5, 44, 0, UTC);
        ZonedDateTime expectedDateTime = ZonedDateTime.of(2015, 8, 28, 12, 6, 0, 0, UTC);

        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        ZonedDateTime nextExecutionDateTime = executionTime.nextExecution(startDateTime).get();
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


        ZonedDateTime startDateTime = ZonedDateTime.of(2015, 8, 28, 12, 5, 14, 0, UTC);
        ZonedDateTime expectedDateTime = ZonedDateTime.of(2015, 8, 28, 12, 5, 30, 0, UTC);

        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        ZonedDateTime nextExecutionDateTime = executionTime.nextExecution(startDateTime).get();
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
        ZonedDateTime startDateTime = ZonedDateTime.parse("2015-08-28T12:05:14.000-03:00");

        assertTrue(ZonedDateTime.parse("2015-08-28T14:00:00.000-03:00").compareTo(ExecutionTime.forCron(cron).nextExecution(startDateTime).get()) == 0);
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
        ZonedDateTime startDateTime = ZonedDateTime.parse("2015-08-28T12:05:14.000-03:00");

        assertTrue(ZonedDateTime.parse("2015-08-28T14:00:00.000-03:00").compareTo(ExecutionTime.forCron(cron).nextExecution(startDateTime).get()) == 0);
    }

    /**
     * Test for issue #57
     * https://github.com/jmrozanec/cron-utils/issues/57
     * Reported case: BetweenDayOfWeekValueGenerator does not work for the first day of a month in some cases.
     * Expected: first day of month should be returned ok
     */
    @Test
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
        ZonedDateTime sameDayBeforeEventStartDateTime = ZonedDateTime.parse("1970-01-01T00:00:00.000-03:00");
        assertEquals(1, ExecutionTime.forCron(cron).nextExecution(sameDayBeforeEventStartDateTime).get().getDayOfMonth());
        ZonedDateTime sameDayAfterEventStartDateTime = ZonedDateTime.parse("1970-01-01T12:00:00.000-03:00");
        assertEquals(2, ExecutionTime.forCron(cron).nextExecution(sameDayAfterEventStartDateTime).get().getDayOfMonth());
    }

    /**
     * Issue #136: Bug exposed at PR #136
     * https://github.com/jmrozanec/cron-utils/pull/136
     * Reported case: when executing isMatch for a given range of dates,
     * if date is invalid, we get an exception, not a boolean as response.
     */
    @Test
    public void testMatchWorksAsExpectedForCustomCronsWhenPreviousOrNextOccurrenceIsMissing() {
        CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withDayOfMonth()
                .supportsL().supportsW()
                .and()
                .withMonth().and()
                .withYear()
                .and().instance();

        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse("05 05 2004");
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        ZonedDateTime start = ZonedDateTime.of(2004, 5, 5, 23, 55, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime end = ZonedDateTime.of(2004, 5, 6, 1, 0, 0, 0, ZoneId.of("UTC"));
        while(start.compareTo(end)<0){
            assertTrue(executionTime.isMatch(start)==(start.getDayOfMonth()==5));
            start = start.plusMinutes(1);
        }
    }
}
