package com.cron.utils.parser;

import com.cron.utils.CronType;

import java.util.HashMap;
import java.util.Map;

/*
 * Copyright 2014 jmrozanec
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
public class CronParserRegistry {
    private Map<CronType, CronParser> registry;

    private CronParserRegistry() {
        registry = new HashMap<CronType, CronParser>();
        register(CronType.CRON4J, cron4jParser());
        register(CronType.QUARTZ, quartzParser());
        register(CronType.UNIX, unixCrontabParser());
    }

    private CronParser cron4jParser() {
        return ParserDefinitionBuilder.defineParser()
                .withMinutes()
                .withHours()
                .withDayOfMonth()
                .withMonth()
                .withDayOfWeek()
                .instance();
    }

    private CronParser unixCrontabParser() {
        return ParserDefinitionBuilder.defineParser()
                .withMinutes()
                .withHours()
                .withDayOfMonth()
                .withMonth()
                .withDayOfWeek()
                .instance();
    }

    private CronParser quartzParser() {
        return ParserDefinitionBuilder.defineParser()
                .withSeconds()
                .withMinutes()
                .withHours()
                .withDayOfMonth()
                .withMonth()
                .withDayOfWeek()
                .withYear()
                .andLastFieldOptional()
                .instance();
    }

    public CronParserRegistry register(CronType cronType, CronParser parser) {
        registry.put(cronType, parser);
        return this;
    }

    public CronParser retrieveParser(CronType cronType) {
        return registry.get(cronType);
    }

    public static CronParserRegistry instance() {
        return new CronParserRegistry();
    }
}
