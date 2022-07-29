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

package com.cronutils.model.definition;

import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.utils.Preconditions;

import java.io.Serializable;
import java.util.*;

/**
 * Defines fields and conditions over each field for a cron.
 * The class is thread safe.
 */
public class CronDefinition implements Serializable {

    private static final long serialVersionUID = 7067112327461432170L;
    private final Map<CronFieldName, FieldDefinition> fieldDefinitions;
    private final Set<CronConstraint> cronConstraints;
    private final Set<CronNicknames> cronNicknames;
    private final boolean matchDayOfWeekAndDayOfMonth;

    /**
     * Constructor.
     *
     * @param fieldDefinitions - list with field definitions. Must not be null or empty.
     *                         Throws a NullPointerException if a null values is received
     *                         Throws an IllegalArgumentException if an empty list is received
     */
    public CronDefinition(final List<FieldDefinition> fieldDefinitions, final Set<CronConstraint> cronConstraints, Set<CronNicknames> cronNicknames, final boolean matchDayOfWeekAndDayOfMonth) {
        Preconditions.checkNotNull(cronNicknames, "Cron nicknames must not be null");
        if(!cronNicknames.contains(CronNicknames.REBOOT)){
            Preconditions.checkNotNull(fieldDefinitions, "Field definitions must not be null");
            Preconditions.checkNotNull(cronConstraints, "Cron validations must not be null");
            Preconditions.checkNotNullNorEmpty(fieldDefinitions, "Field definitions must not be empty");
            Preconditions.checkArgument(!fieldDefinitions.get(0).isOptional(), "The first field must not be optional");
        }
        this.fieldDefinitions = new EnumMap<>(CronFieldName.class);
        for (final FieldDefinition field : fieldDefinitions) {
            this.fieldDefinitions.put(field.getFieldName(), field);
        }
        this.cronConstraints = Collections.unmodifiableSet(cronConstraints);
        this.cronNicknames = Collections.unmodifiableSet(cronNicknames);
        this.matchDayOfWeekAndDayOfMonth = matchDayOfWeekAndDayOfMonth;
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
        return Collections.unmodifiableMap(fieldDefinitions);
    }

    /**
     * Returns field definition for field name of this cron.
     *
     * @param cronFieldName cron field name
     * @return FieldDefinition instance
     */
    public FieldDefinition getFieldDefinition(final CronFieldName cronFieldName) {
        return fieldDefinitions.get(cronFieldName);
    }

    /**
     * Returns {@code true} if this cron contains a field definition for field name.
     *
     * @param cronFieldName cron field name
     * @return {@code true} if this cron contains a field definition for field name
     */
    public boolean containsFieldDefinition(final CronFieldName cronFieldName) {
        return fieldDefinitions.containsKey(cronFieldName);
    }

    public Set<CronConstraint> getCronConstraints() {
        return cronConstraints;
    }

    public Set<CronNicknames> getCronNicknames() {
        return cronNicknames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CronDefinition that = (CronDefinition) o;
        return matchDayOfWeekAndDayOfMonth == that.matchDayOfWeekAndDayOfMonth
            && fieldDefinitions.equals(that.fieldDefinitions)
            && cronConstraints.equals(that.cronConstraints)
            && cronNicknames.equals(that.cronNicknames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            fieldDefinitions, cronConstraints, cronNicknames, matchDayOfWeekAndDayOfMonth);
    }
}

