package com.cronutils.mapper;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
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
public class CronMapperIntegrationTest {

    @Test
    public void testSpecificTimeCron4jToQuartz(){
        String expression = "30 8 10 6 *";
        String expected = String.format("0 %s *", expression);
        assertEquals(expected, createFromCron4jToQuartz().map(cron4jParser().parse(expression)).asString());
    }

    @Test
    public void testMoreThanOneInstanceCron4jToQuartz(){
        String expression = "0 11,16 * * *";
        String expected = String.format("0 %s *", expression);
        assertEquals(expected, createFromCron4jToQuartz().map(cron4jParser().parse(expression)).asString());
    }

    @Test
    public void testRangeOfTimeCron4jToQuartz(){
        String expression = "0 9-18 * * 1-3";
        String expected = String.format("0 %s *", expression);
        assertEquals(expected, createFromCron4jToQuartz().map(cron4jParser().parse(expression)).asString());
    }

    @Test
    public void testSpecificTimeQuartzToCron4j(){
        String expected = "30 8 10 6 *";
        String expression = String.format("5 %s 1984", expected);
        assertEquals(expected, createFromQuartzToCron4j().map(quartzParser().parse(expression)).asString());
    }

    @Test
    public void testMoreThanOneInstanceQuartzToCron4j(){
        String expected = "0 11,16 * * *";
        String expression = String.format("5 %s 1984", expected);
        assertEquals(expected, createFromQuartzToCron4j().map(quartzParser().parse(expression)).asString());
    }

    @Test
    public void testRangeOfTimeQuartzToCron4j(){
        String expected = "0 9-18 * * 1-3";
        String expression = String.format("5 %s 1984", expected);
        assertEquals(expected, createFromQuartzToCron4j().map(quartzParser().parse(expression)).asString());
    }

    private CronParser cron4jParser(){
        return new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J));
    }

    private CronParser quartzParser(){
        return new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    private CronMapper createFromCron4jToQuartz(){
        return new CronMapper(
                CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J),
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    private CronMapper createFromQuartzToCron4j(){
        return new CronMapper(
                CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ),
                CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J));
    }
}
