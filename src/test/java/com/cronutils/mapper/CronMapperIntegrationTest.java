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

package com.cronutils.mapper;

import java.util.Arrays;

import org.junit.Test;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CronMapperIntegrationTest {

    @Test
    public void testSpecificTimeCron4jToQuartz() {
        assertEquals("0 30 8 10 6 ? *", CronMapper.fromCron4jToQuartz().map(cron4jParser().parse("30 8 10 6 *")).asString());
    }

    @Test
    public void testMoreThanOneInstanceCron4jToQuartz() {
        assertEquals("0 0 11,16 * * ? *", CronMapper.fromCron4jToQuartz().map(cron4jParser().parse("0 11,16 * * *")).asString());
    }

    @Test
    public void testRangeOfTimeCron4jToQuartz() {
        final String expression = "0 9-18 * * 1-3";
        final String expected = "0 0 9-18 ? * 2-4 *";
        assertEquals(expected, CronMapper.fromCron4jToQuartz().map(cron4jParser().parse(expression)).asString());
    }

    @Test
    public void testSpecificTimeQuartzToCron4j() {
        final String expression = "5 30 8 10 6 ? 1984";
        assertEquals("30 8 10 6 *", CronMapper.fromQuartzToCron4j().map(quartzParser().parse(expression)).asString());
    }

    @Test
    public void testMoreThanOneInstanceQuartzToCron4j() {
        final String expression = "5 0 11,16 * * ? 1984";
        assertEquals("0 11,16 * * *", CronMapper.fromQuartzToCron4j().map(quartzParser().parse(expression)).asString());
    }

    @Test
    public void testRangeOfTimeQuartzToCron4j() {
        final String expected = "0 9-18 * * 0-2";
        final String expression = "5 0 9-18 ? * 1-3 1984";
        assertEquals(expected, CronMapper.fromQuartzToCron4j().map(quartzParser().parse(expression)).asString());
    }

    @Test
    public void testRangeOfTimeQuartzToSpring() {
        final String expected = "5 0 9-18 ? * 1-3";
        final String expression = "5 0 9-18 ? * 1-3 1984";
        assertEquals(expected, CronMapper.fromQuartzToSpring().map(quartzParser().parse(expression)).asString());
    }

    @Test
    public void testDaysOfWeekUnixToQuartz() {
        final String input = "* * * * 3,5-6";
        final String expected = "0 * * ? * 4,6-7 *";
        assertEquals(expected, CronMapper.fromUnixToQuartz().map(unixParser().parse(input)).asString());
    }

    /**
     * Issue #36, #56: Unix to Quartz not accurately mapping every minute pattern
     * or patterns that involve every day of month and every day of week.
     */
    @Test
    public void testEveryMinuteUnixToQuartz() {
        final String input = "* * * * *";
        final String expected1 = "0 * * * * ? *";
        final String expected2 = "0 * * ? * * *";
        final String mapping = CronMapper.fromUnixToQuartz().map(unixParser().parse(input)).asString();
        assertTrue(
                String.format("Expected [%s] or [%s] but got [%s]", expected1, expected2, mapping),
                Arrays.asList(expected1, expected2).contains(mapping)
        );
    }

    /**
     * Issue #36, #56: Unix to Quartz not accurately mapping every minute pattern
     * or patterns that involve every day of month and every day of week.
     */
    @Test
    public void testUnixToQuartzQuestionMarkRequired() {
        final String input = "0 0 * * 1";
        final String expected = "0 0 0 ? * 2 *";
        final String mapping = CronMapper.fromUnixToQuartz().map(unixParser().parse(input)).asString();
        assertEquals(String.format("Expected [%s] but got [%s]", expected, mapping), expected, mapping);
    }

    private CronParser cron4jParser() {
        return new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J));
    }

    private CronParser quartzParser() {
        return new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    private CronParser unixParser() {
        return new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
    }
}
