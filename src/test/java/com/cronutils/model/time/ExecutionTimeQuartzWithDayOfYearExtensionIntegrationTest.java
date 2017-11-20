package com.cronutils.model.time;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.definition.TestCronDefinitionsFactory;
import com.cronutils.parser.CronParser;

import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertEquals;

/*
 * Copyright 2015 jmrozanec Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
public class ExecutionTimeQuartzWithDayOfYearExtensionIntegrationTest {
    private static final String BI_WEEKLY_STARTING_WITH_FIRST_DAY_OF_YEAR = "0 0 0 ? * ? * 1/14";
    private static final String FIRST_QUARTER_BI_WEEKLY_STARTING_WITH_FIRST_DAY_OF_YEAR = "0 0 0 ? 1-3 ? * 1/14";
    private static final String WITHOUT_DAY_OF_YEAR = "0 0 0 1 * ? *";       // i.e. DoY field omitted
    private static final String WITHOUT_SPECIFIC_DAY_OF_YEAR = "0 0 0 1 * ? * ?";     // i.e. DoY field set to question mark

    private CronParser parser;
    private CronParser quartzParser;

    @Before
    public void setUp() throws Exception {
        parser = new CronParser(TestCronDefinitionsFactory.withDayOfYearDefinitionWhereYearAndDoYOptionals());
        quartzParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    @Test
    public void testForCron() {
        assertEquals(ExecutionTime.class, ExecutionTime.forCron(parser.parse(BI_WEEKLY_STARTING_WITH_FIRST_DAY_OF_YEAR)).getClass());
        assertEquals(ExecutionTime.class, ExecutionTime.forCron(parser.parse(FIRST_QUARTER_BI_WEEKLY_STARTING_WITH_FIRST_DAY_OF_YEAR)).getClass());
        assertEquals(ExecutionTime.class, ExecutionTime.forCron(parser.parse(WITHOUT_DAY_OF_YEAR)).getClass());
        assertEquals(ExecutionTime.class, ExecutionTime.forCron(parser.parse(WITHOUT_SPECIFIC_DAY_OF_YEAR)).getClass());
    }

    @Test //issue #249
    public void testNextExecutionEveryTwoWeeksStartingWithFirstDayOfYear() {
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(BI_WEEKLY_STARTING_WITH_FIRST_DAY_OF_YEAR));

        for (int i = 1; i < 30; i++) {
            final ZonedDateTime now = ZonedDateTime.of(2017, 10, i, 0, 0, 0, 0, UTC);
            final int dayOfMostRecentPeriod = (now.getDayOfYear() - 1) % 14;
            final ZonedDateTime expected = now.plusDays(14 - dayOfMostRecentPeriod);
            assertEquals("Wrong next time from " + now, expected, executionTime.nextExecution(now).get());
        }
    }

    @Test
    public void testLastExecutionEveryTwoWeeksStartingWithFirstDayOfYear() {
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(BI_WEEKLY_STARTING_WITH_FIRST_DAY_OF_YEAR));

        for (int i = 1; i < 30; i++) {
            final ZonedDateTime now = ZonedDateTime.of(2017, 10, i, 0, 0, 0, 0, UTC);
            final int dayOfMostRecentPeriod = (now.getDayOfYear() - 1) % 14;
            final ZonedDateTime expected = now.minusDays(dayOfMostRecentPeriod == 0 ? 14 : dayOfMostRecentPeriod);
            assertEquals("Wrong next time from " + now, expected, executionTime.lastExecution(now).get());
        }
    }

    @Test
    public void testLastExecutionEveryTwoWeeksStartingWithFirstDayOfYearIssue249TzUTC() {
        //s m H DoM M DoW Y DoY
        ZonedDateTime now = ZonedDateTime.of(2017, 10, 7, 0, 0, 0, 0, UTC);
        ZonedDateTime expected = ZonedDateTime.of(2017, 9, 24, 0, 0, 0, 0, UTC);
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(BI_WEEKLY_STARTING_WITH_FIRST_DAY_OF_YEAR));
        assertEquals(expected, executionTime.lastExecution(now).get());
    }

    @Test
    public void testLastExecutionEveryTwoWeeksStartingWithFirstDayOfYearIssue249TzBuenosAires() {
        //s m H DoM M DoW Y DoY
        ZonedDateTime now = ZonedDateTime.of(2017, 10, 7, 0, 0, 0, 0, ZoneId.of("America/Argentina/Buenos_Aires"));
        ZonedDateTime expected = ZonedDateTime.of(2017, 9, 24, 0, 0, 0, 0, ZoneId.of("America/Argentina/Buenos_Aires"));
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(BI_WEEKLY_STARTING_WITH_FIRST_DAY_OF_YEAR));
        assertEquals(expected, executionTime.lastExecution(now).get());
    }

    @Test
    public void testExecutionTimesEveryTwoWeeksStartingWithFirstDayOfYear() {
        final ZonedDateTime[] expectedExecutionTimes = new ZonedDateTime[] {
                ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, UTC),
                ZonedDateTime.of(2017, 1, 15, 0, 0, 0, 0, UTC),
                ZonedDateTime.of(2017, 1, 29, 0, 0, 0, 0, UTC),
                ZonedDateTime.of(2017, 2, 12, 0, 0, 0, 0, UTC),
                ZonedDateTime.of(2017, 2, 26, 0, 0, 0, 0, UTC),
                ZonedDateTime.of(2017, 3, 12, 0, 0, 0, 0, UTC),
                ZonedDateTime.of(2017, 3, 26, 0, 0, 0, 0, UTC),
                ZonedDateTime.of(2017, 4, 9, 0, 0, 0, 0, UTC),
                ZonedDateTime.of(2017, 4, 23, 0, 0, 0, 0, UTC),
                ZonedDateTime.of(2017, 5, 7, 0, 0, 0, 0, UTC),
                ZonedDateTime.of(2017, 5, 21, 0, 0, 0, 0, UTC),
                ZonedDateTime.of(2017, 6, 4, 0, 0, 0, 0, UTC)
        };

        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(BI_WEEKLY_STARTING_WITH_FIRST_DAY_OF_YEAR));

        for (final ZonedDateTime expectedExecutionTime : expectedExecutionTimes) {
            assertEquals(expectedExecutionTime, executionTime.nextExecution(expectedExecutionTime.minusDays(1)).get());
        }

        for (int i = 1; i < expectedExecutionTimes.length; i++) {
            assertEquals(expectedExecutionTimes[i], executionTime.nextExecution(expectedExecutionTimes[i - 1]).get());
        }

        for (int i = 1; i < expectedExecutionTimes.length; i++) {
            assertEquals(expectedExecutionTimes[i - 1], executionTime.lastExecution(expectedExecutionTimes[i]).get());
        }
    }

    @Test //issue #188
    public void testQuartzCompatibilityIfDoYisOmitted() {
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(WITHOUT_DAY_OF_YEAR));
        final ExecutionTime quartzExecutionTime = ExecutionTime.forCron(quartzParser.parse(WITHOUT_DAY_OF_YEAR));

        ZonedDateTime start = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, UTC).minusSeconds(1);
        for (int i = 0; i < 12; i++) {
            final ZonedDateTime expectedDateTime = quartzExecutionTime.nextExecution(start).get();
            assertEquals(quartzExecutionTime.nextExecution(start).get(), executionTime.nextExecution(start).get());
            start = expectedDateTime.plusSeconds(1);
        }
    }

    @Test //issue #188
    public void testQuartzCompatibilityIfDoYisQuestionMark() {
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(WITHOUT_SPECIFIC_DAY_OF_YEAR));
        final ExecutionTime quartzExecutionTime = ExecutionTime.forCron(quartzParser.parse(WITHOUT_DAY_OF_YEAR));

        ZonedDateTime start = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, UTC).minusSeconds(1);
        for (int i = 0; i < 12; i++) {
            final ZonedDateTime expectedDateTime = quartzExecutionTime.nextExecution(start).get();
            assertEquals(quartzExecutionTime.nextExecution(start).get(), executionTime.nextExecution(start).get());
            start = expectedDateTime.plusSeconds(1);
        }
    }

    @Test //issue #190
    public void testExecutionTimesWithIncrementsGreaterThanDaysOfMonth() {
        final int increment = 56;
        final String incrementGreaterDaysOfMonthStartingWithFirstDayOfYear = "0 0 0 ? * ? * 1/" + String.valueOf(increment);
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(incrementGreaterDaysOfMonthStartingWithFirstDayOfYear));
        ZonedDateTime start = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, UTC);
        for (int i = 0; i < 6; i++) {
            final ZonedDateTime expected = start;
            final ZonedDateTime actual = executionTime.nextExecution(start.minusSeconds(1)).get();
            assertEquals(expected, actual);
            start = expected.plusDays(increment);
        }
    }

    @Test
    public void testCustomQuarterlySchedule() {
        final ZonedDateTime firstDayOf2016 = ZonedDateTime.of(2016, 1, 1, 0, 0, 0, 0, UTC);
        final ZonedDateTime startWithLastDayOf2015 = ZonedDateTime.of(2015, 12, 31, 0, 0, 0, 0, UTC);
        final String customQuarterlyStartingWithDay47OfYear = "0 0 0 ? * ? * 47/91";
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(customQuarterlyStartingWithDay47OfYear));
        //final ZonedDateTime[] expectedExecutionTimes = IntStream.range(0, 4).mapToObj(i -> firstDayOf2016.withDayOfYear(47 + i * 91)).toArray(ZonedDateTime[]::new);
        ZonedDateTime[] expectedExecutionTimes = new ZonedDateTime[4];
        for (int j = 0; j < 4; j++) {
            expectedExecutionTimes[j] = firstDayOf2016.withDayOfYear(47 + j * 91);
        }
        ZonedDateTime start = startWithLastDayOf2015;
        for (final ZonedDateTime expectedExecutionTime : expectedExecutionTimes) {
            final ZonedDateTime actual = executionTime.nextExecution(start).get();
            assertEquals(expectedExecutionTime, actual);
            start = expectedExecutionTime.plusSeconds(1);
        }
    }

}
