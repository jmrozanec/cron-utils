package com.cronutils;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #715: nW throws DateTimeException on short months instead of skipping them.
 */
public class Issue715Test {

    @Test
    public void nextExecutionSkipsShortMonthsFor31WQuartz() {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        final ExecutionTime et = ExecutionTime.forCron(parser.parse("0 0 0 31W * ?"));

        ZonedDateTime current = ZonedDateTime.parse("2025-06-01T00:00:00Z");
        final List<String> actual = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final Optional<ZonedDateTime> next = et.nextExecution(current);
            assertTrue(next.isPresent(), "expected next execution after " + current);
            actual.add(next.get().toLocalDate().toString());
            current = next.get();
        }

        // Quartz 2.5.0 reference: skip June/Sep/Nov/Feb; weekday-adjust Aug and Jan
        assertEquals(List.of(
                "2025-07-31",
                "2025-08-29",
                "2025-10-31",
                "2025-12-31",
                "2026-01-30"
        ), actual);
    }

    @Test
    public void nextExecutionSkipsShortMonthsFor31WSpring53() {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING53));
        final ExecutionTime et = ExecutionTime.forCron(parser.parse("0 0 0 31W * ?"));

        final Optional<ZonedDateTime> next = et.nextExecution(ZonedDateTime.parse("2025-06-01T00:00:00Z"));
        assertTrue(next.isPresent());
        assertEquals("2025-07-31", next.get().toLocalDate().toString());
    }
}
