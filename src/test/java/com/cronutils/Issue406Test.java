package com.cronutils;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class Issue406Test {
    @Test
    public void testDayOfWeekIsCorrectlyApplied() {
        // GIVEN a spring cron operating at 1AM every weekday
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING);
        final CronParser parser = new CronParser(cronDefinition);
        final ExecutionTime execTime = ExecutionTime.forCron(parser.parse("0 0 1 * * MON-FRI"));

        // WHEN I get the next execution at 3AM on Saturday
        final ZonedDateTime threeAmFifthJanuary2019 = ZonedDateTime.of(
            LocalDate.of(2019, 1, 5),
            LocalTime.of(3, 0),
            ZoneId.systemDefault()
        );
        final Optional<ZonedDateTime> nextExecution = execTime.nextExecution(threeAmFifthJanuary2019);
        
        // THEN the result is 1AM on Monday
        assertEquals(
            Optional.of(
                    ZonedDateTime.of(
                    LocalDate.of(2019, 1, 7),
                    LocalTime.of(1, 0),
                    ZoneId.systemDefault()
                )
            ),
            nextExecution
        );
    }
}
