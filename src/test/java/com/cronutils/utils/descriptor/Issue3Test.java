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
}
