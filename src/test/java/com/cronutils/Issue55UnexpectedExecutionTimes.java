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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronConstraint;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertEquals;

public class Issue55UnexpectedExecutionTimes {

    private static final Logger LOGGER = LoggerFactory.getLogger(Issue55UnexpectedExecutionTimes.class);
    private CronDefinition cronDefinition;

    /**
     * Setup.
     */
    @Before
    public void setUp() {
        cronDefinition = CronDefinitionBuilder.defineCron()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth()
                .supportsHash().supportsL().supportsW().supportsQuestionMark().and()
                .withMonth().and()
                .withDayOfWeek()//Monday=1
                .withIntMapping(7, 0) //we support non-standard non-zero-based numbers!
                .supportsHash().supportsL().supportsW().supportsQuestionMark().and()
                .withYear().optional().and()
                .withCronValidation(
                        //both a day-of-week AND a day-of-month parameter should fail for this case; otherwise returned values are correct
                        new CronConstraint("Both, a day-of-week AND a day-of-month parameter, are not supported.") {

                            private static final long serialVersionUID = -5934767434702909825L;

                            @Override
                            public boolean validate(final Cron cron) {
                                if (!(cron.retrieve(CronFieldName.DAY_OF_MONTH).getExpression() instanceof QuestionMark)) {
                                    return cron.retrieve(CronFieldName.DAY_OF_WEEK).getExpression() instanceof QuestionMark;
                                } else {
                                    return !(cron.retrieve(CronFieldName.DAY_OF_WEEK).getExpression() instanceof QuestionMark);
                                }
                            }
                        })
                .instance();
    }

    /**
     * Test.
     */
    @Test
    public void testOnceEveryThreeDaysNoInstantsWithinTwoDays() {
        LOGGER.debug("TEST1 - expecting 0 instants");
        final ZonedDateTime startTime = ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        final ZonedDateTime endTime = startTime.plusDays(2);
        final CronParser parser = new CronParser(cronDefinition);
        final Cron cron = parser.parse("0 0 */3 * ?");
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        final List<Instant> instants = getInstants(executionTime, startTime, endTime);
        LOGGER.debug("instants.size() == {}", instants.size());
        LOGGER.debug("instants: {}", instants);
        assertEquals(0, instants.size());
    }

    /**
     * Test.
     */
    @Test
    public void testOnceAMonthTwelveInstantsInYear() {
        LOGGER.debug("TEST2 - expecting 12 instants");
        final ZonedDateTime startTime = ZonedDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        final ZonedDateTime endTime = startTime.plusYears(1);
        final CronParser parser = new CronParser(cronDefinition);
        final Cron cron = parser.parse("0 12 L * ?");
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        final List<Instant> instants = getInstants(executionTime, startTime, endTime);
        LOGGER.debug("instants.size() == {}", instants.size());
        LOGGER.debug("instants: {}", instants);
        assertEquals(12, instants.size());
    }

    private List<Instant> getInstants(final ExecutionTime executionTime, final ZonedDateTime startTime, final ZonedDateTime endTime) {
        final List<Instant> instantList = new ArrayList<>();
        final Optional<ZonedDateTime> startTimeExecution = executionTime.nextExecution(startTime);
        if (startTimeExecution.isPresent()) {
            ZonedDateTime next = startTimeExecution.get();
            while (next.isBefore(endTime)) {
                final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(next);
                instantList.add(next.toInstant());
                if (nextExecution.isPresent()) {
                    next = nextExecution.get();
                } else {
                    throw new NullPointerException("next execution is not present");
                }
            }
            return instantList;
        } else {
            throw new NullPointerException("starttime execution was not present");
        }
    }

}
