/*
 * Copyright 2014 jmrozanec
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

package com.cronutils.model.time;

import com.cronutils.model.CompositeCron;
import com.cronutils.model.Cron;
import com.cronutils.model.SingleCron;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculates execution time given a cron pattern.
 */
public interface ExecutionTime {

    /**
     * Creates execution time for given Cron.
     *
     * @param cron - Cron instance
     * @return ExecutionTime instance
     */
    static ExecutionTime forCron(final Cron cron) {
        if (cron instanceof SingleCron) {
            final Map<CronFieldName, CronField> fields = cron.retrieveFieldsAsMap();
            final ExecutionTimeBuilder executionTimeBuilder = new ExecutionTimeBuilder(cron);
            for (final CronFieldName name : CronFieldName.values()) {
                if (fields.get(name) != null) {
                    switch (name) {
                        case SECOND:
                            executionTimeBuilder.forSecondsMatching(fields.get(name));
                            break;
                        case MINUTE:
                            executionTimeBuilder.forMinutesMatching(fields.get(name));
                            break;
                        case HOUR:
                            executionTimeBuilder.forHoursMatching(fields.get(name));
                            break;
                        case DAY_OF_WEEK:
                            executionTimeBuilder.forDaysOfWeekMatching(fields.get(name));
                            break;
                        case DAY_OF_MONTH:
                            executionTimeBuilder.forDaysOfMonthMatching(fields.get(name));
                            break;
                        case MONTH:
                            executionTimeBuilder.forMonthsMatching(fields.get(name));
                            break;
                        case YEAR:
                            executionTimeBuilder.forYearsMatching(fields.get(name));
                            break;
                        case DAY_OF_YEAR:
                            executionTimeBuilder.forDaysOfYearMatching(fields.get(name));
                            break;
                        default:
                            break;
                    }
                }
            }
            return executionTimeBuilder.build();
        }
        if (cron instanceof CompositeCron) {
            return new CompositeExecutionTime(((CompositeCron) cron).getCrons().parallelStream().map(ExecutionTime::forCron).collect(Collectors.toList()));
        }

        return new ExecutionTime() {
            @Override
            public Optional<ZonedDateTime> nextExecution(ZonedDateTime date) {
                return Optional.empty();
            }

            @Override
            public Optional<Duration> timeToNextExecution(ZonedDateTime date) {
                return Optional.empty();
            }

            @Override
            public Optional<ZonedDateTime> lastExecution(ZonedDateTime date) {
                return Optional.empty();
            }

            @Override
            public Optional<Duration> timeFromLastExecution(ZonedDateTime date) {
                return Optional.empty();
            }

            @Override
            public boolean isMatch(ZonedDateTime date) {
                return false;
            }
        };
    }

    /**
     * Provide nearest date for next execution.
     *
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Optional ZonedDateTime instance, never null. Contains next execution time or empty.
     */
    Optional<ZonedDateTime> nextExecution(final ZonedDateTime date);

    /**
     * Provide nearest time for next execution.
     *
     * Due to the question #468 we clarify: crons execute on local instance time.
     * See: https://serverfault.com/questions/791713/what-time-zone-is-a-cron-job-using
     * We ask for a ZonedDateTime for two reasons:
     * (i) to provide flexibility on which timezone the cron is being executed
     * (ii) to be able to reproduce issues regardless of our own local time (e.g.: daylight savings, etc.)
     *
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Duration instance, never null. Time to next execution.
     */
    Optional<Duration> timeToNextExecution(final ZonedDateTime date);

    /**
     * Provide nearest date for last execution.
     *
     * Due to the question #468 we clarify: crons execute on local instance time.
     * See: https://serverfault.com/questions/791713/what-time-zone-is-a-cron-job-using
     * We ask for a ZonedDateTime for two reasons:
     * (i) to provide flexibility on which timezone the cron is being executed
     * (ii) to be able to reproduce issues regardless of our own local time (e.g.: daylight savings, etc.)
     *
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Optional ZonedDateTime instance, never null. Last execution time or empty.
     */
    Optional<ZonedDateTime> lastExecution(final ZonedDateTime date);

    /**
     * Provide nearest time from last execution.
     *
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return Duration instance, never null. Time from last execution.
     */
    Optional<Duration> timeFromLastExecution(final ZonedDateTime date);

    /**
     * Provide feedback if a given date matches the cron expression.
     *
     * @param date - ZonedDateTime instance. If null, a NullPointerException will be raised.
     * @return true if date matches cron expression requirements, false otherwise.
     */
    boolean isMatch(ZonedDateTime date);

    /**
     * Provide count of times cron expression would execute between given start and end dates
     *
     * @param startDate - Start date. If null, a NullPointerException will be raised.
     * @param endDate - End date. If null, a NullPointerException will be raised.
     * @return count of executions
     */
    default int countExecutions(ZonedDateTime startDate, ZonedDateTime endDate) {
        return getExecutionDates(startDate, endDate).size();
    }

    /**
     * Provide date times when cron expression would execute between given start and end dates.
     * End date should be after start date. Otherwise, IllegalArgumentException is raised
     *
     * @param startDate - Start date. If null, a NullPointerException will be raised.
     * @param endDate - End date. If null, a NullPointerException will be raised.
     * @return list of date times
     */
    default List<ZonedDateTime> getExecutionDates(ZonedDateTime startDate, ZonedDateTime endDate) {
        if (endDate.equals(startDate) || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate should take place later in time than startDate");
        }
        List<ZonedDateTime> executions = new ArrayList<>();
        ZonedDateTime nextExecutionDate = nextExecution(startDate).orElse(null);

        if (nextExecutionDate == null) return Collections.emptyList();
        while(nextExecutionDate != null && (nextExecutionDate.isBefore(endDate) || nextExecutionDate.equals(endDate))){
            executions.add(nextExecutionDate);
            nextExecutionDate = nextExecution(nextExecutionDate).orElse(null);
        }
        return executions;
    }
}
