package com.cronutils.utils.descriptor;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Issue 3 - a cron firing on an explicit minute 0 lost that minute in its description, so
 * "* 0 9-23 * * ?" read as "every second every hour between 9 and 23" and claimed roughly
 * sixty times the firings the expression actually produces.
 */
public class Issue3Test {

    private final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

    @ParameterizedTest
    @CsvSource({
            "'* 0 9-23 * * ?', 'every second at 0 minute every hour between 9 and 23'",
            "'5 0 9-23 * * ?', 'at 5 second at 0 minute every hour between 9 and 23'",
            "'0/5 0 * * * ?',  'every 5 seconds from second 0 at 0 minute'",
            "'* 0 */4 * * ?',  'every second at 0 minute every 4 hours'"
    })
    public void explicitMinuteZeroIsDescribedWhenSecondsAreNotImplicit(String expression, String expected) {
        assertEquals(expected, CronDescriptor.instance(Locale.ENGLISH).describe(parser.parse(expression)));
    }

    @ParameterizedTest
    @CsvSource({
            "'0 0 9-23 * * ?', 'every hour between 9 and 23'",
            "'0 0 10 * * ?',   'at 10:00'"
    })
    public void minuteZeroStaysImplicitWhenSecondsAreImplicitToo(String expression, String expected) {
        assertEquals(expected, CronDescriptor.instance(Locale.ENGLISH).describe(parser.parse(expression)));
    }

    @ParameterizedTest
    @CsvSource({
            "'* 30 9-23 * * ?', 'every second at 30 minute every hour between 9 and 23'",
            "'* 0 10 * * ?',    'every second at 10:00'"
    })
    public void nonZeroAndCollapsibleMinutesAreUnaffected(String expression, String expected) {
        assertEquals(expected, CronDescriptor.instance(Locale.ENGLISH).describe(parser.parse(expression)));
    }

    @ParameterizedTest
    @CsvSource({
            "'0 0 0 1 1 ?',      'at 00:00 on day 1 in January'",
            "'0 11 11 11 11 ?',  'at 11:11 on day 11 in November'",
            "'0 15 10 * * ? 2005', 'at 10:15 in 2005'"
    })
    public void dayOfMonthMonthAndYearReadAsProse(String expression, String expected) {
        assertEquals(expected, CronDescriptor.instance(Locale.ENGLISH).describe(parser.parse(expression)));
    }

    @ParameterizedTest
    @CsvSource({
            "'0/1 * * * * ?', 'every second'",
            "'*/1 * * * * ?', 'every second'",
            "'* * * * * ?',   'every second'"
    })
    public void aPeriodOfOneFromZeroDropsTheRedundantStart(String expression, String expected) {
        assertEquals(expected, CronDescriptor.instance(Locale.ENGLISH).describe(parser.parse(expression)));
    }

    @ParameterizedTest
    @CsvSource({
            "'5/1 * * * * ?',  'every second from second 5'",
            "'0 0/5 14 * * ?', 'every 5 minutes from minute 0 at 14 hour'",
            "'0 0 0 3/5 * ?',  'at 00:00 every 5 days from day 3'"
    })
    public void aMeaningfulStartIsKept(String expression, String expected) {
        assertEquals(expected, CronDescriptor.instance(Locale.ENGLISH).describe(parser.parse(expression)));
    }

    @ParameterizedTest
    @CsvSource({
            "'0 0 0 ? * SUN',        'at 00:00 on Sunday'",
            "'0 10,44 14 ? 3 WED',   'at 10 and 44 minutes at 14 hour in March on Wednesday'",
            "'0 0 0 ? * MON-FRI',    'at 00:00 every day between Monday and Friday'",
            "'0 0 0 ? * 6L',         'at 00:00 on the last Friday of the month'",
            "'0 0 0 ? * 3/5',        'at 00:00 every 5 days from Tuesday'"
    })
    public void dayOfWeekReadsAsProse(String expression, String expected) {
        assertEquals(expected, CronDescriptor.instance(Locale.ENGLISH).describe(parser.parse(expression)));
    }

    /**
     * A list has to read the same way as the single value it generalises, else "in March"
     * sits next to "at January, May and December months" in the same vocabulary.
     */
    @ParameterizedTest
    @CsvSource({
            "'0 0 16 * 1,5,12 ?',      'at 16:00 in January, May and December'",
            "'0 0 16 1,5,26 * ?',      'at 16:00 on days 1, 5 and 26'",
            "'0 0 0 ? * MON,WED,FRI',  'at 00:00 on Monday, Wednesday and Friday'",
            "'0 15 10 * * ? 2005,2006','at 10:15 in 2005 and 2006'"
    })
    public void listsReadLikeTheSingleValueForm(String expression, String expected) {
        assertEquals(expected, CronDescriptor.instance(Locale.ENGLISH).describe(parser.parse(expression)));
    }

    /**
     * The expression from Naxos84's 2017 comment on issue 3, which described month 10 as
     * "month 10" rather than October and the year start as "year 2017".
     */
    @org.junit.jupiter.api.Test
    public void everyFieldAsAnEveryWithAStart() {
        assertEquals("every 4 seconds from second 3 every 6 minutes from minute 5 every 8 hours from hour 7 "
                        + "every 2 days from day 9 every 2 months from October every 2 years from 2017",
                CronDescriptor.instance(Locale.ENGLISH).describe(parser.parse("3/4 5/6 7/8 9/2 10/2 ? 2017/2")));
    }
}
