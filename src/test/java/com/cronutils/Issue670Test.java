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

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unix crontab support for the L, LW and W special characters.
 */
class Issue670Test {

    private final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
    private final CronDescriptor descriptor = CronDescriptor.instance(Locale.UK);

    @ParameterizedTest
    @ValueSource(strings = {
            "0 0 L * *",
            "0 0 L-3 * *",
            "0 0 LW * *",
            "0 13 LW * *",
            "0 0 1W * *",
            "0 0 15W * *",
            "0 0 L,15 * *",
            "0 0 * * L",
            "0 0 * * 0L",
            "0 0 * * 6L",
            "0 0 LW * 6L"
    })
    void unixParserAcceptsSpecialChars(String expression) {
        final Cron cron = parser.parse(expression);
        cron.validate();
        assertEquals(expression, cron.asString());
    }

    /**
     * W is a day-of-month modifier ("nearest weekday to the nth"); it has no day-of-week meaning
     * and no value generator implements it there, so it must not parse.
     */
    @ParameterizedTest
    @ValueSource(strings = {"0 0 * * 5W", "0 0 * * 0W", "0 0 * * 1W", "0 0 * * LW", "0 0 * * 1-5W"})
    void unixParserRejectsWInDayOfWeek(String expression) {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(expression));
    }

    @Test
    void unixParserRejectsBareWInDayOfMonth() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("0 0 W * *"));
    }

    @ParameterizedTest
    @CsvSource({
            // June 2025: 1st is a Sunday, 14th a Saturday, 30th a Monday
            "0 0 L * *,     2025-06-30",
            "0 0 L-3 * *,   2025-06-27",
            "0 0 LW * *,    2025-06-30",
            "0 0 1W * *,    2025-06-02",
            "0 0 14W * *,   2025-06-13",
            "0 0 15W * *,   2025-06-16",
            "0 0 * * 1L,    2025-06-30",
            "0 0 * * 6L,    2025-06-28",
            "0 0 * * 0L,    2025-06-29",
            // November 2025: 1st is a Saturday, so the nearest weekday rolls forward to Monday the 3rd
            "0 0 1W 11 *,   2025-11-03",
            // February 2026: last day is Saturday the 28th, so LW is Friday the 27th
            "0 0 LW 2 *,    2026-02-27"
    })
    void nextExecutionMatchesSpecialChar(String expression, String expectedDate) {
        final ZonedDateTime from = ZonedDateTime.of(2025, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(expression));
        final ZonedDateTime next = executionTime.nextExecution(from)
                .orElseThrow(() -> new AssertionError(String.format("no next execution for [%s]", expression)));

        assertEquals(LocalDate.parse(expectedDate), next.toLocalDate());
        assertTrue(executionTime.isMatch(next), String.format("[%s] should match its own next execution", expression));
    }

    /**
     * Unix day-of-month and day-of-week are OR'ed when both are restricted.
     */
    @Test
    void dayOfMonthAndDayOfWeekAreOred() {
        final ExecutionTime executionTime = ExecutionTime.forCron(parser.parse("0 0 LW * 6L"));
        final ZonedDateTime lastSaturday = ZonedDateTime.of(2025, 6, 28, 0, 0, 0, 0, ZoneOffset.UTC);
        final ZonedDateTime lastWeekday = ZonedDateTime.of(2025, 6, 30, 0, 0, 0, 0, ZoneOffset.UTC);

        assertTrue(executionTime.isMatch(lastSaturday), "last Saturday should match");
        assertTrue(executionTime.isMatch(lastWeekday), "last weekday should match");
    }

    @ParameterizedTest
    @CsvSource({
            "0 0 L * *,     at 00:00 last day of month",
            "0 0 L-10 * *,  at 00:00 10 days before the last day of the month",
            "0 0 LW * *,    at 00:00 last weekday of month",
            "0 0 1W * *,    at 00:00 the nearest weekday to the 1 of the month",
            "0 0 * * 6L,    at 00:00 last Saturday of every month"
    })
    void descriptorDescribesSpecialChars(String expression, String expectedDescription) {
        assertEquals(expectedDescription, descriptor.describe(parser.parse(expression)));
    }
}
