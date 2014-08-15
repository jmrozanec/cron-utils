package com.cron.utils.model;

import com.cron.utils.CronFieldName;
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
public class CronDefinition {
    private Map<CronFieldName, FieldDefinition> fieldDefinitions;
    private boolean lastFieldOptional;

    public CronDefinition(List<FieldDefinition> fieldDefinitions, boolean lastFieldOptional){
        this.fieldDefinitions = Maps.newHashMap();
        Validate.notNull(fieldDefinitions);
        for(FieldDefinition field : fieldDefinitions){
            this.fieldDefinitions.put(field.getFieldName(), field);
        }
        this.lastFieldOptional = lastFieldOptional;
    }

    public boolean isLastFieldOptional() {
        return lastFieldOptional;
    }

    public Set<FieldDefinition> getFieldDefinitions(){
        return new HashSet<FieldDefinition>(fieldDefinitions.values());
    }
}
