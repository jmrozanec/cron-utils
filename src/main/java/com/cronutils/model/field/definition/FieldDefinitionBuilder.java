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

package com.cronutils.model.field.definition;

import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.utils.Preconditions;

/**
 * Provides means to define cron field definitions.
 */
public class FieldDefinitionBuilder {
    protected CronDefinitionBuilder cronDefinitionBuilder;
    protected final CronFieldName fieldName;
    protected FieldConstraintsBuilder constraints;
    protected boolean optional;

    /**
     * Constructor.
     *
     * @param cronDefinitionBuilder - ParserDefinitionBuilder instance -
     *                              if null, a NullPointerException will be raised
     * @param fieldName             - CronFieldName instance -
     *                              if null, a NullPointerException will be raised
     */
    public FieldDefinitionBuilder(final CronDefinitionBuilder cronDefinitionBuilder, final CronFieldName fieldName) {
        this.cronDefinitionBuilder = Preconditions.checkNotNull(cronDefinitionBuilder, "ParserBuilder must not be null");
        this.fieldName = Preconditions.checkNotNull(fieldName, "CronFieldName must not be null");
        constraints = FieldConstraintsBuilder.instance().forField(fieldName);
    }

    /**
     * Provides means to define int values mappings between equivalent values.
     * As a convention, higher values are mapped into lower ones
     *
     * @param source - higher value
     * @param dest   - lower value with equivalent meaning to source
     * @return this instance
     */
    public FieldDefinitionBuilder withIntMapping(final int source, final int dest) {
        constraints.withIntValueMapping(source, dest);
        return this;
    }

    /**
     * Allows to set a range of valid values for field.
     *
     * @param startRange - start range value
     * @param endRange   - end range value
     * @return same FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withValidRange(final int startRange, final int endRange) {
        constraints.withValidRange(startRange, endRange);
        return this;
    }

    /**
     * Specifies that defined range for given field must be a strict range.
     *
     * @return same FieldDefinitionBuilder instance
     */
    public FieldDefinitionBuilder withStrictRange() {
        constraints.withStrictRange();
        return this;
    }

    /**
     * Allows to tag a field as optional.
     *
     * @return this instance
     */
    public FieldDefinitionBuilder optional() {
        optional = true;
        return this;
    }

    /**
     * Registers CronField in ParserDefinitionBuilder and returns its instance.
     *
     * @return ParserDefinitionBuilder instance obtained from constructor
     */
    public CronDefinitionBuilder and() {
        cronDefinitionBuilder.register(new FieldDefinition(fieldName, constraints.createConstraintsInstance(), optional));
        return cronDefinitionBuilder;
    }
}

