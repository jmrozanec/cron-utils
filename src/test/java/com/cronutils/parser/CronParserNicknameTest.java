package com.cronutils.parser;

import com.cronutils.builder.CronBuilder;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.cronutils.model.field.expression.Always.always;
import static com.cronutils.model.field.expression.FieldExpressionFactory.on;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CronParserNicknameTest {

    private CronParser parser;

    private CronDefinition definitionWithNicknames;

    @BeforeEach
    void setUp() {
        definitionWithNicknames = CronDefinitionBuilder.defineCron()
                //copied from CronDefinitionBuilder.unixCrontab()
                .withMinutes().withValidRange(0, 59).withStrictRange().and()
                .withHours().withValidRange(0, 23).withStrictRange().and()
                .withDayOfMonth().withValidRange(1, 31).withStrictRange().and()
                .withMonth().withValidRange(1, 12).withStrictRange().and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).withStrictRange().and()
                //add support for all custom nicknames
                .withSupportedNicknameYearly().withSupportedNicknameAnnually()
                .withSupportedNicknameMonthly().withSupportedNicknameWeekly()
                .withSupportedNicknameMidnight().withSupportedNicknameDaily()
                .withSupportedNicknameHourly()
                .instance();
        parser = new CronParser(definitionWithNicknames);
    }

    @Test
    void testYearly() {
        Cron expected = CronBuilder.cron(definitionWithNicknames)
                .withMinute(on(0))
                .withHour(on(0))
                .withDoM(on(1))
                .withMonth(on(1))
                .withDoW(always())
                .instance();
        Cron parsed = parser.parse("@yearly");
        assertTrue(parsed.equivalent(expected));
    }

    @Test
    void testAnnually() {
        Cron expected = CronBuilder.cron(definitionWithNicknames)
                .withMinute(on(0))
                .withHour(on(0))
                .withDoM(on(1))
                .withMonth(on(1))
                .withDoW(always())
                .instance();
        Cron parsed = parser.parse("@annually");
        assertTrue(parsed.equivalent(expected));
    }

    @Test
    void testMonthly() {
        Cron expected = CronBuilder.cron(definitionWithNicknames)
                .withMinute(on(0))
                .withHour(on(0))
                .withDoM(on(1))
                .withMonth(always())
                .withDoW(always())
                .instance();
        Cron parsed = parser.parse("@monthly");
        assertTrue(parsed.equivalent(expected));
    }

    @Test
    void testWeekly() {
        Cron expected = CronBuilder.cron(definitionWithNicknames)
                .withMinute(on(0))
                .withHour(on(0))
                .withDoM(always())
                .withMonth(always())
                .withDoW(on(0))
                .instance();
        Cron parsed = parser.parse("@weekly");
        assertTrue(parsed.equivalent(expected));
    }

    @Test
// issue #522
    void testMidnight() {
        Cron expected = CronBuilder.cron(definitionWithNicknames)
                .withMinute(on(0))
                .withHour(on(0))
                .withDoM(always())
                .withMonth(always())
                .withDoW(always())
                .instance();
        Cron parsed = parser.parse("@midnight");
        assertTrue(parsed.equivalent(expected));
    }

    @Test
    void testCronDaily() {
        Cron expected = CronBuilder.cron(definitionWithNicknames)
                .withMinute(on(0))
                .withHour(on(0))
                .withDoM(always())
                .withMonth(always())
                .withDoW(always())
                .instance();
        Cron parsed = parser.parse("@daily");
        assertTrue(parsed.equivalent(expected));
    }

    @Test
    void testCronHourly() {
        Cron expected = CronBuilder.cron(definitionWithNicknames)
                .withMinute(on(0))
                .withHour(always())
                .withDoM(always())
                .withMonth(always())
                .withDoW(always())
                .instance();
        Cron parsed = parser.parse("@hourly");
        assertTrue(parsed.equivalent(expected));
    }

}
