package com.cronutils.model.definition;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.utils.Preconditions;

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
 * Defines fields and conditions over each field for a cron.
 */
public class CronDefinition implements Serializable {
    private Map<CronFieldName, FieldDefinition> fieldDefinitions;
    private Set<CronConstraint> cronConstraints;
    private boolean strictRanges;
    private boolean matchDayOfWeekAndDayOfMonth;

    /**
     * Constructor.
     *
     * @param fieldDefinitions - list with field definitions. Must not be null or empty.
     *                         Throws a NullPointerException if a null values is received
     *                         Throws an IllegalArgumentException if an empty list is received
     */
    public CronDefinition(List<FieldDefinition> fieldDefinitions, Set<CronConstraint> cronConstraints,
            boolean strictRanges, boolean matchDayOfWeekAndDayOfMonth) {
        Preconditions.checkNotNull(fieldDefinitions, "Field definitions must not be null");
        Preconditions.checkNotNull(cronConstraints, "Cron validations must not be null");
        Preconditions.checkNotNullNorEmpty(fieldDefinitions, "Field definitions must not be empty");
        Preconditions.checkArgument(!fieldDefinitions.get(0).isOptional(), "The first field must not be optional");
        this.fieldDefinitions = new HashMap<>();
        for (FieldDefinition field : fieldDefinitions) {
            this.fieldDefinitions.put(field.getFieldName(), field);
        }
        this.cronConstraints = Collections.unmodifiableSet(cronConstraints);
        this.strictRanges = strictRanges;
        this.matchDayOfWeekAndDayOfMonth = matchDayOfWeekAndDayOfMonth;
    }

    /**
     * If ranges for field values should be strictly enforced ('from' greater than 'to').
     *
     * @return true if should be enforced, false otherwise.
     */
    public boolean isStrictRanges() {
        return strictRanges;
    }

    /**
     * If both the day of the week and day of the month should be matched.
     *
     * @return true if both should be matched, false otherwise
     */
    public boolean isMatchDayOfWeekAndDayOfMonth() {
        return matchDayOfWeekAndDayOfMonth;
    }

    /**
     * Returns field definitions for this cron.
     *
     * @return Set of FieldDefinition instances, never null.
     */
    public Set<FieldDefinition> getFieldDefinitions() {
        return new HashSet<>(fieldDefinitions.values());
    }

    /**
     * Retrieve all cron field definitions values as map.
     *
     * @return unmodifiable Map with key CronFieldName and values FieldDefinition, never null
     */
    public Map<CronFieldName, FieldDefinition> retrieveFieldDefinitionsAsMap() {
        return Collections.unmodifiableMap(this.fieldDefinitions);
    }

    /**
     * Returns field definition for field name of this cron.
     *
     * @param cronFieldName cron field name
     * @return FieldDefinition instance
     */
    public FieldDefinition getFieldDefinition(CronFieldName cronFieldName) {
        return fieldDefinitions.get(cronFieldName);
    }

    /**
     * Returns {@code true} if this cron contains a field definition for field name.
     *
     * @param cronFieldName cron field name
     * @return {@code true} if this cron contains a field definition for field name
     */
    public boolean containsFieldDefinition(CronFieldName cronFieldName) {
        return fieldDefinitions.containsKey(cronFieldName);
    }

    public Set<CronConstraint> getCronConstraints() {
        return cronConstraints;
    }
}

