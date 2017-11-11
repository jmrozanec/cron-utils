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

package com.cronutils.parser;

import java.util.Comparator;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.utils.Preconditions;
import com.google.common.base.MoreObjects;

/**
 * Represents a cron field.
 */
public class CronParserField {

    private final CronFieldName field;
    private final FieldConstraints constraints;
    private final FieldParser parser;
    private final boolean optional;

    /**
     * Mandatory CronParserField Constructor.
     *
     * @param fieldName   - CronFieldName instance
     * @param constraints - FieldConstraints, constraints
     */
    public CronParserField(CronFieldName fieldName, FieldConstraints constraints) {
        this(fieldName, constraints, false);
    }

    /**
     * Constructor.
     *
     * @param fieldName   - CronFieldName instance
     * @param constraints - FieldConstraints, constraints
     * @param optional    - optional tag
     */
    public CronParserField(CronFieldName fieldName, FieldConstraints constraints, boolean optional) {
        this.field = Preconditions.checkNotNull(fieldName, "CronFieldName must not be null");
        this.constraints = Preconditions.checkNotNull(constraints, "FieldConstraints must not be null");
        this.parser = new FieldParser(constraints);
        this.optional = optional;
    }

    /**
     * Returns field name.
     *
     * @return CronFieldName, never null
     */
    public CronFieldName getField() {
        return field;
    }

    /**
     * Returns optional tag.
     *
     * @return optional tag
     */
    public final boolean isOptional() {
        return optional;
    }

    /**
     * Parses a String cron expression.
     *
     * @param expression - cron expression
     * @return parse result as CronFieldParseResult instance - never null. May throw a RuntimeException if cron expression is bad.
     */
    public CronField parse(String expression) {
        return new CronField(field, parser.parse(expression), constraints);
    }

    /**
     * Create a Comparator that compares CronField instances using CronFieldName value.
     *
     * @return Comparator for CronField instance, never null.
     */
    public static Comparator<CronParserField> createFieldTypeComparator() {
        return Comparator.comparingInt(o -> o.getField().getOrder());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("field", field).toString();
    }
}
