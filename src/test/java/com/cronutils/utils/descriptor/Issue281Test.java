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

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class Issue281Test {

    @Test
    public void shouldAcceptLastMonth() {
        final Cron cron = buildCron("0 0 0 24 1/12 ?");
        assertThat("cron is not null", cron != null);
    }

    @Test
    public void shouldAcceptFirstMonth() {
        final Cron cron = buildCron("0 0 0 24/1 1/12 ?");
        assertThat("cron is not null", cron != null);
    }

    @Test
    public void shouldAcceptLastDayOfMonth() {
        final Cron cron = buildCron("0 0 0 1/31 7 ?");
        assertThat("cron is not null", cron != null);
    }

    @Test
    public void shouldAcceptFirstDayOfMonth() {
        final Cron cron = buildCron("0 0 0 24/1 1/12 ?");
        assertThat("cron is not null", cron != null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenMonthExceeded() {
        final Cron cron = buildCron("0 0 0 24 1/13 ?");
    }

    private Cron buildCron(String expression) {
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        final CronParser parser = new CronParser(cronDefinition);
        return parser.parse(expression);
    }
}
