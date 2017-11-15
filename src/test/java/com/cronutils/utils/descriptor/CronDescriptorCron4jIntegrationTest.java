package com.cronutils.utils.descriptor;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

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
public class CronDescriptorCron4jIntegrationTest {
    private CronDescriptor descriptor;
    private CronParser parser;

    @Before
    public void setUp() throws Exception {
        descriptor = CronDescriptor.instance(Locale.UK);
        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J));
    }

    @Test
    public void testEveryMinuteBetween1100And1110() {
        assertEquals("every minute between 11:00 and 11:10", descriptor.describe(parser.parse("0-10 11 * * *")));
    }

    @Test
    public void testEveryMinute() {
        assertEquals("every minute", descriptor.describe(parser.parse("* * * * *")));
        assertEquals("every minute", descriptor.describe(parser.parse("*/1 * * * *")));
        assertEquals("every minute", descriptor.describe(parser.parse("0/1 * * * *")));
    }

    @Test
    public void testEveryFiveMinutes() {
        assertEquals("every 5 minutes", descriptor.describe(parser.parse("*/5 * * * *")));
        assertEquals("every 5 minutes", descriptor.describe(parser.parse("0/5 * * * *")));
    }

    @Test
    public void testAtElevenThirty() {
        assertEquals("at 11:30", descriptor.describe(parser.parse("30 11 * * *")));
    }

    @Test
    public void testAtTwentyThreeFromMondayThroughFriday() {
        assertEquals("at 23:00 every day between Monday and Friday", descriptor.describe(parser.parse("0 23 * * MON-FRI")));
        assertEquals("at 23:00 every day between Monday and Friday", descriptor.describe(parser.parse("0 23 * * 1-5")));
    }

    /**
     * Issue #32: Exception thrown when requesting a description
     * java.util.UnknownFormatConversionException: Conversion = 'p'
     * Should not throw any exceptions!
     */
    @Test
    public void testDescribeIssue32Expression01() {
        String cronExpr = "* 1,2,3,4,5,6 * 1,2,3 *";
        descriptor.describe(parser.parse(cronExpr));
    }

    /**
     * Issue #32: Exception thrown when requesting a description:
     * java.lang.ArrayIndexOutOfBoundsException: 1
     * Should not throw any exceptions!
     */
    @Test
    public void testDescribeIssue32Expression02() {
        String cronExpr = "* 1 1,2 * 4";
        descriptor.describe(parser.parse(cronExpr));
    }
}
