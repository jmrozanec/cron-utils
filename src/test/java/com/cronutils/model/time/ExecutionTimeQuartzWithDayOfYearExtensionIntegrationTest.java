package com.cronutils.model.time;

import com.cronutils.model.definition.CronConstraintsFactory;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.definition.TestCronDefinitionsFactory;
import com.cronutils.parser.CronParser;
import org.junit.Before;
import org.junit.Test;
import org.threeten.bp.*;
import org.threeten.bp.temporal.ChronoUnit;

import static org.junit.Assert.*;

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
public class ExecutionTimeQuartzWithDayOfYearExtensionIntegrationTest {
    private static final String BI_WEEKLY_STARTING_WITH_FIRST_DAY_OF_YEAR = "0 0 0 ? * ? * 1/14";
    private static final String FIRST_QUATER_BI_WEEKLY_STARTING_WITH_FIRST_DAY_OF_YEAR = "0 0 0 ? 1-3 ? * 1/14";
    private static final String WITHOUT_DAY_OF_YEAR = "0 0 0 * * ? *";
    private static final String WITHOUT_SPECIFIC_DAY_OF_YEAR = "0 0 0 * * ? * ?";
    private CronParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new CronParser(TestCronDefinitionsFactory.withDayOfYearDefinition());
    }

    @Test
    public void testForCron() throws Exception {
        assertEquals(ExecutionTime.class, ExecutionTime.forCron(parser.parse(BI_WEEKLY_STARTING_WITH_FIRST_DAY_OF_YEAR)).getClass());
        assertEquals(ExecutionTime.class, ExecutionTime.forCron(parser.parse(FIRST_QUATER_BI_WEEKLY_STARTING_WITH_FIRST_DAY_OF_YEAR)).getClass());
        assertEquals(ExecutionTime.class, ExecutionTime.forCron(parser.parse(WITHOUT_DAY_OF_YEAR)).getClass());
        assertEquals(ExecutionTime.class, ExecutionTime.forCron(parser.parse(WITHOUT_SPECIFIC_DAY_OF_YEAR)).getClass());
    }

    //@Test TODO #Issue #184
    public void testNextExecutionEveryTwoWeeksStartingWithFirstDayOfYear() throws Exception {
        ZonedDateTime now = truncateToDays(ZonedDateTime.now());
        int dayOfYear = now.getDayOfYear();
        int dayOfMostRecentPeriod = dayOfYear % 14;
        ZonedDateTime expected = dayOfMostRecentPeriod == 1 ? now : now.plusDays(15-dayOfMostRecentPeriod);
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(BI_WEEKLY_STARTING_WITH_FIRST_DAY_OF_YEAR));
        assertEquals(expected, executionTime.nextExecution(now).get());
    }
    
    //@Test TODO #Issue #184
    public void testLastExecutionEveryTwoWeeksStartingWithFirstDayOfYear() throws Exception {
        ZonedDateTime now = truncateToDays(ZonedDateTime.now());
        int dayOfYear = now.getDayOfYear();
        int dayOfMostRecentPeriod = dayOfYear % 14;
        ZonedDateTime expected = dayOfMostRecentPeriod == 1 ? now : now.minusDays(dayOfMostRecentPeriod-1);
        ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(BI_WEEKLY_STARTING_WITH_FIRST_DAY_OF_YEAR));
        assertEquals(expected, executionTime.lastExecution(now).get());
    }
    
    private static ZonedDateTime truncateToDays(ZonedDateTime dateTime){
        return dateTime.truncatedTo(ChronoUnit.DAYS);
    }
}
