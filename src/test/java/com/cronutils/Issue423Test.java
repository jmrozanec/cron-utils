package com.cronutils;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
@Ignore
public class Issue423Test {
    private static final LocalDate saturday = LocalDate.of(2020, 4, 25);
    private static ZonedDateTime shortZDT(int h, int m) {
        return ZonedDateTime.of(
            saturday,
            LocalTime.of(h, m),
            ZoneId.of("Australia/Perth")
        );
    }

    private static class TestPair {
        public final ZonedDateTime test;
        public final ZonedDateTime expected;
        public TestPair(ZonedDateTime t, ZonedDateTime exp) {
            test = t;
            expected = exp;
        }
    }

    @Test
    public void issue423() {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        final Cron cron = parser.parse("0 0 0-07,17-0 ? * SAT");
        final CronDescriptor cd = CronDescriptor.instance(Locale.UK);
        assertTrue(cd.describe(cron).length() > 0);
        // at time of test creation, the descriptor is
        // "every hour between 0 and 7 and every hour between 17 and 0 at Saturday day"

        final ExecutionTime et = ExecutionTime.forCron(cron);
        // At this point, an an exception WAS logged. But, not anymore!

        Arrays.asList(
            new TestPair(shortZDT( 0,  0), shortZDT( 1, 0)),
            new TestPair(shortZDT( 0, 30), shortZDT( 1, 0)),
            new TestPair(shortZDT( 6,  0), shortZDT( 7, 0)),
            new TestPair(shortZDT( 7,  0), shortZDT(17, 0)),
            new TestPair(shortZDT(16,  0), shortZDT(17, 0)), // Should be 17:00, but skips to the next Saturday
            new TestPair(shortZDT(17,  0), shortZDT(18, 0)), // Should be 18:00, but skips to the next Saturday
            new TestPair(shortZDT(18,  0), shortZDT(19, 0))  // Should be 19:00, but skips to the next Saturday
        ).forEach(tp -> {
//            System.err.println("Expected: " + tp.expected + "; Actual: " + et.nextExecution(tp.test).get().toString());
            assertEquals(
                "All these should be on the same Saturday",
                tp.expected,
                et.nextExecution(tp.test).get()
            );
        });
    }
}
