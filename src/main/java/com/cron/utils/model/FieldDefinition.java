package com.cron.utils.model;

import com.cron.utils.CronFieldName;
import com.cron.utils.parser.field.FieldConstraints;
import org.apache.commons.lang3.Validate;

import java.util.Comparator;

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
public class FieldDefinition {
    private CronFieldName fieldName;
    private FieldConstraints constraints;

    public FieldDefinition(CronFieldName fieldName, FieldConstraints constraints){
        this.fieldName = Validate.notNull(fieldName, "CronFieldName must not be null");
        this.constraints = Validate.notNull(constraints, "FieldConstraints must not be null");
    }

    public CronFieldName getFieldName() {
        return fieldName;
    }

    public FieldConstraints getConstraints() {
        return constraints;
    }

    public static Comparator<FieldDefinition> createFieldDefinitionComparator() {
        return new Comparator<FieldDefinition>() {
            @Override
            public int compare(FieldDefinition o1, FieldDefinition o2) {
                return o1.getFieldName().getOrder() - o2.getFieldName().getOrder();
            }
        };
    }
}
