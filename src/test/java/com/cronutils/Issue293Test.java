package com.cronutils;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

// https://github.com/jmrozanec/cron-utils/issues/293
@RunWith(Parameterized.class)
public class Issue293Test {
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final String cronText;

    /**
     * Each test is a cron spec excluding the reference month (December)
     * @return unix cron
     */
    @Parameterized.Parameters(name = "{0}")
    public static String[] data() {
        return new String[] {
            "15 18 * 1-11 *",       // DateTimeException - Invalid date - Nov 31
            "15 18 * 1-11 0-5",     // DateTimeException - Invalid date - Nov 31
            "15 18 * 1-11 4-5",     // Actual is 11/24
            "15 18 * 1-11 1-5"      // Actual is 11/29
        };
    }

    public Issue293Test(String cronText) {
        this.cronText = cronText;
    }

    @Test
    public void test() {
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
