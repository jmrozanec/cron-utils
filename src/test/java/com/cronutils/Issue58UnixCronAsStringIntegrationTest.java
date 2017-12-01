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

import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class Issue58UnixCronAsStringIntegrationTest {
    private CronParser cronParser;

    @Before
    public void setup() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        cronParser = new CronParser(cronDefinition);
    }

    @Test
    public void everyEvenHourShouldBeParsedCorrectly() {
        final Cron cron = cronParser.parse("0 0/1 * * *");
        assertThat(cron.asString(), anyOf(is("0 0/1 * * *"), is("0 /1 * * *"), is("0 0 * * *")));
    }

    @Test
    public void everyOddHourShouldBeParsedCorrectly() {
        final Cron cron = cronParser.parse("0 1/2 * * *");
        assertThat(cron.asString(), is("0 1/2 * * *"));
    }

    @Test
    public void everyEvenMinuteShouldBeParsedCorrectly() {
        final Cron cron = cronParser.parse("0/1 * * * *");
        assertThat(cron.asString(), anyOf(is("0/1 * * * *"), is("/1 * * * *"), is("0 * * * *")));
    }

    @Test
    public void everyOddMinuteShouldBeParsedCorrectly() {
        final Cron cron = cronParser.parse("1/2 * * * *");
        assertThat(cron.asString(), is("1/2 * * * *"));
    }
}
