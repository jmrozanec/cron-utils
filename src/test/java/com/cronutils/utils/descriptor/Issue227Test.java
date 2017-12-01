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

package com.cronutils.utils.descriptor;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertEquals;

public class Issue227Test {

    /**
     * Issue #227 - Getting a leaking "%s" in description output.
     */
    private CronParser parser;

    @Before
    public void setUp() {
        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    @Test
    public void testProperDescriptorOutput() {
        final Cron cron = parser.parse("0 5-35/30 * * * ?");
        final CronDescriptor descriptor = CronDescriptor.instance(Locale.US);
        final String description = descriptor.describe(cron);

        assertEquals("every 30 minutes between 5 and 35", description);
    }

    @Test
    public void testProperDescriptorOutputWithSeconds() {
        final Cron cron = parser.parse("5-35/30 * * * * ?");
        final CronDescriptor descriptor = CronDescriptor.instance(Locale.US);
        final String description = descriptor.describe(cron);

        assertEquals("every 30 seconds between 5 and 35", description);
    }

}
