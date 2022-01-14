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

import static org.junit.Assert.assertEquals;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import java.text.ParseException;
import java.util.Locale;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Provide an example on how convert a cron expression to ISO8601
 */
public class Issue462Test {

    private CronParser parser;
    private CronDescriptor descriptor;

    @Before
    public void setUp() {
        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING));
        descriptor = CronDescriptor.instance(Locale.ENGLISH);
    }

    /**
     * Faulty describe that includes a proposal for the fixed output.
     */
    @Test
    public void testCaseEverySecondOnEvery30Minutes() {
        final String cronExpression = "* /30 * * * *";
        final Cron cron = parser.parse(cronExpression);
        // create the *** BY NOW ** wrong describe
        final String describe = descriptor.describe(cron);
        // ** will fail by now ** this is a just proposal how the describe may look like
        assertEquals("every second every 30 minutes", describe);
    }

    /**
     * Correct describe for showing that the same output is generated.
     */
    @Test
    public void testCaseOnceEvery30Minutes() {
        final String cronExpression = "0 /30 * * * *";
        final Cron cron = parser.parse(cronExpression);
        // create the correct describe
        final String describe = descriptor.describe(cron);
        assertEquals("every 30 minutes", describe);
    }
}
