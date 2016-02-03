package com.cronutils.model.time;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
public class ExecutionTimeCron4jIntegrationTest {
    private CronParser cron4jCronParser;
    private static final String EVERY_MONDAY_AT_18 = "0 18 * * 1";
    private static final String EVERY_15_MINUTES = "0/15 * * * *";
    private static final String EVERY_2_HOURS = "0 0/2 * * *";
    private static final String EVERY_WEEKDAY_AT_6 = "0 6 * * MON-FRI";
	private static final Logger log = LoggerFactory.getLogger(ExecutionTimeCron4jIntegrationTest.class);

    @Before
    public void setUp(){
        cron4jCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J));
    }

    @Test
    public void testForCron() throws Exception {
        assertEquals(ExecutionTime.class, ExecutionTime.forCron(cron4jCronParser.parse(EVERY_MONDAY_AT_18)).getClass());
    }

    /**
     * Issue #37: nextExecution not calculating correct time
     */
    @Test
    public void testEveryWeekdayAt6() throws Exception {
        DateTime lastRun = new DateTime();
        ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse(EVERY_WEEKDAY_AT_6));
        
        // iterate through the next 8 days so we roll over for a week
        // and make sure the next run time is always in the future from the prior run time
		for (int i = 0; i < 8; i++) {
			
			DateTime nextRun = executionTime.nextExecution(lastRun);
			log.debug("LastRun = [{}]", lastRun);
			log.debug("NextRun = [{}]", nextRun);

			assertNotEquals(6, nextRun.getDayOfWeek());
			assertNotEquals(7, nextRun.getDayOfWeek());
			assertTrue(lastRun.isBefore(nextRun));
            lastRun = lastRun.plusDays(1);
		}
    }

    /**
     * Issue #37: nextExecution not calculating correct time
     */
    @Test
    public void testEvery2Hours() throws Exception {
        DateTime lastRun = new DateTime();
        ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse(EVERY_2_HOURS));
        
        // iterate through the next 36 hours so we roll over the to the next day
        // and make sure the next run time is always in the future from the prior run time
		for (int i = 0; i < 36; i++) {
			
			DateTime nextRun = executionTime.nextExecution(lastRun);
			log.debug("LastRun = [{}]", lastRun);
			log.debug("NextRun = [{}]", nextRun);

			assertTrue(nextRun.getHourOfDay() % 2 == 0);
			assertTrue(lastRun.isBefore(nextRun));
            lastRun = lastRun.plusHours(1);
		}
    }

    /**
     * Issue #37:  nextExecution not calculating correct time
     */
    @Test
    public void testEvery15Minutes() throws Exception {
        DateTime lastRun = new DateTime();
        ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse(EVERY_15_MINUTES));
        
        // iterate through the next 75 minutes so we roll over the top of the hour
        // and make sure the next run time is always in the future from the prior run time
		for (int i = 0; i < 75; i++) {
			
			DateTime nextRun = executionTime.nextExecution(lastRun);
			log.debug("LastRun = [{}]", lastRun);
			log.debug("NextRun = [{}]", nextRun);

			assertTrue(nextRun.getMinuteOfHour() % 15 == 0);
			assertTrue(lastRun.isBefore(nextRun));
            lastRun = lastRun.plusMinutes(1);
		}
    }
    
    /**
     * Issue #26: bug 1: if day of week specified, always from day of month is not considered.
     */
    @Test
    public void testDayOfWeekOverridesAlwaysAtDayOfMonth() throws Exception {
        DateTime now = DateTime.now();
        ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse(EVERY_MONDAY_AT_18));
        DateTime next = executionTime.nextExecution(now);
        assertEquals(1, next.getDayOfWeek());
        assertTrue(now.isBefore(next));
    }

    /**
     * Issue #26: bug 1: if day of week specified, always from day of month is not considered.
     */
    @Test
    public void testDayOfMonthOverridesAlwaysAtDayOfWeek() throws Exception {
        DateTime now = DateTime.now();
        ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse("0 18 1 * *"));
        DateTime next = executionTime.nextExecution(now);
        assertEquals(1, next.getDayOfMonth());
        assertTrue(now.isBefore(next));
    }

    /**
     * Issue #26: bug 2: nextNext should be greater than next, not the same value.
     */
    @Test
    public void testNextExecutionOverNextExecution() throws Exception {
        DateTime now = DateTime.now();
        ExecutionTime executionTime = ExecutionTime.forCron(cron4jCronParser.parse(EVERY_MONDAY_AT_18));
        DateTime next = executionTime.nextExecution(now);
        DateTime nextNext = executionTime.nextExecution(next);
        assertTrue(now.isBefore(next));
        assertTrue(next.isBefore(nextNext));
    }
}
