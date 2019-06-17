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

    private static final String TEST_DATE = "2017-09-29T14:46:01.166-07:00";

    /**
     * This is the UNIX cron definition with a single modification to match both Day Of Week and Day Of Month.
     */
    private final CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
            .withMinutes().withStrictRange().and()
            .withHours().withStrictRange().and()
            .withDayOfMonth().withStrictRange().and()
            .withMonth().withStrictRange().and()
            .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).withStrictRange().and()
            .matchDayOfWeekAndDayOfMonth() // the regular UNIX cron definition permits matching either DoW or DoM
            .instance();

    @Test
    public void testFirstMondayOfTheMonthNextExecution() {
        final CronParser parser = new CronParser(cronDefinition);

        // This is 9am on a day between the 1st and 7th which is a Monday (in this case it should be Oct 2
        final Cron myCron = parser.parse("0 9 1-7 * 1");
        final ZonedDateTime time = ZonedDateTime.parse(TEST_DATE);
        assertEquals(ZonedDateTime.parse("2017-10-02T09:00-07:00"), getNextExecutionTime(myCron, time));
    }

    @Test
    public void testEveryWeekdayFirstWeekOfMonthNextExecution() {
        final CronParser parser = new CronParser(cronDefinition);

        // This is 9am on Mon-Fri day between the 1st and 7th (in this case it should be Oct 2)
        final Cron myCron = parser.parse("0 9 1-7 * 1-5");
        final ZonedDateTime time = ZonedDateTime.parse(TEST_DATE);
        assertEquals(ZonedDateTime.parse("2017-10-02T09:00-07:00"), getNextExecutionTime(myCron, time));
    }

    @Test
    public void testEveryWeekendFirstWeekOfMonthNextExecution() {
        final CronParser parser = new CronParser(cronDefinition);

        // This is 9am on Sat and Sun day between the 1st and 7th (in this case it should be Oct 1)
        final Cron myCron = parser.parse("0 9 1-7 * 6-7");
        final ZonedDateTime time = ZonedDateTime.parse(TEST_DATE);
        assertEquals(ZonedDateTime.parse("2017-10-01T09:00-07:00"), getNextExecutionTime(myCron, time));
    }

    @Test
    public void testEveryWeekdaySecondWeekOfMonthNextExecution() {
        final CronParser parser = new CronParser(cronDefinition);

        // This is 9am on Mon-Fri day between the 8th and 14th (in this case it should be Oct 9 Mon)
        final Cron myCron = parser.parse("0 9 8-14 * 1-5");
        final ZonedDateTime time = ZonedDateTime.parse(TEST_DATE);
        assertEquals(ZonedDateTime.parse("2017-10-09T09:00-07:00"), getNextExecutionTime(myCron, time));
    }

    @Test
    public void testEveryWeekendForthWeekOfMonthNextExecution() {
        final CronParser parser = new CronParser(cronDefinition);

        // This is 9am on Sat and Sun day between the 22nd and 28th (in this case it should be Oct 22)
        final Cron myCron = parser.parse("0 9 22-28 * 6-7");
        final ZonedDateTime time = ZonedDateTime.parse(TEST_DATE);
        assertEquals(ZonedDateTime.parse("2017-10-22T09:00-07:00"), getNextExecutionTime(myCron, time));
    }

    private ZonedDateTime getNextExecutionTime(final Cron cron, final ZonedDateTime time) {
        final Optional<ZonedDateTime> nextExecution = ExecutionTime.forCron(cron).nextExecution(time);
        if (nextExecution.isPresent()) {
            return nextExecution.get();
        } else {
            throw new NullPointerException("next execution was not present");
        }
    }
}
