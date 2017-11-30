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

package com.cronutils.parser;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CronParserCron4JIntegrationTest {
    private CronParser cron4jParser;

    @Before
    public void setUp() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J);
        cron4jParser = new CronParser(cronDefinition);
    }

    @Test
    public void testParseIssue32Expression01() {
        final String cronExpr = "* 1,2,3,4,5,6 * 1,2,3 *";
        cron4jParser.parse(cronExpr);
    }

    @Test
    public void testParseIssue32Expression02() {
        final String cronExpr = "* 1 1,2 * 4";
        cron4jParser.parse(cronExpr);
    }

    @Test
    public void testParseStrictRangeEnforced01() {
        final String cronExpr = "* 1 1-2 * 4";
        cron4jParser.parse(cronExpr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseStrictRangeEnforced02() {
        final String cronExpr = "* 1 5-2 * 4";
        cron4jParser.parse(cronExpr);
    }

    @Test
    public void testParseLastDayOfMonth() {
        final String cronExpr = "* * L * *";
        final Cron cron = cron4jParser.parse(cronExpr);
        assertThat(cron.asString(), is("* * L * *"));
    }

    @Test //issue 202
    public void testParseSunday() {
        final String cronExpr = "* * * * sun";
        cron4jParser.parse(cronExpr);
    }
}
