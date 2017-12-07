package com.cronutils.utils.descriptor;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.Ignore;
import org.junit.Test;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.descriptor.refactor.TimeDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertEquals;

//FIXME https://github.com/jmrozanec/cron-utils/issues/3

public class TestDescriptor {

    private final CronDescriptor descriptor = CronDescriptor.instance();

    // SoM MoH HoD D0M MoY  DoW Year
    // 3/4 5/6 7/8 9/2 10/2 ?   2017/2

    //Every 4 seconds starting at second 03,
    //every 6 minutes starting at minute :05,
    //every 8 hours starting at 07am,
    //every 2 days starting on the 9th,
    //every 2 months starting in October,
    //every 2 years starting in 2017
    @Ignore
    @Test
    public void testFull() {
        final Cron cron = getCron("3/4 5/6 7/8 9/2 10/2 ? 2017");
        assertEquals("every 4 seconds starting at second 03, every 6 minutes starting at minute 05, every 8 hours starting at 07am, "
                     + "every 2 days startint on the 9th, every 2 months starting in October, in 2017", descriptor.describe(cron));
    }

    private Cron getCron(final String quartzExpression) {
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        final CronParser parser = new CronParser(cronDefinition);
        return parser.parse(quartzExpression);
    }

    @Test
    public void testEverySecond() {
        final Cron cron = getCron("* * * * * ? *");
        final TimeDescriptor t = new TimeDescriptor(ResourceBundle.getBundle("CronUtilsI18N", Locale.UK));
        assertEquals("every second", t.describe(cron));
    }

    @Test
    public void testEvery1Second() {
        final Cron cron = getCron("*/1 * * * * ? *");
        final TimeDescriptor t = new TimeDescriptor(ResourceBundle.getBundle("CronUtilsI18N", Locale.UK));
        assertEquals("every second", t.describe(cron));
    }

    @Test
    public void testEvery2Seconds() {
        final Cron cron = getCron("*/2 * * * * ? *");
        final TimeDescriptor t = new TimeDescriptor(ResourceBundle.getBundle("CronUtilsI18N", Locale.UK));
        assertEquals("every 2 seconds", t.describe(cron));
    }

    @Test
    public void testEverySecondGerman() {
        final Cron cron = getCron("* * * * * ? *");
        final TimeDescriptor t = new TimeDescriptor(ResourceBundle.getBundle("CronUtilsI18N", Locale.GERMAN));
        assertEquals("jede sekunde", t.describe(cron));
    }

    @Test
    public void testEvery1SecondGerman() {
        final Cron cron = getCron("*/1 * * * * ? *");
        final TimeDescriptor t = new TimeDescriptor(ResourceBundle.getBundle("CronUtilsI18N", Locale.GERMAN));
        assertEquals("jede sekunde", t.describe(cron));
    }

    @Test
    public void testEvery2SecondsGerman() {
        final Cron cron = getCron("*/2 * * * * ? *");
        final TimeDescriptor t = new TimeDescriptor(ResourceBundle.getBundle("CronUtilsI18N", Locale.GERMAN));
        assertEquals("alle 2 sekunden", t.describe(cron));
    }

    @Test
    public void testEverySecondWithoutYear() {
        final Cron cron = getCron("* * * * * ?");
        assertEquals("every second", descriptor.describe(cron));
    }

    @Test
    public void testEverySecond2() {
        final Cron cron = getCron("* * * ? * * *");
        final Cron otherCron = getCron("* * * * * ? *");
        assertEquals("every second", descriptor.describe(cron));
        assertEquals(descriptor.describe(cron), descriptor.describe(otherCron));
    }

    @Test
    public void testEverySecond2WithoutYear() {
        final Cron cron = getCron("* * * ? * * *");
        final Cron otherCron = getCron("* * * * * ? *");
        assertEquals("every second", descriptor.describe(cron));
        assertEquals(descriptor.describe(cron), descriptor.describe(otherCron));
    }

    @Ignore
    @Test
    public void testAtSecondEveryMinute() {
        final Cron cron = getCron("1 * * * * ? *");
        final Cron otherCron = getCron("15 * * * * ? *");
        assertEquals("at second 01 of every minute", descriptor.describe(cron));
        assertEquals("at second 15 of every minute", descriptor.describe(otherCron));
    }

