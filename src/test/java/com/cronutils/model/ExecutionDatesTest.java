package com.cronutils.model;

import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExecutionDatesTest {
    private CronParser cron4jCronParser;

    @BeforeEach
    public void setUp() {
        cron4jCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J));
    }

    @Test
    public void testExecutionCountBetweenDates() {
        int numOfDays = 10;
        ZonedDateTime startDate = ZonedDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        ZonedDateTime endDate = startDate.plusDays(numOfDays);

        ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse("0 * * * *"));
        int executionCount = executionTime.countExecutions(startDate, endDate);
        assertEquals(240, executionCount);
    }

    @Test
    public void testExecutionDatesBetweenDates() {
        int year = 2022;
        int month = 1;
        int dayOfMonth = 1;
        int minute = 0;
        int second = 0;
        int nanoSecond = 0;
        ZoneId zoneId = ZoneId.systemDefault();

        int numOfDays = 1;
        ZonedDateTime startDate = ZonedDateTime.of(year, month, dayOfMonth, 0, minute, second, nanoSecond, zoneId);
        ZonedDateTime endDate = startDate.plusDays(numOfDays);

        ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse("0 * * * *"));
        List<ZonedDateTime> dates = executionTime.getExecutionDates(startDate, endDate);

        assertEquals(24, dates.size());

        for (int i = 1; i < 24; i++) {
            ZonedDateTime expectedDate = ZonedDateTime.of(year, month, dayOfMonth, i, minute, second, nanoSecond, zoneId);
            ZonedDateTime actualDate = dates.get(i - 1);
            assertEquals(expectedDate, actualDate);
        }
    }

    @Test
    public void throwExceptionWhenEndDateIsBeforeStartDate() {
        ZonedDateTime startDate = ZonedDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        ZonedDateTime endDate = startDate.minusDays(1);

        ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse("0 * * * *"));
        assertThrows(IllegalArgumentException.class, () -> executionTime.getExecutionDates(startDate, endDate));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Europe/Paris", "Asia/Kolkata", "UTC"})
    public void includeEndInstantInDifferentZone(String endZone) {
        ZonedDateTime startDate = ZonedDateTime.parse("2026-01-01T00:00:00Z");
        ZonedDateTime expectedExecution = startDate.plusHours(9);
        ZonedDateTime endDate = expectedExecution.withZoneSameInstant(ZoneId.of(endZone));
        ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse("0 9 * * *"));

        assertEquals(List.of(expectedExecution), executionTime.getExecutionDates(startDate, endDate));
        assertEquals(1, executionTime.countExecutions(startDate, endDate));
        assertEquals(List.of(), executionTime.getExecutionDates(startDate, endDate.minusNanos(1)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Europe/Paris", "Asia/Kolkata", "UTC"})
    public void rejectEqualInstantsInDifferentZones(String endZone) {
        ZonedDateTime startDate = ZonedDateTime.parse("2026-01-01T00:00:00Z");
        ZonedDateTime endDate = startDate.withZoneSameInstant(ZoneId.of(endZone));
        ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse("0 * * * *"));

        assertThrows(IllegalArgumentException.class, () -> executionTime.getExecutionDates(startDate, endDate));
        assertThrows(IllegalArgumentException.class, () -> executionTime.countExecutions(startDate, endDate));
    }

    @Test
    public void throwExceptionWhenEndDateEqualsStarDate() {
        ZonedDateTime startDate = ZonedDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        ZonedDateTime endDate = ZonedDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());

        ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse("0 * * * *"));
        assertThrows(IllegalArgumentException.class, () -> executionTime.getExecutionDates(startDate, endDate));

    }
}
