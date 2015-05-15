package com.cronutils.model.time;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
public class ExecutionTimeIntegrationTest {
    private CronParser quartzCronParser;
    private static final String EVERY_SECOND = "* * * * * * *";

    @Before
    public void setUp(){
        quartzCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    @Test
    public void testForCron() throws Exception {
        assertEquals(ExecutionTime.class, ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND)).getClass());
    }

    @Test
    public void testNextExecutionEverySecond() throws Exception {
        DateTime now = DateTime.now();
        DateTime expected = truncateToSeconds(now.plusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(expected, executionTime.nextExecution(now));
    }

    @Test
    public void testTimeToNextExecution() throws Exception {
        DateTime now = DateTime.now();
        DateTime expected = truncateToSeconds(now.plusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(new Interval(now, expected).toDuration(), executionTime.timeToNextExecution(now));
    }

    @Test
    public void testLastExecution() throws Exception {
        DateTime now = DateTime.now();
        DateTime expected = truncateToSeconds(now.minusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(expected, executionTime.lastExecution(now));
    }

    @Test
    public void testTimeFromLastExecution() throws Exception {
        DateTime now = DateTime.now();
        DateTime expected = truncateToSeconds(now.minusSeconds(1));
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(EVERY_SECOND));
        assertEquals(new Interval(expected, now).toDuration(), executionTime.timeFromLastExecution(now));
    }

    /**
     * Test for issue #9
     * https://github.com/jmrozanec/cron-utils/issues/9
     * Reported case: If you write a cron expression that contains a month or day of week, nextExection() ignores it.
     * Expected: should not ignore month or day of week field
     */
    @Test
    public void testDoesNotIgnoreMonthOrDayOfWeek(){
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser cronParser = new CronParser(cronDefinition);
        //seconds, minutes, hours, dayOfMonth, month, dayOfWeek
        ExecutionTime executionTime = ExecutionTime.forCron(cronParser.parse("0 11 11 11 11 ?"));
        DateTime now = new DateTime(2015, 4, 15, 0, 0, 0);
        DateTime whenToExecuteNext = executionTime.nextExecution(now);
        assertEquals(2015, whenToExecuteNext.getYear());
        assertEquals(11, whenToExecuteNext.getMonthOfYear());
        assertEquals(1, whenToExecuteNext.getDayOfMonth());
        assertEquals(11, whenToExecuteNext.getHourOfDay());
        assertEquals(11, whenToExecuteNext.getMinuteOfHour());
        assertEquals(0, whenToExecuteNext.getSecondOfMinute());
    }

    @Test
    public void testHourlyIntervalTimeFromLastExecution() throws Exception {
        DateTime now = DateTime.now();
        DateTime previousHour = now.minusHours(1);
        String quartzCronExpression = String.format("0 0 %s * * ?", previousHour.getHourOfDay());
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(quartzCronExpression));
        
        assertTrue(executionTime.timeFromLastExecution(now).getStandardMinutes() <= 120);
    }

    private DateTime truncateToSeconds(DateTime dateTime){
        return new DateTime(
                dateTime.getYear(),
                dateTime.getMonthOfYear(),
                dateTime.getDayOfMonth(),
                dateTime.getHourOfDay(),
                dateTime.getMinuteOfHour(),
                dateTime.getSecondOfMinute()
        );
    }
}