    @Ignore
    @Test
    public void testAtSecondEveryMinuteWithoutYear() {
        final Cron cron = getCron("1 * * * * ?");
        final Cron otherCron = getCron("15 * * * * ?");
        assertEquals("at second 01 of every minute", descriptor.describe(cron));
        assertEquals("at second 15 of every minute", descriptor.describe(otherCron));
    }

    @Ignore
    @Test
    public void testEverySecondDuringMinuteOfEveryHour() {
        final Cron cron = getCron("* 1 * * * ? *");
        final Cron otherCron = getCron("* 15 * * * ? *");
        assertEquals("every second during minute 01 of every hour", descriptor.describe(cron));
        assertEquals("every second during minute 15 of every hour", descriptor.describe(otherCron));
    }

    @Ignore
    @Test
    public void testEverySecondDuringMinuteOfEveryHourWithoutYear() {
        final Cron cron = getCron("* 1 * * * ?");
        final Cron otherCron = getCron("* 15 * * * ?");
        assertEquals("every second during minute 01 of every hour", descriptor.describe(cron));
        assertEquals("every second during minute 15 of every hour", descriptor.describe(otherCron));
    }

    @Ignore
    @Test
    public void testEverySecondOnTheDayEveryMonth() {
        final Cron first = getCron("* * * 1 * ? *");
        final Cron second = getCron("* * * 1 * ? *");
        final Cron third = getCron("* * * 1 * ? *");
        final Cron otherCron = getCron("* * * 15 * ? *");
        assertEquals("every second, on the 1st day, every month", descriptor.describe(first));
        assertEquals("every second, on the 2nd day, every month", descriptor.describe(second));
        assertEquals("every second, on the 3rd day, every month", descriptor.describe(third));
        assertEquals("every second, on the 15th day, every month", descriptor.describe(otherCron));
    }

    @Ignore
    @Test
    public void testEverySecondOnTheDayEveryMonthWithoutYear() {
        final Cron first = getCron("* * * 1 * ?");
        final Cron second = getCron("* * * 1 * ?");
        final Cron third = getCron("* * * 1 * ?");
        final Cron otherCron = getCron("* * * 15 * ?");
        assertEquals("every second, on the 1st day, every month", descriptor.describe(first));
        assertEquals("every second, on the 2nd day, every month", descriptor.describe(second));
        assertEquals("every second, on the 3rd day, every month", descriptor.describe(third));
        assertEquals("every second, on the 15th day, every month", descriptor.describe(otherCron));
    }

    @Ignore
    @Test
    public void testEverySecondEveryDayInMonth() {
        final Cron cron = getCron("* * * * 1 ? *");
        final Cron otherCron = getCron("* * 12 * * ? *");
        assertEquals("every second, every day, in January", descriptor.describe(cron));
        assertEquals("every second, every day, in December", descriptor.describe(otherCron));
    }

    @Ignore
    @Test
    public void testEverySecondEveryDayInMonthWithoutYear() {
        final Cron cron = getCron("* * * * 1 ?");
        final Cron otherCron = getCron("* * 12 * * ?");
        assertEquals("every second, every day, in January", descriptor.describe(cron));
        assertEquals("every second, every day, in December", descriptor.describe(otherCron));
    }

    @Ignore
    @Test
    public void testEverySecondEveryDayInYear() {
        final Cron cron = getCron("* * * * * ? 2017");
        final Cron otherCron = getCron("* * * * * ? 2018");
        //maybe we could assert only "every second in 2017"
        assertEquals("every second, every day, in 2017", descriptor.describe(cron));
        assertEquals("every second, every day, in 2018", descriptor.describe(otherCron));
    }

