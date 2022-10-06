package com.cronutils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

// https://github.com/jmrozanec/cron-utils/issues/293
public class Issue293Test {
    private static final ZoneId ZONE = ZoneId.systemDefault();

    @ParameterizedTest
    @ValueSource(strings = {
        "15 18 * 1-11 *",       // DateTimeException - Invalid date - Nov 31
        "15 18 * 1-11 0-5",     // DateTimeException - Invalid date - Nov 31
        "15 18 * 1-11 4-5",     // Actual is 11/24
        "15 18 * 1-11 1-5"      // Actual is 11/29
    })
    public void test(String cronText) {
        CronDefinition def = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser parser = new CronParser(def);

        Cron cron = parser.parse(cronText);
        ExecutionTime et = ExecutionTime.forCron(cron);

        ZonedDateTime vs = ZonedDateTime.of(2017, 12, 1, 9, 30, 0, 0, ZONE);
        assertEquals(DayOfWeek.FRIDAY, vs.getDayOfWeek());

        // Last match prior to our reference time
        ZonedDateTime expected = ZonedDateTime.of(2017, 11, 30, 18, 15, 0, 0, ZONE);
        assertEquals(DayOfWeek.THURSDAY, expected.getDayOfWeek());

	Optional<ZonedDateTime> lastExecution = et.lastExecution(vs);
	if (lastExecution.isPresent()) {
	    ZonedDateTime actual = lastExecution.get();
	    assertEquals(expected, actual);
	} else {
	    fail("last execution was not present");
	}
    }
}
