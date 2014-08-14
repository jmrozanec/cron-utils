package com.cron.utils.parser;

import com.cron.utils.CronFieldName;
import com.cron.utils.model.CronDefinition;
import com.cron.utils.model.FieldDefinition;
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
 * Builder that allows to define and create CronParser instances
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
     * @return new ParserDefinitionBuilder instance
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
    public FieldSpecialCharsDefinitionBuilder withDayOfWeek() {
        return new FieldSpecialCharsDefinitionBuilder(this, CronFieldName.DAY_OF_WEEK);
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
     * @return this ParserDefinitionBuilder instance
     */
    public CronDefinitionBuilder lastFieldOptional() {
        lastFieldOptional = true;
        return this;
    }

    /**
     * Registers a certain FieldDefinition
     * @param definition - FieldDefinition  instance, never null
     */
    void register(FieldDefinition definition) {
        fields.put(definition.getFieldName(), definition);
    }

    /**
     * Creates a new CronParser instance with provided field definitions
     * @return returns CronParser instance, never null
     */
    public CronDefinition instance() {
        return new CronDefinition(new ArrayList<FieldDefinition>(this.fields.values()), lastFieldOptional);
    }
}
