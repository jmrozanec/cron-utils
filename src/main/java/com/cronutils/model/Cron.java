package com.cronutils.model;

import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 * Represents a cron expression
 */
public class Cron {
    private CronDefinition cronDefinition;
    private Map<CronFieldName, CronField> fields;
    private String asString;

    public Cron(CronDefinition cronDefinition, List<CronField> fields){
        this.cronDefinition = Validate.notNull(cronDefinition, "CronDefinition must not be null");
        Validate.notNull(fields, "CronFields cannot be null");
        this.fields = Maps.newHashMap();
        for(CronField field : fields){
            this.fields.put(field.getField(), field);
        }
    }

    /**
     * Retrieve value for cron field
     * @param name - cron field name.
     *             If null, a NullPointerException will be raised.
     * @return CronField that corresponds to given CronFieldName
     */
    public CronField retrieve(CronFieldName name){
        return fields.get(Validate.notNull(name, "CronFieldName must not be null"));
    }

    /**
     * Retrieve all cron field values as map
     * @return unmodifiable Map with key CronFieldName and values CronField, never null
     */
    public Map<CronFieldName, CronField> retrieveFieldsAsMap(){
        return Collections.unmodifiableMap(fields);
    }

    public String asString(){
        if(asString == null){
            ArrayList<CronField> fields = new ArrayList<CronField>(this.fields.values());
            Collections.sort(fields, CronField.createFieldComparator());
            StringBuilder builder = new StringBuilder();
            for(int j =0; j<fields.size(); j++){
                builder.append(String.format("%s ", fields.get(j).getExpression().asString()));
            }
            asString = builder.toString().trim();
        }
        return asString;
    }

    public CronDefinition getCronDefinition() {
        return cronDefinition;
    }
}
