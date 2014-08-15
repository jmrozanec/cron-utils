package com.cron.utils.model;

import com.cron.utils.CronFieldName;
import com.cron.utils.parser.field.CronField;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.Validate;

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
public class Cron {
    private Map<CronFieldName, CronField> fields;

    public Cron(List<CronField> fields){
        this.fields = Maps.newHashMap();
        Validate.notNull(fields);
        for(CronField field : fields){
            this.fields.put(field.getField(), field);
        }
    }

    public CronField retrieve(CronFieldName name){
        return fields.get(name);
    }

    public Map<CronFieldName, CronField> retrieveFieldAsMap(){
        return Collections.unmodifiableMap(fields);
    }
}
