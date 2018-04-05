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
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

//FIXME this test does not check anything
public class OpenIssuesTest {
    private final DateTimeFormatter dfSimple = DateTimeFormatter.ofPattern("hh:mm:ss MM/dd/yyyy a X", Locale.US);
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("hh:mm:ss EEE, MMM dd yyyy a X", Locale.US);

    @Test
    public void testBasicCron() {
        printDate("03:15:00 11/20/2015 PM Z");
        printDate("03:15:00 11/27/2015 PM Z");
    }

    private void printDate(final String startDate) {
        final ZonedDateTime now = ZonedDateTime.parse(startDate, dfSimple);
        System.out.println("Starting: " + df.format(now));
        printNextDate(now, "0 6 * * 0");//Sunday
        printNextDate(now, "0 6 * * 1");
        printNextDate(now, "0 6 * * 2");
        printNextDate(now, "0 6 * * 3");
        printNextDate(now, "0 6 * * 4");
        printNextDate(now, "0 6 * * 5");
        printNextDate(now, "0 6 * * 6");
    }

    private void printNextDate(final ZonedDateTime now, final String cronString) {
        final ZonedDateTime date = nextSchedule(cronString, now);
        System.out.println("Next time: " + df.format(date));
    }

    private static ZonedDateTime nextSchedule(final String cronString, final ZonedDateTime lastExecution) {
        final CronParser cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        final Cron cron = cronParser.parse(cronString);

        final ExecutionTime executionTime = ExecutionTime.forCron(cron);

        final Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(lastExecution);
        if (nextExecution.isPresent()) {
            return nextExecution.get();
        } else {
            throw new NullPointerException("next execution is not present");
        }
    }
}

