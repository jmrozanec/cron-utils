package com.cronutils.model.definition;

import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.FieldDefinition;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.Validate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * Defines fields and conditions over each field for a cron
 */
public class CronDefinition {
    private Map<CronFieldName, FieldDefinition> fieldDefinitions;
    private boolean lastFieldOptional;

    /**
     * Constructor
     * @param fieldDefinitions - list with field definitions. Must not be null or empty.
     *                         Throws a NullPointerException if a null values is received
     *                         Throws an IllegalArgumentException if an empty list is received
     * @param lastFieldOptional - boolean, value stating if last field is optional
     */
    public CronDefinition(List<FieldDefinition> fieldDefinitions, boolean lastFieldOptional){
        Validate.notNull(fieldDefinitions, "Field definitions must not be null");
        Validate.notEmpty(fieldDefinitions, "Field definitions must not be empty");
        if(lastFieldOptional){
            Validate.isTrue(fieldDefinitions.size() > 1, "If last field is optional, field definition must hold at least two fields");
        }
        this.fieldDefinitions = Maps.newHashMap();
        for(FieldDefinition field : fieldDefinitions){
            this.fieldDefinitions.put(field.getFieldName(), field);
        }
        this.lastFieldOptional = lastFieldOptional;
    }

    /**
     * If last field of a cron expression is optional
     * @return true if has an optional field, false otherwise.
     */
    public boolean isLastFieldOptional() {
        return lastFieldOptional;
    }

    /**
     * Returns field definitions for this cron
     * @return Set of FieldDefinition instances, never null.
     */
    public Set<FieldDefinition> getFieldDefinitions(){
        return new HashSet<FieldDefinition>(fieldDefinitions.values());
    }

    /**
     * Returns field definition for field name of this cron
     * @param cronFieldName cron field name
     * @return FieldDefinition instance
     */
    public FieldDefinition getFieldDefinition(CronFieldName cronFieldName){
        return fieldDefinitions.get(cronFieldName);
    }
}
