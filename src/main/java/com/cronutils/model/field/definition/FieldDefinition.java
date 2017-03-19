package com.cronutils.model.field.definition;

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

import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.utils.Preconditions;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Represents a definition of allowed values for a cron field.
 */
public class FieldDefinition implements Serializable {
    private CronFieldName fieldName;
    private FieldConstraints constraints;

    /**
     * Constructor
     * @param fieldName - CronFieldName; name of the field
     *                  if null, a NullPointerException will be raised.
     * @param constraints - FieldConstraints, constraints;
     *                    if null, a NullPointerException will be raised.
     */
    public FieldDefinition(CronFieldName fieldName, FieldConstraints constraints){
        this.fieldName = Preconditions.checkNotNull(fieldName, "CronFieldName must not be null");
        this.constraints = Preconditions.checkNotNull(constraints, "FieldConstraints must not be null");
    }

    /**
     * Retrieve field name
     * @return CronFieldName instance, never null;
     */
    public CronFieldName getFieldName() {
        return fieldName;
    }

    /**
     * Get field constraints
     * @return FieldConstraints instance, never null;
     */
    public FieldConstraints getConstraints() {
        return constraints;
    }

    /**
     * Creates a field definition comparator. Will compare by CronFieldName order value;
     * @return Comparator for FieldDefinition instance, never null;
     */
    public static Comparator<FieldDefinition> createFieldDefinitionComparator() {
        return new Comparator<FieldDefinition>() {
            @Override
            public int compare(FieldDefinition o1, FieldDefinition o2) {
                return o1.getFieldName().getOrder() - o2.getFieldName().getOrder();
            }
        };
    }
}

