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

package com.cronutils.mapper;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MappingOptionalFieldsTest {

    @Test
    public void testMappingOptionalFields() {
        CronDefinition cronDefinition = CronDefinitionBuilder.defineCron()
                .withMinutes().withStrictRange().withValidRange(0, 60).and()
                .withHours().withStrictRange().and()
                .withDayOfMonth().supportsL().withStrictRange().and()
                .withMonth().withStrictRange().and()
                .withDayOfWeek().withValidRange(0, 7).withMondayDoWValue(1).withIntMapping(7, 0).supportsHash().supportsL().withStrictRange().and()
                .withYear().optional().and()
                .instance();

        final CronDefinition quartzDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);

        CronParser parser = new CronParser(quartzDefinition);
        CronMapper mapper = new CronMapper(quartzDefinition, cronDefinition, cron -> cron);


        final String expected = "0 9-18 * * 0-2";
        final String expression = "5 0 9-18 ? * 1-3";
        final String mapping = mapper.map(parser.parse(expression)).asString();
        assertEquals(String.format("Expected [%s] but got [%s]", expected, mapping), expected, mapping);
    }

}
