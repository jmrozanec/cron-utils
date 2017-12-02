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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.fail;

public class Issue143Test {

    private static final String LAST_EXECUTION_NOT_PRESENT_ERROR = "last execution was not present";
    private CronParser parser;
    private ZonedDateTime currentDateTime;

    @Before
    public void setUp() {
        // Make sure that current date is before Dec-31
        currentDateTime = ZonedDateTime.of(LocalDateTime.of(2016, 12, 20, 12, 0),
                ZoneId.systemDefault());

        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    @Test
    public void testCase1() {
        ExecutionTime et = ExecutionTime.forCron(parser.parse("0 0 12 31 12 ? *"));
        Optional<ZonedDateTime> olast = et.lastExecution(currentDateTime);
        ZonedDateTime last = olast.orElse(null);

        ZonedDateTime expected = ZonedDateTime.of(LocalDateTime.of(2015, 12, 31, 12, 0),
                ZoneId.systemDefault());
        Assert.assertEquals(expected, last);
    }

    @Test
    public void testCase2() {
        final ExecutionTime et = ExecutionTime.forCron(parser.parse("0 0 12 ? 12 SAT#5 *"));
        final Optional<ZonedDateTime> lastExecution = et.lastExecution(currentDateTime);
        if (lastExecution.isPresent()) {
            final ZonedDateTime expected = ZonedDateTime.of(LocalDateTime.of(2012, 12, 29, 12, 0), ZoneId.systemDefault());
            Assert.assertEquals(expected, lastExecution.get());
        } else {
            fail(LAST_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    @Test
    public void testCase3() {
        final ExecutionTime et = ExecutionTime.forCron(parser.parse("0 0 12 31 1/1 ? *"));
        final Optional<ZonedDateTime> lastExecution = et.lastExecution(currentDateTime);
        if (lastExecution.isPresent()) {
            final ZonedDateTime expected = ZonedDateTime.of(LocalDateTime.of(2016, 10, 31, 12, 0), ZoneId.systemDefault());
            Assert.assertEquals(expected, lastExecution.get());
        } else {
            fail(LAST_EXECUTION_NOT_PRESENT_ERROR);
        }
    }

    @Test
    public void testCase4() {
        final ExecutionTime et = ExecutionTime.forCron(parser.parse("0 0 12 ? 1/1 SAT#5 *"));
        final Optional<ZonedDateTime> lastExecution = et.lastExecution(currentDateTime);
        if (lastExecution.isPresent()) {
            final ZonedDateTime expected = ZonedDateTime.of(LocalDateTime.of(2016, 10, 29, 12, 0), ZoneId.systemDefault());
            Assert.assertEquals(expected, lastExecution.get());
        } else {
            fail(LAST_EXECUTION_NOT_PRESENT_ERROR);
        }
    }
}
