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

import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Issue223Test {

    /**
     * Issue #223: for dayOfWeek value == 3 && division of day, nextExecution do not return correct results.
     */
    @Test
    public void testEveryWednesdayOfEveryDayNextExecution() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        final CronParser parser = new CronParser(cronDefinition);
        final Cron myCron = parser.parse("* * * * 3");
        ZonedDateTime time = ZonedDateTime.parse("2017-09-05T11:31:55.407-05:00");
        final Optional<ZonedDateTime> nextExecution = ExecutionTime.forCron(myCron).nextExecution(time);
        if (nextExecution.isPresent()) {
            assertEquals(ZonedDateTime.parse("2017-09-06T00:00-05:00"), nextExecution.get());
        } else {
            fail("next execution was not present");
        }

        final Cron myCron2 = parser.parse("* * */1 * 3");
        time = ZonedDateTime.parse("2017-09-05T11:31:55.407-05:00");
        final Optional<ZonedDateTime> nextExecution2 = ExecutionTime.forCron(myCron2).nextExecution(time);
        if (nextExecution2.isPresent()) {
            assertEquals(ZonedDateTime.parse("2017-09-06T00:00-05:00"), nextExecution2.get());
        } else {
            fail("next execution was not present");
        }
    }

}
