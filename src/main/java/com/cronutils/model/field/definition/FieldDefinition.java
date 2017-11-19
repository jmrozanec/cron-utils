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

import java.io.Serializable;
import java.util.Comparator;

import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.utils.Preconditions;

/**
 * Represents a definition of allowed values for a cron field.
 */
public class FieldDefinition implements Serializable {
    private CronFieldName fieldName;
    private FieldConstraints constraints;
    private final boolean optional;

    /**
     * Mandatory field Constructor.
     *
     * @param fieldName   - CronFieldName; name of the field
     *                    if null, a NullPointerException will be raised.
     * @param constraints - FieldConstraints, constraints;
     *                    if null, a NullPointerException will be raised.
     */
    public FieldDefinition(CronFieldName fieldName, FieldConstraints constraints) {
        this(fieldName, constraints, false);
    }

    /**
     * Constructor.
     *
     * @param fieldName   - CronFieldName; name of the field
     *                    if null, a NullPointerException will be raised.
     * @param constraints - FieldConstraints, constraints;
     *                    if null, a NullPointerException will be raised.
     * @param optional    - if {@code false} the field is mandatory, optional otherwise.
     */
    public FieldDefinition(CronFieldName fieldName, FieldConstraints constraints, boolean optional) {
        this.fieldName = Preconditions.checkNotNull(fieldName, "CronFieldName must not be null");
        this.constraints = Preconditions.checkNotNull(constraints, "FieldConstraints must not be null");
        this.optional = optional;
    }

    /**
     * Retrieve field name.
     *
     * @return CronFieldName instance, never null;
     */
    public CronFieldName getFieldName() {
        return fieldName;
    }

    /**
     * Get field constraints.
     *
     * @return FieldConstraints instance, never null;
     */
    public FieldConstraints getConstraints() {
        return constraints;
    }

    /**
     * Get optional tag.
     *
     * @return optional tag
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * Creates a field definition comparator. Will compare by CronFieldName order value;
     *
     * @return Comparator for FieldDefinition instance, never null;
     */
    public static Comparator<FieldDefinition> createFieldDefinitionComparator() {
        return Comparator.comparingInt(o -> o.getFieldName().getOrder());
    }
}

