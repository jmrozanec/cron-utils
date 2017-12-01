/*
 * Copyright 2015 jmrozanec
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cronutils;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertEquals;

public class Issue215Test {

    // test for https://github.com/jmrozanec/cron-utils/issues/215
    @Test
    public void testWorkdayBugWithNextMonth() {

        testWorkdays8Quartz(LocalDateTime.of(2017, 7, 7, 10, 0), LocalDateTime.of(2017, 7, 10, 8, 0));
        testWorkdays8Quartz(LocalDateTime.of(2017, 8, 31, 10, 0), LocalDateTime.of(2017, 9, 1, 8, 0));
        testWorkdays8Quartz(LocalDateTime.of(2017, 6, 30, 10, 0), LocalDateTime.of(2017, 7, 3, 8, 0));
        testWorkdays8Quartz(LocalDateTime.of(2017, 9, 29, 10, 0), LocalDateTime.of(2017, 10, 2, 8, 0));

        testWorkdays8Cron4j(LocalDateTime.of(2017, 7, 7, 10, 0), LocalDateTime.of(2017, 7, 10, 8, 0)); //good
        testWorkdays8Cron4j(LocalDateTime.of(2017, 8, 31, 10, 0), LocalDateTime.of(2017, 9, 1, 8, 0)); //good
        testWorkdays8Cron4j(LocalDateTime.of(2017, 6, 30, 10, 0), LocalDateTime.of(2017, 7, 3, 8, 0)); //not good
        testWorkdays8Cron4j(LocalDateTime.of(2017, 9, 29, 10, 0), LocalDateTime.of(2017, 10, 2, 8, 0)); //not good
    }

    @Test
    public void testFridayToSaturday() {
        // cron4j and quartz have different monday day of week values, so test both
        testFridayToSaturdayQuartz(
                LocalDateTime.of(2017, Month.MARCH, 28, 0, 0),
                LocalDateTime.of(2017, Month.MARCH, 31, 8, 0));
        testFridayToSaturdayQuartz(
                LocalDateTime.of(2017, Month.MARCH, 31, 9, 0),
                LocalDateTime.of(2017, Month.APRIL, 1, 8, 0));

        testFridayToSaturdayCron4j(
                LocalDateTime.of(2017, Month.MARCH, 28, 0, 0),
                LocalDateTime.of(2017, Month.MARCH, 31, 8, 0));
        testFridayToSaturdayCron4j(
                LocalDateTime.of(2017, Month.MARCH, 31, 9, 0),
                LocalDateTime.of(2017, Month.APRIL, 1, 8, 0));

        testFridayToSaturdayQuartz(
                LocalDateTime.of(2017, Month.JULY, 10, 0, 0),
                LocalDateTime.of(2017, Month.JULY, 14, 8, 0));
        testFridayToSaturdayQuartz(
                LocalDateTime.of(2017, Month.JULY, 15, 0, 0),
                LocalDateTime.of(2017, Month.JULY, 15, 8, 0));

        testFridayToSaturdayCron4j(
                LocalDateTime.of(2017, Month.JULY, 10, 0, 0),
                LocalDateTime.of(2017, Month.JULY, 14, 8, 0));
        testFridayToSaturdayCron4j(
                LocalDateTime.of(2017, Month.JULY, 15, 0, 0),
                LocalDateTime.of(2017, Month.JULY, 15, 8, 0));

        testFridayToSaturdayQuartz(
                LocalDateTime.of(2010, Month.DECEMBER, 31, 0, 0),
                LocalDateTime.of(2010, Month.DECEMBER, 31, 8, 0));

        testFridayToSaturdayQuartz(
                LocalDateTime.of(2010, Month.DECEMBER, 31, 9, 0),
                LocalDateTime.of(2011, Month.JANUARY, 1, 8, 0));

        testFridayToSaturdayCron4j(
                LocalDateTime.of(2010, Month.DECEMBER, 31, 0, 0),
                LocalDateTime.of(2010, Month.DECEMBER, 31, 8, 0));

        testFridayToSaturdayCron4j(
                LocalDateTime.of(2010, Month.DECEMBER, 31, 9, 0),
                LocalDateTime.of(2011, Month.JANUARY, 1, 8, 0));
    }

    private void testFridayToSaturdayQuartz(final LocalDateTime startDate, final LocalDateTime expectedNextExecution) {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        final Cron quartzCron = parser.parse("0 0 8 ? * FRI-SAT");
        checkNextExecution(startDate, expectedNextExecution, quartzCron);
    }

    private void testFridayToSaturdayCron4j(final LocalDateTime startDate, final LocalDateTime expectedNextExecution) {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J));
        final Cron quartzCron = parser.parse("0 8 * * FRI-SAT");
        checkNextExecution(startDate, expectedNextExecution, quartzCron);
    }

    private void testWorkdays8Quartz(final LocalDateTime startDate, final LocalDateTime expectedNextExecution) {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
        final Cron quartzCron = parser.parse("0 0 8 ? * MON-FRI");
        checkNextExecution(startDate, expectedNextExecution, quartzCron);
    }

    private void testWorkdays8Cron4j(final LocalDateTime startDate, final LocalDateTime expectedNextExecution) {
        final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J));
        final Cron quartzCron = parser.parse("0 8 * * MON-FRI");
        checkNextExecution(startDate, expectedNextExecution, quartzCron);
    }

    private void checkNextExecution(final LocalDateTime startDate, final LocalDateTime expectedNextExecution, final Cron cron) {
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        final ZonedDateTime zonedDateTime = ZonedDateTime.of(startDate, ZoneId.systemDefault());
        final Optional<ZonedDateTime> next = executionTime.nextExecution(zonedDateTime);
        assert (next.isPresent());
        assertEquals(ZonedDateTime.of(expectedNextExecution, ZoneId.systemDefault()), next.get());
    }
}