    @Ignore
    @Test
    public void testAtTimeEveryDay() {
        final Cron cron = getCron("0 0 0 * * ? *");
        assertEquals("at 00:00:00am every day", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeEveryDay2() {
        final Cron cron = getCron("00 00 16 * * ? *");
        assertEquals("at 16:00:00pm every day", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtMultipleTimesEveryDay() {
        final Cron cron = getCron("00 00 8,16 * * ? *");
        assertEquals("at second 00, at minute 00, at 08am and 16pm, of every day", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOfDayEveryMonth() {
        final Cron cron = getCron("0 0 16 1 * ? *");
        assertEquals("at 16:00:00pm, on the first day, every month", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOfMultipleDaysEveryMonth() {
        final Cron cron = getCron("0 0 16 1,5,26 * ? *");
        assertEquals("at 16:00:00pm on the 1st, 5th and 26th day, every month", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOfEveryDayInMonth() {
        final Cron cron = getCron("0 0 16 * 3 ? *");
        assertEquals("at 16:00:00pm, every day, in March", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOfEveryDayInMultipleMonth() {
        final Cron cron = getCron("0 0 16 * 1,5,12 ? *");
        assertEquals("at 16:00:00pm, every day in January, May and December", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOfEveryDayBetweenMonths() {
        final Cron cron = getCron("0 0 16 * 3-7 ? *");
        assertEquals("at 16:00:00pm, every day between March and July", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOfEveryDayBetweenMultipleMonths() {
        final Cron cron = getCron("0 0 16 * 3-7,10-12 ? *");
        assertEquals("at 16:00:00pm, every day between march and July and every month between October and December", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOfFirstInMultipleMonths() {
        final Cron cron = getCron("0 0 16 1 3,5,12 ? *");
        assertEquals("at 16:00:00pm, on the 1st day in March, May and December", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOfSecondInMultipleMonths() {
        final Cron cron = getCron("0 0 16 2 3,5,12 ? *");
        assertEquals("at 16:00:00pm, on the 2nd day in March, May and December", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOfThirdInMultipleMonths() {
        final Cron cron = getCron("0 0 16 3 3,5,12 ? *");
        assertEquals("at 16:00:00pm, on the 3rd day in March, May and December", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOfSpecificDayInMultipleMonths() {
        final Cron cron = getCron("0 0 16 15 3,5,12 ? *");
        assertEquals("at 16:00:00pm, on the 15th day in March, May and December", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOfSpecificDayBetweenMonths() {
        final Cron cron = getCron("0 0 16 15 3-7 ? *");
        assertEquals("at 16:00:00pm, on the 15th day every month between March and July", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOfSpecificDayBetweenMultipleMonths() {
        final Cron cron = getCron("0 0 16 15 3-7,10-12 ? *");
        assertEquals("at 16:00:00pm, on the 15th day every month between March and July and every month between October and December",
                     descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOnLastDayOfMonth() {
        final Cron cron = getCron("0 0 16 L * ? *");
        assertEquals("at 16:00:00pm, on the last day of the month, every month", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOnLastDayOfSpecificMonth() {
        final Cron cron = getCron("0 0 16 L 3 ? *");
        assertEquals("at 16:00:00pm, on the last day of the month, in March", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOnThirdLastDayOfMonth() {
        final Cron cron = getCron("0 0 16 L-3 * ? *");
        assertEquals("at 16:00:00pm, 3 days before the end of the month, every month", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOnNthSpecificWeekdayOfMonth() {
        final Cron cron = getCron("0 0 16 ? * 5#2 *");
        assertEquals("at 16:00:00pm, on the 2nd Thursday of the month, every month", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeOnLastSpecificWeekdayOfMonth() {
        final Cron cron = getCron("0 0 16 ? * 2L *");
        assertEquals("at 16:00:00pm, on the last Monday of the month, every month", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeEverySpecificWeekdayOfMonth() {
        final Cron cron = getCron("0 0 16 ? * 2 *");
        assertEquals("at 16:00:00pm, every Monday of the month, every month", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeEverySpecificWeekdayOfSpecificMonth() {
        final Cron cron = getCron("0 0 16 ? 5 3 *");
        assertEquals("at 16:00:00pm, every Tuesday of the month, in May", descriptor.describe(cron));
    }

    @Ignore
    @Test
    public void testAtTimeMultipleWeekdaysOfMultipleSpecificMonths() {
        final Cron cron = getCron("0 0 16 ? 1,5,12 2,5,7 *");
        assertEquals("at 16:00:00pm, every Monday, Thursday and Saturday of the month, in January, May and December", descriptor.describe(cron));
    }




}
