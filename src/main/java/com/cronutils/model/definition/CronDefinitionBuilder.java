package com.cronutils.model.definition;

import com.cronutils.model.CronType;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.FieldDayOfWeekDefinitionBuilder;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.model.field.definition.FieldDefinitionBuilder;
import com.cronutils.model.field.definition.FieldSpecialCharsDefinitionBuilder;
import com.google.common.collect.Maps;

import java.util.ArrayList;
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

/**
 * Builder that allows to define and create CronDefinition instances
 */
public class CronDefinitionBuilder {
    private Map<CronFieldName, FieldDefinition> fields;
    private boolean lastFieldOptional;

    /**
     * Constructor.
     * lastFieldOptional is defined false.
     */
    private CronDefinitionBuilder() {
        fields = Maps.newHashMap();
        lastFieldOptional = false;
    }

    /**
     * Creates a builder instance
     * @return new CronDefinitionBuilder instance
     */
    public static CronDefinitionBuilder defineCron() {
        return new CronDefinitionBuilder();
    }

    /**
     * Adds definition for seconds field
     * @return new FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withSeconds() {
        return new FieldDefinitionBuilder(this, CronFieldName.SECOND);
    }

    /**
     * Adds definition for minutes field
     * @return new FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withMinutes() {
        return new FieldDefinitionBuilder(this, CronFieldName.MINUTE);
    }

    /**
     * Adds definition for hours field
     * @return new FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withHours() {
        return new FieldDefinitionBuilder(this, CronFieldName.HOUR);
    }

    /**
     * Adds definition for day of month field
     * @return new FieldSpecialCharsDefinitionBuilder instance
     */
    public FieldSpecialCharsDefinitionBuilder withDayOfMonth() {
        return new FieldSpecialCharsDefinitionBuilder(this, CronFieldName.DAY_OF_MONTH);
    }

    /**
     * Adds definition for month field
     * @return new FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withMonth() {
        return new FieldDefinitionBuilder(this, CronFieldName.MONTH);
    }

    /**
     * Adds definition for day of week field
     * @return new FieldSpecialCharsDefinitionBuilder instance
     */
    public FieldDayOfWeekDefinitionBuilder withDayOfWeek() {
        return new FieldDayOfWeekDefinitionBuilder(this, CronFieldName.DAY_OF_WEEK);
    }

    /**
     * Adds definition for year field
     * @return new FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withYear() {
        return new FieldDefinitionBuilder(this, CronFieldName.YEAR);
    }

    /**
     * Sets lastFieldOptional value to true
     * @return this CronDefinitionBuilder instance
     */
    public CronDefinitionBuilder lastFieldOptional() {
        lastFieldOptional = true;
        return this;
    }

    /**
     * Registers a certain FieldDefinition
     * @param definition - FieldDefinition  instance, never null
     */
    public void register(FieldDefinition definition) {
        fields.put(definition.getFieldName(), definition);
    }

    /**
     * Creates a new CronDefinition instance with provided field definitions
     * @return returns CronDefinition instance, never null
     */
    public CronDefinition instance() {
        return new CronDefinition(new ArrayList<FieldDefinition>(this.fields.values()), lastFieldOptional);
    }

    /**
     * Creates CronDefinition instance matching cron4j specification;
     * @return CronDefinition instance, never null;
     */
    private static CronDefinition cron4j() {
        return CronDefinitionBuilder.defineCron()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(0,6).withMondayDoWValue(1).and()
                .instance();

    }

    /**
     * Creates CronDefinition instance matching quartz specification;
     * @return CronDefinition instance, never null;
     */
    private static CronDefinition quartz() {
        return CronDefinitionBuilder.defineCron()
                .withSeconds().and()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().supportsHash().supportsL().supportsW().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(1,7).withMondayDoWValue(2).supportsHash().supportsL().supportsW().and()
                .withYear().withValidRange(1970, 2099).and()
                .lastFieldOptional()
                .instance();
    }

    /**
     * Creates CronDefinition instance matching unix crontab specification;
     * @return CronDefinition instance, never null;
     */
    private static CronDefinition unixCrontab() {
        return CronDefinitionBuilder.defineCron()
                .withMinutes().and()
                .withHours().and()
                .withDayOfMonth().and()
                .withMonth().and()
                .withDayOfWeek().withValidRange(0,7).withMondayDoWValue(1).withIntMapping(7,0).and()
                .instance();
    }

    /**
     * Creates CronDefinition instance matching cronType specification;
     * @param cronType - some cron type. If null, a RuntimeException will be raised.
     * @return CronDefinition instance if definition is found; a RuntimeException otherwise.
     */
    public static CronDefinition instanceDefinitionFor(CronType cronType){
        switch (cronType){
            case CRON4J:
                return cron4j();
            case QUARTZ:
                return quartz();
            case UNIX:
                return unixCrontab();
            default:
                throw new RuntimeException(String.format("No cron definition found for %s", cronType));
        }
    }
}
