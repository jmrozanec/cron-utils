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
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import static org.junit.Assert.assertEquals;

/**
 * https://github.com/jmrozanec/cron-utils/issues/230
 * add by Wangxin
 * add chinese local test
 */
public class CronDescriptorCron4jIntegrationZhTest {
    private CronDescriptor descriptor;
    private CronParser parser;

    @Before
    public void setUp() {
        descriptor = CronDescriptor.instance(Locale.CHINESE);
        parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J));
    }

    @Test
    public void testEveryMinuteBetween1100And1110() {
        assertEquals("每 分钟 在 11:00 和 11:10", descriptor.describe(parser.parse("0-10 11 * * *")));
    }

    @Test
    public void testEveryMinute() {
        assertEquals("每 分钟", descriptor.describe(parser.parse("* * * * *")));
        assertEquals("每 分钟", descriptor.describe(parser.parse("*/1 * * * *")));
        assertEquals("每 分钟", descriptor.describe(parser.parse("0/1 * * * *")));
    }

    @Test
    public void testEveryFiveMinutes() {
        assertEquals("每 5 分钟", descriptor.describe(parser.parse("*/5 * * * *")));
        assertEquals("每 5 分钟", descriptor.describe(parser.parse("0/5 * * * *")));
    }

    @Test
    public void testAtElevenThirty() {
        assertEquals("在 11:30", descriptor.describe(parser.parse("30 11 * * *")));
    }

}
