/*
 * Copyright 2015 jmrozanec
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

package com.cronutils.builder;

import com.cronutils.model.Cron;
import com.cronutils.model.RebootCron;
import com.cronutils.model.SingleCron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.expression.visitor.ValidationFieldExpressionVisitor;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.utils.VisibleForTesting;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import static com.cronutils.model.field.CronFieldName.*;
import static com.cronutils.utils.Preconditions.checkState;

public class CronBuilder {

    private final Map<CronFieldName, CronField> fields = new EnumMap<>(CronFieldName.class);
    private final CronDefinition definition;

    private CronBuilder(final CronDefinition definition) {
        this.definition = definition;
    }

    public static CronBuilder cron(final CronDefinition definition) {
        return new CronBuilder(definition);
    }

    public CronBuilder withDoY(final FieldExpression expression) {
        return addField(DAY_OF_YEAR, expression);
    }

    public CronBuilder withYear(final FieldExpression expression) {
        return addField(YEAR, expression);
    }

    public CronBuilder withDoM(final FieldExpression expression) {
        return addField(DAY_OF_MONTH, expression);
    }

    public CronBuilder withMonth(final FieldExpression expression) {
        return addField(MONTH, expression);
    }

    public CronBuilder withDoW(final FieldExpression expression) {
        return addField(DAY_OF_WEEK, expression);
    }

    public CronBuilder withHour(final FieldExpression expression) {
        return addField(HOUR, expression);
    }

    public CronBuilder withMinute(final FieldExpression expression) {
        return addField(MINUTE, expression);
    }

    public CronBuilder withSecond(final FieldExpression expression) {
        return addField(SECOND, expression);
    }

    public Cron instance() {
        return new SingleCron(definition, new ArrayList<>(fields.values())).validate();
    }

    private CronBuilder applyIfPresent(
            CronFieldName fieldName,
            FieldExpression expression) {

        if (definition.containsFieldDefinition(fieldName)) {
            addField(fieldName, expression);
        }
        return this;
    }

    public static Cron yearly(final CronDefinition definition) {
        return new CronBuilder(definition)
                .applyIfPresent(SECOND,       new On(new IntegerFieldValue(0)))
                .applyIfPresent(MINUTE,       new On(new IntegerFieldValue(0)))
                .applyIfPresent(HOUR,         new On(new IntegerFieldValue(0)))
                .applyIfPresent(DAY_OF_MONTH, new On(new IntegerFieldValue(1)))
                .applyIfPresent(MONTH,        new On(new IntegerFieldValue(1)))
                .applyIfPresent(DAY_OF_WEEK,  FieldExpression.always())
                .instance();
    }
    public static Cron annually(final CronDefinition definition){
        return yearly(definition);
    }

    public static Cron monthly(final CronDefinition definition) {
        return new CronBuilder(definition)
                .applyIfPresent(SECOND,       new On(new IntegerFieldValue(0)))
                .applyIfPresent(MINUTE,       new On(new IntegerFieldValue(0)))
                .applyIfPresent(HOUR,         new On(new IntegerFieldValue(0)))
                .applyIfPresent(DAY_OF_MONTH, new On(new IntegerFieldValue(1)))
                .applyIfPresent(MONTH,        FieldExpression.always())
                .applyIfPresent(DAY_OF_WEEK,  FieldExpression.always())
                .instance();
    }
    public static Cron weekly(final CronDefinition definition) {
        return new CronBuilder(definition)
                .applyIfPresent(SECOND,       new On(new IntegerFieldValue(0)))
                .applyIfPresent(MINUTE,       new On(new IntegerFieldValue(0)))
                .applyIfPresent(HOUR,         new On(new IntegerFieldValue(0)))
                .applyIfPresent(DAY_OF_MONTH, FieldExpression.always())
                .applyIfPresent(MONTH,        FieldExpression.always())
                .applyIfPresent(DAY_OF_WEEK,  new On(new IntegerFieldValue(0)))
                .instance();
    }

    public static Cron daily(final CronDefinition definition) {
        return new CronBuilder(definition)
                .applyIfPresent(SECOND,       new On(new IntegerFieldValue(0)))
                .applyIfPresent(MINUTE,       new On(new IntegerFieldValue(0)))
                .applyIfPresent(HOUR,         new On(new IntegerFieldValue(0)))
                .applyIfPresent(DAY_OF_MONTH, FieldExpression.always())
                .applyIfPresent(MONTH,        FieldExpression.always())
                .applyIfPresent(DAY_OF_WEEK,  FieldExpression.always())
                .instance();
    }

    public static Cron midnight(final CronDefinition definition){
        return daily(definition);
    }

    public static Cron hourly(final CronDefinition definition) {
        return new CronBuilder(definition)
                .applyIfPresent(SECOND,       new On(new IntegerFieldValue(0)))
                .applyIfPresent(MINUTE,       new On(new IntegerFieldValue(0)))
                .applyIfPresent(HOUR,         FieldExpression.always())
                .applyIfPresent(DAY_OF_MONTH, FieldExpression.always())
                .applyIfPresent(MONTH,        FieldExpression.always())
                .applyIfPresent(DAY_OF_WEEK,  FieldExpression.always())
                .instance();
    }

    public static Cron reboot(final CronDefinition definition){
        return new RebootCron(definition);
    }

    @VisibleForTesting
    CronBuilder addField(final CronFieldName name, final FieldExpression expression) {
        checkState(definition != null, "CronBuilder not initialized.");

        final FieldDefinition fieldDefinition = definition.getFieldDefinition(name);
        checkState(fieldDefinition != null, "Cron field definition does not exist: %s", name);

        final FieldConstraints constraints = fieldDefinition.getConstraints();
        expression.accept(new ValidationFieldExpressionVisitor(constraints));
        fields.put(name, new CronField(name, expression, constraints));

        return this;
    }
}
