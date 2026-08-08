package com.cronutils;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #692: ExecutionTime incorrectly skips month intervals (e.g. "0 0 0 15 10/3 ? *").
 * "start/interval" on months must include year-wrapped months (Oct + every 3 months → Oct, Jan, Apr, Jul).
 */
public class Issue692Test {

    private final CronParser cronParser =
            new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

    @Test
    public void nextExecutionForMonthInterval() {
        // every 3 months starting in October
        final ExecutionTime executionTime = ExecutionTime.forCron(cronParser.parse("0 0 0 * 10/3 ? *"));
        final ZonedDateTime referenceDate = ZonedDateTime.parse("2026-02-01T00:00:00Z");
        final Optional<ZonedDateTime> next = executionTime.nextExecution(referenceDate);
        assertTrue(next.isPresent());
        assertEquals(LocalDate.of(2026, 4, 1), next.get().toLocalDate());
    }

    @Test
    public void nextExecutionForMonthIntervalWithSpecificDayOfMonth() {
        final ExecutionTime executionTime = ExecutionTime.forCron(cronParser.parse("0 0 0 15 10/3 ? *"));
        final ZonedDateTime referenceDate = ZonedDateTime.parse("2026-01-01T00:00:00Z");
        final Optional<ZonedDateTime> next = executionTime.nextExecution(referenceDate);
        assertTrue(next.isPresent());
        assertEquals(LocalDate.of(2026, 1, 15), next.get().toLocalDate());
    }

    @Test
    public void executionDatesForMonthInterval() {
        final ExecutionTime executionTime = ExecutionTime.forCron(cronParser.parse("0 0 0 15 10/3 ? *"));
        final ZonedDateTime startDate = ZonedDateTime.parse("2025-10-01T00:00:00Z");
        final ZonedDateTime endDate = ZonedDateTime.parse("2026-10-31T00:00:00Z");
        final List<LocalDate> expected = Arrays.asList(
                LocalDate.of(2025, 10, 15),
                LocalDate.of(2026, 1, 15),
                LocalDate.of(2026, 4, 15),
                LocalDate.of(2026, 7, 15),
                LocalDate.of(2026, 10, 15)
        );
        final List<LocalDate> actual = executionTime.getExecutionDates(startDate, endDate).stream()
                .map(ZonedDateTime::toLocalDate)
                .collect(Collectors.toList());
        assertEquals(expected, actual);
    }

    @Test
    public void previousExecutionForMonthInterval() {
        final ExecutionTime executionTime = ExecutionTime.forCron(cronParser.parse("0 0 0 15 10/3 ? *"));
        final Optional<ZonedDateTime> previous =
                executionTime.lastExecution(ZonedDateTime.parse("2026-02-01T00:00:00Z"));
        assertTrue(previous.isPresent());
        assertEquals(LocalDate.of(2026, 1, 15), previous.get().toLocalDate());
    }

    @Test
    public void nextExecutionSequenceEveryThreeMonthsFromOctober() {
        final ExecutionTime executionTime = ExecutionTime.forCron(cronParser.parse("0 0 0 1 10/3 ? *"));
        ZonedDateTime current = ZonedDateTime.parse("2025-09-01T00:00:00Z");
        final List<String> actual = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final Optional<ZonedDateTime> next = executionTime.nextExecution(current);
            assertTrue(next.isPresent(), "expected next after " + current);
            actual.add(next.get().toLocalDate().toString());
            current = next.get();
        }
        assertEquals(Arrays.asList(
                "2025-10-01",
                "2026-01-01",
                "2026-04-01",
                "2026-07-01",
                "2026-10-01"
        ), actual);
    }
}
