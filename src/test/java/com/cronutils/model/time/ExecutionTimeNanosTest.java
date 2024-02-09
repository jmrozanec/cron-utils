package com.cronutils.model.time;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExecutionTimeNanosTest {
    public static ExecutionTime createExecutionTimeWithSeconds() {
        CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .instance();
        Cron cron = new CronParser(cronDefinition)
                .parse("0,1,3 * * * *");

        return ExecutionTime.forCron(cron);
    }

    public static ExecutionTime createExecutionTimeWithoutSeconds() {
        CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .instance();
        Cron cron = new CronParser(cronDefinition)
                .parse("0,1,3 * * *");

        return ExecutionTime.forCron(cron);
    }

    public static Stream<Arguments> cronExpressionsWithSeconds() {
        // args are "now", "isMatch", "expectedNext", "expectedLast"
        // for cron "0,1,3 * * * *"
        return Stream.of(
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 46, 0, 0, ZoneOffset.UTC),
                true,
                ZonedDateTime.of(2024, 2, 7, 13, 46, 1, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 13, 45, 3, 0, ZoneOffset.UTC)
            ),
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 46, 0, 999999999, ZoneOffset.UTC),
                false,
                ZonedDateTime.of(2024, 2, 7, 13, 46, 1, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 13, 46, 0, 0, ZoneOffset.UTC)
            ),
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 46, 1, 0, ZoneOffset.UTC),
                true,
                ZonedDateTime.of(2024, 2, 7, 13, 46, 3, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 13, 46, 0, 0, ZoneOffset.UTC)
            ),
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 46, 1, 999999999, ZoneOffset.UTC),
                false,
                ZonedDateTime.of(2024, 2, 7, 13, 46, 3, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 13, 46, 1, 0, ZoneOffset.UTC)
            ),
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 46, 2, 0, ZoneOffset.UTC),
                false,
                ZonedDateTime.of(2024, 2, 7, 13, 46, 3, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 13, 46, 1, 0, ZoneOffset.UTC)
            ),
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 46, 2, 999999999, ZoneOffset.UTC),
                false,
                ZonedDateTime.of(2024, 2, 7, 13, 46, 3, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 13, 46, 1, 0, ZoneOffset.UTC)
            ),
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 46, 3, 0, ZoneOffset.UTC),
                true,
                ZonedDateTime.of(2024, 2, 7, 13, 47, 0, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 13, 46, 1, 0, ZoneOffset.UTC)
        ),
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 46, 3, 999999999, ZoneOffset.UTC),
                false,
                ZonedDateTime.of(2024, 2, 7, 13, 47, 0, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 13, 46, 3, 0, ZoneOffset.UTC)
            )
        );
    }

    public static Stream<Arguments> cronExpressionsWithoutSeconds() {
        // args are "now", "isMatch", "expectedNext", "expectedLast"
        // for cron "0,1,3 * * *"
        return Stream.of(
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 0, 0, 0, ZoneOffset.UTC),
                true,
                ZonedDateTime.of(2024, 2, 7, 13, 1, 0, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 12, 3, 0, 0, ZoneOffset.UTC)
            ),
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 0, 0, 999999999, ZoneOffset.UTC),
                false,
                ZonedDateTime.of(2024, 2, 7, 13, 1, 0, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 13, 0, 0, 0, ZoneOffset.UTC)
            ),
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 1, 0, 0, ZoneOffset.UTC),
                true,
                ZonedDateTime.of(2024, 2, 7, 13, 3, 0, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 13, 0, 0, 0, ZoneOffset.UTC)
            ),
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 1, 0, 999999999, ZoneOffset.UTC),
                false,
                ZonedDateTime.of(2024, 2, 7, 13, 3, 0, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 13, 1, 0, 0, ZoneOffset.UTC)
            ),
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 2, 0, 0, ZoneOffset.UTC),
                false,
                ZonedDateTime.of(2024, 2, 7, 13, 3, 0, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 13, 1, 0, 0, ZoneOffset.UTC)
            ),
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 2, 0, 999999999, ZoneOffset.UTC),
                false,
                ZonedDateTime.of(2024, 2, 7, 13, 3, 0, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 13, 1, 0, 0, ZoneOffset.UTC)
            ),
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 3, 0, 0, ZoneOffset.UTC),
                true,
                ZonedDateTime.of(2024, 2, 7, 14, 0, 0, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 13, 1, 0, 0, ZoneOffset.UTC)
            ),
            Arguments.of(
                ZonedDateTime.of(2024, 2, 7, 13, 3, 0, 999999999, ZoneOffset.UTC),
                true,
                ZonedDateTime.of(2024, 2, 7, 14, 0, 0, 0, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 2, 7, 13, 3, 0, 0, ZoneOffset.UTC)
            )
        );
    }

    @ParameterizedTest
    @MethodSource("cronExpressionsWithSeconds")
    public void testIsMatchWithSeconds(ZonedDateTime now, boolean expectedIsMatch) {
        ExecutionTime executionTime = createExecutionTimeWithSeconds();

        boolean isMatch = executionTime.isMatch(now);
        assertEquals(expectedIsMatch, isMatch);
    }

    @ParameterizedTest
    @MethodSource("cronExpressionsWithSeconds")
    public void testNextExecutionWithSeconds(ZonedDateTime now, boolean ignored1, ZonedDateTime expectedNext) {
        ExecutionTime executionTime = createExecutionTimeWithSeconds();

        Optional<ZonedDateTime> nextTime = executionTime.nextExecution(now);
        assertTrue(nextTime.isPresent());
        assertEquals(expectedNext, nextTime.get());
    }

    @ParameterizedTest
    @MethodSource("cronExpressionsWithSeconds")
    public void testLastExecutionWithSeconds(ZonedDateTime now, boolean ignored1, ZonedDateTime ignored2, ZonedDateTime expectedLast) {
        ExecutionTime executionTime = createExecutionTimeWithSeconds();

        Optional<ZonedDateTime> lastTime = executionTime.lastExecution(now);
        assertTrue(lastTime.isPresent());
        assertEquals(expectedLast, lastTime.get());
    }

    @ParameterizedTest
    @MethodSource("cronExpressionsWithoutSeconds")
    public void testIsMatchWithoutSeconds(ZonedDateTime now, boolean expectedIsMatch) {
        ExecutionTime executionTime = createExecutionTimeWithoutSeconds();

        boolean isMatch = executionTime.isMatch(now);
        assertEquals(expectedIsMatch, isMatch);
    }

    @ParameterizedTest
    @MethodSource("cronExpressionsWithoutSeconds")
    public void testNextExecutionWithoutSeconds(ZonedDateTime now, boolean ignored, ZonedDateTime expectedNext) {
        ExecutionTime executionTime = createExecutionTimeWithoutSeconds();

        Optional<ZonedDateTime> nextTime = executionTime.nextExecution(now);
        assertTrue(nextTime.isPresent());
        assertEquals(expectedNext, nextTime.get());
    }

    @ParameterizedTest
    @MethodSource("cronExpressionsWithoutSeconds")
    public void testLastExecutionWithoutSeconds(ZonedDateTime now, boolean ignored1, ZonedDateTime ignored2, ZonedDateTime expectedLast) {
        ExecutionTime executionTime = createExecutionTimeWithoutSeconds();

        Optional<ZonedDateTime> lastTime = executionTime.lastExecution(now);
        assertTrue(lastTime.isPresent());
        assertEquals(expectedLast, lastTime.get());
    }
}
