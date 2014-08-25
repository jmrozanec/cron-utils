package com.cronutils.model.definition;

import com.cronutils.model.CronType;
import com.google.common.collect.Maps;

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
public class CronDefinitionRegistry {
    private Map<CronType, CronDefinition> registry;

    private CronDefinitionRegistry() {
        registry = Maps.newHashMap();
        register(CronType.CRON4J, cron4j());
        register(CronType.QUARTZ, quartz());
        register(CronType.UNIX, unixCrontab());
    }

    private CronDefinition cron4j() {
        return CronDefinitionBuilder.defineCron()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .withDayOfWeek().and()
                .instance();

    }

    private CronDefinition unixCrontab() {
        return CronDefinitionBuilder.defineCron()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .withDayOfWeek().and()
                .instance();
    }

    private CronDefinition quartz() {
        return CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().supportsHash().supportsL().supportsW().and()
                .withMonth().and()
                .withDayOfWeek().withIntMapping(7, 0).supportsHash().supportsL().supportsW().and()
                .withYear().and()
                .lastFieldOptional()
                .instance();
    }

    public CronDefinitionRegistry register(CronType cronType, CronDefinition definition) {
        registry.put(cronType, definition);
        return this;
    }

    public CronDefinition retrieve(CronType cronType) {
        return registry.get(cronType);
    }

    public static CronDefinitionRegistry instance() {
        return new CronDefinitionRegistry();
    }
}
