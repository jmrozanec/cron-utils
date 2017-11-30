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

import org.junit.Ignore;
import org.junit.Test;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;

import static org.hamcrest.MatcherAssert.assertThat;

//FIXME
@Ignore ("Ignore this until someone is working on a fix")
public class Issue281Test {

    private static final String ISSUE_EXPRESSION = "0 0 0 24 1/12 ?";

    @Test
    public void testCronTypeQuartz() {
        final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        final CronParser parser = new CronParser(cronDefinition);
        final Cron cron = parser.parse(ISSUE_EXPRESSION);
        assertThat("cron is not null", cron != null);
    }
}
