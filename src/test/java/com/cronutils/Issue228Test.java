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
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertEquals;

public class Issue228Test {
    /**
     * This is the UNIX cron definition with a single modification to match both Day Of Week and Day Of Month
     */
    private CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
            .withMinutes().and()
            .withHours().and()
            .withDayOfMonth().and()
            .withMonth().and()
            .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).and()
            .enforceStrictRanges()
            .matchDayOfWeekAndDayOfMonth() // the regular UNIX cron definition permits matching either DoW or DoM
            .instance();

    @Test
    public void testFirstMondayOfTheMonthNextExecution() {
        CronParser parser = new CronParser(cronDefinition);

        // This is 9am on a day between the 1st and 7th which is a Monday (in this case it should be Oct 2
        Cron myCron = parser.parse("0 9 1-7 * 1");
        ZonedDateTime time = ZonedDateTime.parse("2017-09-29T14:46:01.166-07:00");

        Optional<ZonedDateTime> onext = ExecutionTime.forCron(myCron).nextExecution(time);
        ZonedDateTime next = onext.orElse(null);

        assertEquals(ZonedDateTime.parse("2017-10-02T09:00-07:00"), next);
    }

    @Test
    public void testEveryWeekdayFirstWeekOfMonthNextExecution() {
        CronParser parser = new CronParser(cronDefinition);

        // This is 9am on Mon-Fri day between the 1st and 7th (in this case it should be Oct 2)
        Cron myCron = parser.parse("0 9 1-7 * 1-5");
        ZonedDateTime time = ZonedDateTime.parse("2017-09-29T14:46:01.166-07:00");

        Optional<ZonedDateTime> onext = ExecutionTime.forCron(myCron).nextExecution(time);
        ZonedDateTime next = onext.orElse(null);

        assertEquals(ZonedDateTime.parse("2017-10-02T09:00-07:00"), next);
    }

    @Test
    public void testEveryWeekendFirstWeekOfMonthNextExecution() {
        CronParser parser = new CronParser(cronDefinition);

        // This is 9am on Sat and Sun day between the 1st and 7th (in this case it should be Oct 1)
        Cron myCron = parser.parse("0 9 1-7 * 6-7");
        ZonedDateTime time = ZonedDateTime.parse("2017-09-29T14:46:01.166-07:00");

        Optional<ZonedDateTime> onext = ExecutionTime.forCron(myCron).nextExecution(time);
        ZonedDateTime next = onext.orElse(null);

        assertEquals(ZonedDateTime.parse("2017-10-01T09:00-07:00"), next);
    }

    @Test
    public void testEveryWeekdaySecondWeekOfMonthNextExecution() {
        CronParser parser = new CronParser(cronDefinition);

        // This is 9am on Mon-Fri day between the 8th and 14th (in this case it should be Oct 9 Mon)
        Cron myCron = parser.parse("0 9 8-14 * 1-5");
        ZonedDateTime time = ZonedDateTime.parse("2017-09-29T14:46:01.166-07:00");

        Optional<ZonedDateTime> onext = ExecutionTime.forCron(myCron).nextExecution(time);
        ZonedDateTime next = onext.orElse(null);
        assertEquals(ZonedDateTime.parse("2017-10-09T09:00-07:00"), next);
    }

    @Test
    public void testEveryWeekendForthWeekOfMonthNextExecution() {
        CronParser parser = new CronParser(cronDefinition);

        // This is 9am on Sat and Sun day between the 22nd and 28th (in this case it should be Oct 22)
        Cron myCron = parser.parse("0 9 22-28 * 6-7");
        ZonedDateTime time = ZonedDateTime.parse("2017-09-29T14:46:01.166-07:00");

        Optional<ZonedDateTime> onext = ExecutionTime.forCron(myCron).nextExecution(time);
        ZonedDateTime next = onext.orElse(null);

        assertEquals(ZonedDateTime.parse("2017-10-22T09:00-07:00"), next);
    }
}
