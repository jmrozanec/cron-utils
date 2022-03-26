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

package com.cronutils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronConstraintsFactory;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;


class Issue503Test {

    /** Note: Cron-Default is 1970 */
    private static final int VALID_YEAR_MIN      = 1900;
    private static final int VALID_YEAR_MAX      = 2099;
    private static final int LEAP_YEAR_DAY_COUNT = 366;

    private CronParser parser;

    @BeforeEach
    void setUp() {
        final CronDefinition cronDefinitionWithDayOfYearSupport = CronDefinitionBuilder.defineCron().withSeconds().and().withMinutes().and().withHours().and().withDayOfMonth().withValidRange(1, 31).supportsL().supportsW().supportsLW().supportsQuestionMark().and().withMonth().and().withDayOfWeek().withValidRange(1, 7).withMondayDoWValue(2).supportsHash().supportsL().supportsQuestionMark().and().withYear().withValidRange(VALID_YEAR_MIN, VALID_YEAR_MAX).optional().and().withDayOfYear().supportsQuestionMark().withValidRange(1, LEAP_YEAR_DAY_COUNT).optional().and().withCronValidation(CronConstraintsFactory.ensureEitherDayOfYearOrMonth()).withCronValidation(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth()).instance();
        parser = new CronParser(cronDefinitionWithDayOfYearSupport);
    }


    @Test
    void testCustomQuarterlySchedule() {
        final String customQuaterlyStartingWithDay47OfYear = "0 0 0 ? * ? * 47/91";
        final LocalDateTime[] expectedExecutionTimes = IntStream.range(0, 4).mapToObj(i -> LocalDateTime.of(2017, 1, 1, 0, 0).withDayOfYear(47 + i * 91)).toArray(LocalDateTime[]::new);
        final LocalDateTime[] scheduledDates = executionTimesFor(customQuaterlyStartingWithDay47OfYear, expectedExecutionTimes[0], expectedExecutionTimes[expectedExecutionTimes.length - 1].plusDays(1)).stream().toArray(LocalDateTime[]::new);

        assertArrayEquals(expectedExecutionTimes, scheduledDates, "Scheduled dates should match precalculated dates.");
    }


    @Test
    void testCustomQuarterlyScheduleForDesignatedYear2017() {
        final String customQuaterlyStartingWithDay47OfYear = "0 0 0 ? * ? 2017 47/91";
        final LocalDateTime[] expectedExecutionTimes = IntStream.range(0, 4).mapToObj(i -> LocalDateTime.of(2017, 1, 1, 0, 0).withDayOfYear(47 + i * 91)).toArray(LocalDateTime[]::new);
        final LocalDateTime[] scheduledDates = executionTimesFor(customQuaterlyStartingWithDay47OfYear, expectedExecutionTimes[0], expectedExecutionTimes[expectedExecutionTimes.length - 1].plusDays(1)).stream().toArray(LocalDateTime[]::new);

        assertArrayEquals(expectedExecutionTimes, scheduledDates, "Scheduled dates should match precalculated dates.");
    }


    /**
     * The list of scheduled dates defined by the given cron expression included in
     * the denoted interval.
     * 
     * @param xquartzCronExpression The extended Quartz cron expression to use.
     * @param start The start date to use, inclusive.
     * @param end The end date to use, exclusive.
     * @return the list of scheduled dates, may be empty. Never {@code null}.
     */
    private List<LocalDateTime> executionTimesFor(final String xquartzCronExpression, final LocalDateTime start, final LocalDateTime end) {
        //Preconditions.checkArgument(Objects.requireNonNull(start).isBefore(Objects.requireNonNull(end)));
        final Cron cron = parser.parse(xquartzCronExpression);
        return executionTimesFor(cron, start, end);
    }


    /**
     * The list of scheduled dates defined by the given cron expression included in
     * the denoted interval.
     *
     * @param cron The cron to use.
     * @param start The start date to use, inclusive.
     * @param end The end date to use, exclusive.
     * @return the list of scheduled dates, may be empty. Never {@code null}.
     */
    private List<LocalDateTime> executionTimesFor(final Cron cron, final LocalDateTime start, final LocalDateTime end) {

        final ExecutionTime cronExecutionTime = ExecutionTime.forCron(cron);

        final ArrayList<LocalDateTime> scheduledDates = new ArrayList<>();
        ZonedDateTime nextScheduledDate = ZonedDateTime.of(start, ZoneId.systemDefault()).minusSeconds(1);
        final ZonedDateTime upperBoundExclusive = ZonedDateTime.of(end, ZoneId.systemDefault());
        do {
            nextScheduledDate = cronExecutionTime.nextExecution(nextScheduledDate).orElse(null);

            if (nextScheduledDate == null || !nextScheduledDate.isBefore(upperBoundExclusive))
                break;

            scheduledDates.add(nextScheduledDate.toLocalDateTime());
        } while (true);

        return Collections.unmodifiableList(scheduledDates);
    }
}
