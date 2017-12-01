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

package com.cronutils.validator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertNotNull;

public class CronValidatorQuartzIntegrationTest {
    private CronParser parser;

    @Before
    public void setUp() {
        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    /**
     * Issue #27: month range string mapping is valid.
     */
    @Test
    public void testMonthRangeMappingIsValid() {
        parser.parse("0 0 0 * JUL-AUG ? *").validate();
    }

    /**
     * Issue #27: single month string mapping is valid.
     */
    @Test
    public void testSingleMonthMappingIsValid() {
        final LocalDate date = LocalDate.of(2015, 1, 1);
        for (int j = 0; j < 12; j++) {
            final String expression = String.format("0 0 0 * %s ? *", date.plusMonths(j).format(DateTimeFormatter.ofPattern("MMM", Locale.US)).toUpperCase());
            parser.parse(expression);
        }
    }

    /**
     * Issue #27: day of week range string mapping is valid.
     */
    @Test
    public void testDayOfWeekRangeMappingIsValid() {
        assertNotNull(parser.parse("0 0 0 ? * MON-FRI *"));
    }

    /**
     * Issue #27: single day of week string mapping is valid.
     */
    @Test
    public void testDayOfWeekMappingIsValid() {
        for (final String dow : new String[] { "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN" }) {
            parser.parse(String.format("0 0 0 ? * %s *", dow));
        }

    }

    /**
     * Issue #31: expressions
     * "0 10,44 14 ? 3 WED" and "0 0 12 ? * SAT-SUN"
     * considered invalid when replacing '?' for '*'
     * Fixed by adding support for question mark character.
     */
    @Test
    public void testQuestionMarkSupport() {
        parser.parse("0 10,44 14 ? 3 WED");
        parser.parse("0 0 12 ? * FRI-SAT");
        parser.parse("0 0 12 ? * SAT-SUN");
    }
}
