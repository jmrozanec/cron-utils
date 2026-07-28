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
 * Issue 126 - "0 59 10 ? 1/2 MON#1 *" read as "at 10:59 every February months Monday 1 of every
 * month". The nth day of week was spelled out as a bare number and claimed to happen "of every
 * month", contradicting the month field that had just restricted it.
 */
public class Issue126Test {

    private final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

    @ParameterizedTest
    @CsvSource({
            "'0 59 10 ? * MON#1 *', 'at 10:59 on the first Monday of the month'",
            "'0 0 0 ? * TUE#2',     'at 00:00 on the second Tuesday of the month'",
            "'0 0 0 ? * WED#3',     'at 00:00 on the third Wednesday of the month'",
            "'0 0 0 ? * THU#4',     'at 00:00 on the fourth Thursday of the month'",
            "'0 0 0 ? * FRI#5',     'at 00:00 on the fifth Friday of the month'"
    })
    public void nthDayOfWeekNamesItsPosition(String expression, String expected) {
        assertEquals(expected, CronDescriptor.instance(Locale.ENGLISH).describe(parser.parse(expression)));
    }

    @ParameterizedTest
    @CsvSource({
            "'0 0 0 ? * MONL',  'at 00:00 on the last Monday of the month'",
            "'0 0 0 ? * 6L',    'at 00:00 on the last Friday of the month'"
    })
    public void lastDayOfWeekReadsTheSameWay(String expression, String expected) {
        assertEquals(expected, CronDescriptor.instance(Locale.ENGLISH).describe(parser.parse(expression)));
    }

    /**
     * The expression from the report: a restricted month must not be followed by a day of week
     * claiming every month.
     */
    @ParameterizedTest
    @CsvSource({
            "'0 59 10 ? 1/2 MON#1 *', 'at 10:59 every 2 months from January on the first Monday of the month'",
            "'0 59 10 ? 3 MON#1 *',   'at 10:59 in March on the first Monday of the month'"
    })
    public void aRestrictedMonthIsNotContradicted(String expression, String expected) {
        assertEquals(expected, CronDescriptor.instance(Locale.ENGLISH).describe(parser.parse(expression)));
    }
}
