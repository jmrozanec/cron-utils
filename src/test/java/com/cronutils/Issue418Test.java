package com.cronutils;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.hamcrest.core.StringEndsWith;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.junit.Assert.*;

public class Issue418Test {

    @Test
    public void testQuartzEvery7DaysStartingSunday() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        final CronParser parser = new CronParser(cronDefinition);
        final ExecutionTime execTime = ExecutionTime.forCron(parser.parse("0 0 2 ? * 1/7 *"));

        final ZonedDateTime startDate = ZonedDateTime.of(
                LocalDate.of(2020, 4, 1),
                LocalTime.of(3, 0),
                ZoneId.systemDefault()
        );
        final ZonedDateTime[] expectedDates = {
                ZonedDateTime.of(
                        LocalDate.of(2020, 4, 5),
                        LocalTime.of(2, 0),
                        ZoneId.systemDefault()
                ),
                ZonedDateTime.of(
                        LocalDate.of(2020, 4, 12),
                        LocalTime.of(2, 0),
                        ZoneId.systemDefault()
                )
        };

        Optional<ZonedDateTime> nextExecution = execTime.nextExecution(startDate);
        assert(nextExecution.isPresent());
        assertEquals( expectedDates[0], nextExecution.get());

        nextExecution = execTime.nextExecution(nextExecution.get());
        assert(nextExecution.isPresent());
        assertEquals( expectedDates[1], nextExecution.get());
    }

    @Test
    public void testInvalidWeekDayStart() {
        try {
            final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
            final CronParser parser = new CronParser(cronDefinition);
            parser.parse("0 0 2 ? * 0/7 *");
            fail("Expected exception for invalid expression");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), endsWith("Value 0 not in range [1, 7]"));
        }
    }

    @Test
    public void testInvalidWeekDayEnd() {
        try {
            final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
            final CronParser parser = new CronParser(cronDefinition);
            parser.parse("0 0 2 ? * 1/8 *");
            fail("Expected exception for invalid expression");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), endsWith("Period 8 not in range [1, 7]"));
        }
    }
}
