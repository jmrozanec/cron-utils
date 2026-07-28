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

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.cronutils.model.CronType.CRON4J;
import static com.cronutils.model.CronType.QUARTZ;
import static com.cronutils.model.CronType.SPRING;
import static com.cronutils.model.CronType.UNIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CronMapperIntegrationTest {
    static Stream<Arguments> cronExpressions() {
        return Stream.of(
                Arguments.of(CRON4J, CronMapper.fromCron4jToQuartz(), "0 11,16 * * *", "0 0 11,16 * * ? *"),
                Arguments.of(CRON4J, CronMapper.fromCron4jToQuartz(), "0 9-18 * * 1-3", "0 0 9-18 ? * 2-4 *"),
                Arguments.of(CRON4J, CronMapper.fromCron4jToQuartz(), "30 8 10 6 *", "0 30 8 10 6 ? *"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToCron4j(), "0 0 0 ? * 5#1", "0 0 * * 4#1"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToCron4j(), "5 0 11,16 * * ? 1984", "0 11,16 * * *"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToCron4j(), "5 0 9-18 ? * 1-3 1984", "0 9-18 * * 0-2"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToCron4j(), "5 30 8 10 6 ? 1984", "30 8 10 6 *"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToSpring(), "0 0 0 ? * 5#1", "0 0 0 ? * 4#1"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToSpring(), "5 0 9-18 ? * 1-3 1984", "5 0 9-18 ? * 0-2"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 1 ? 1/3 FRI#1 *", "0 1 * 1/3 5#1"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 2 ? 1/1 SUN#2", "0 2 * 1/1 0#2"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 8,12 ? * *", "0 8,12 * * *"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "* * 8 ? * SAT", "* 8 * * 6"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 22 ? * MON,TUE,WED,THU,FRI *", "0 22 * * 1,2,3,4,5"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 13 LW * ?", "0 13 LW * *"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 0 1 1-12 ? *", "0 0 1 1-12 *"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 0 1 JAN ? 2099", "0 0 1 1 *"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 0 1,15 * ? *", "0 0 1,15 * *"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 0 19 1-12 ? *", "0 0 19 1-12 *"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 0 21 * ? 2020-2025", "0 0 21 * *"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 0 ? * 3L *", "0 0 * * 2L"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 0 ? 1/1 MON#2 *", "0 0 * 1/1 1#2"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 0,12 ? JAN,MAY,OCT * *", "0 0,12 * 1,5,10 *"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 13 ? 1,4,7,10 6#2", "0 13 * 1,4,7,10 5#2"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 13 ? MAR,JUN,SEP,DEC 1L", "0 13 * 3,6,9,12 0L"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 0 L-10 1-12 ? *", "0 0 L-10 1-12 *"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0/5 * ? 1-12 4#2 *", "0/5 * * 1-12 3#2"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 1 1W * ?", "0 1 1W * *"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 13 ? 1,4,7,10 6#2", "0 13 * 1,4,7,10 5#2"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 15 14,19 ? JAN,MAY,JUL,OCT * *", "15 14,19 * 1,5,7,10 *"),

                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 0 ? * 5#1", "0 0 * * 4#1"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 0 0 L-10 1-12 ? *", "0 0 L-10 1-12 *"),
                Arguments.of(QUARTZ, CronMapper.fromQuartzToUnix(), "0 30 17 ? * 7L *", "30 17 * * 6L"),
                Arguments.of(SPRING, CronMapper.fromSpringToQuartz(), "0 0 0 ? * 5#1", "0 0 0 ? * 6#1 *"),
                Arguments.of(UNIX, CronMapper.fromUnixToQuartz(), "* * * * 3,5-6,*/2,2/3,7/4", "0 * * ? * 4,6-7,*/2,3/3,1/4 *"),
                Arguments.of(UNIX, CronMapper.fromUnixToQuartz(), "0 0 * * 1", "0 0 0 ? * 2 *"),
                Arguments.of(UNIX, CronMapper.fromUnixToQuartz(), "0 0 L * *", "0 0 0 L * ? *"),
                Arguments.of(UNIX, CronMapper.fromUnixToQuartz(), "0 0 L-3 * *", "0 0 0 L-3 * ? *"),
                Arguments.of(UNIX, CronMapper.fromUnixToQuartz(), "0 13 LW * *", "0 0 13 LW * ? *"),
                Arguments.of(UNIX, CronMapper.fromUnixToQuartz(), "0 1 1W * *", "0 0 1 1W * ? *"),
                Arguments.of(UNIX, CronMapper.fromUnixToQuartz(), "0 0 * * 6L", "0 0 0 ? * 7L *")
        );
    }

    @ParameterizedTest
    @MethodSource("cronExpressions")
    void testCronMapping(CronType cronType, CronMapper mapper, String quartzExpression, String expectedExpression) {
        Cron sourceCron = getCron(cronType, quartzExpression);
        String actualCron = mapper.map(sourceCron).asString();
        assertEquals(expectedExpression, actualCron, String.format("Expected [%s] but got [%s]", expectedExpression, actualCron));
    }

    /**
     * Issue #36, #56: Unix to Quartz not accurately mapping every minute pattern
     * or patterns that involve every day of month and every day of week.
     */
    @Test
    void testEveryMinuteUnixToQuartz() {
        final String input = "* * * * *";
        final String expected1 = "0 * * * * ? *";
        final String expected2 = "0 * * ? * * *";
        final String mapping = CronMapper.fromUnixToQuartz().map(unixParser().parse(input)).asString();
        assertTrue(
                Arrays.asList(expected1, expected2).contains(mapping),
                String.format("Expected [%s] or [%s] but got [%s]", expected1, expected2, mapping)
        );
    }

    /**
     * Issue #36, #56: Unix to Quartz not accurately mapping every minute pattern
     * or patterns that involve every day of month and every day of week.
     */
    @Test
    void testUnixToQuartzQuestionMarkRequired() {
        final String input = "0 0 * * 1";
        final String expected = "0 0 0 ? * 2 *";
        final String mapping = CronMapper.fromUnixToQuartz().map(unixParser().parse(input)).asString();
        assertEquals(expected, mapping, String.format("Expected [%s] but got [%s]", expected, mapping));
    }

    private CronParser unixParser() {
        return new CronParser(CronDefinitionBuilder.instanceDefinitionFor(UNIX));
    }

    private Cron getCron(CronType cronType, String quartzExpression) {
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(cronType);
        final CronParser parser = new CronParser(cronDefinition);
        return parser.parse(quartzExpression);
    }
}
