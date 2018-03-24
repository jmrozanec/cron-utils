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

package com.cronutils.model.time;

import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.time.generator.FieldValueGenerator;
import com.cronutils.model.time.generator.FieldValueGeneratorFactory;
import com.cronutils.utils.Preconditions;

import static com.cronutils.model.field.expression.FieldExpression.always;

/**
 * Builds required components to get previous/next execution to certain reference date.
 */
class ExecutionTimeBuilder {
    private final CronDefinition cronDefinition;
    private FieldValueGenerator yearsValueGenerator;
    private CronField daysOfWeekCronField;
    private CronField daysOfMonthCronField;
    private CronField daysOfYearCronField;

    private TimeNode months;
    private TimeNode hours;
    private TimeNode minutes;
    private TimeNode seconds;

    protected ExecutionTimeBuilder(final CronDefinition cronDefinition) {
        this.cronDefinition = cronDefinition;
    }

    protected ExecutionTimeBuilder forSecondsMatching(final CronField cronField) {
        validate(CronFieldName.SECOND, cronField);
        seconds = new TimeNode(FieldValueGeneratorFactory.forCronField(cronField).generateCandidates(0, 59));
        return this;
    }

    protected ExecutionTimeBuilder forMinutesMatching(final CronField cronField) {
        validate(CronFieldName.MINUTE, cronField);
        minutes = new TimeNode(FieldValueGeneratorFactory.forCronField(cronField).generateCandidates(0, 59));
        return this;
    }

    protected ExecutionTimeBuilder forHoursMatching(final CronField cronField) {
        validate(CronFieldName.HOUR, cronField);
        hours = new TimeNode(FieldValueGeneratorFactory.forCronField(cronField).generateCandidates(0, 23));
        return this;
    }

    protected ExecutionTimeBuilder forMonthsMatching(final CronField cronField) {
        validate(CronFieldName.MONTH, cronField);
        months = new TimeNode(FieldValueGeneratorFactory.forCronField(cronField).generateCandidates(1, 12));
        return this;
    }

    protected ExecutionTimeBuilder forYearsMatching(final CronField cronField) {
        validate(CronFieldName.YEAR, cronField);
        yearsValueGenerator = FieldValueGeneratorFactory.forCronField(cronField);
        return this;
    }

    protected ExecutionTimeBuilder forDaysOfWeekMatching(final CronField cronField) {
        validate(CronFieldName.DAY_OF_WEEK, cronField);
        daysOfWeekCronField = cronField;
        return this;
    }

    protected ExecutionTimeBuilder forDaysOfMonthMatching(final CronField cronField) {
        validate(CronFieldName.DAY_OF_MONTH, cronField);
        daysOfMonthCronField = cronField;
        return this;
    }

    protected ExecutionTimeBuilder forDaysOfYearMatching(final CronField cronField) {
        validate(CronFieldName.DAY_OF_YEAR, cronField);
        daysOfYearCronField = cronField;
        return this;
    }

    protected ExecutionTime build() {
        boolean lowestAssigned = false;
        if (seconds == null) {
            seconds = timeNodeLowest(CronFieldName.SECOND, 0, 59);
        } else {
            lowestAssigned = true;
        }
        if (minutes == null) {
            minutes = lowestAssigned ? timeNodeAlways(CronFieldName.MINUTE, 0, 59) : timeNodeLowest(CronFieldName.MINUTE, 0, 59);
        } else {
            lowestAssigned = true;
        }
        if (hours == null) {
            hours = lowestAssigned ? timeNodeAlways(CronFieldName.HOUR, 0, 23) : timeNodeLowest(CronFieldName.HOUR, 0, 23);
        } else {
            lowestAssigned = true;
        }
        if (daysOfMonthCronField == null) {
            final FieldConstraints constraints = getConstraint(CronFieldName.DAY_OF_MONTH);
            daysOfMonthCronField = lowestAssigned
                    ? new CronField(CronFieldName.DAY_OF_MONTH, always(), constraints)
                    : new CronField(CronFieldName.DAY_OF_MONTH, new On(new IntegerFieldValue(1)), constraints);
        } else {
            lowestAssigned = true;
        }
        if (daysOfWeekCronField == null) {
            final FieldConstraints constraints = getConstraint(CronFieldName.DAY_OF_WEEK);
            daysOfWeekCronField = lowestAssigned
                    ? new CronField(CronFieldName.DAY_OF_WEEK, always(), constraints)
                    : new CronField(CronFieldName.DAY_OF_WEEK, new On(new IntegerFieldValue(1)), constraints);
        } else {
            lowestAssigned = true;
        }
        if (months == null) {
            months = lowestAssigned ? timeNodeAlways(CronFieldName.MONTH, 1, 12) : timeNodeLowest(CronFieldName.MONTH, 1, 12);
        }
        if (yearsValueGenerator == null) {
            yearsValueGenerator =
                    FieldValueGeneratorFactory.forCronField(
                            new CronField(CronFieldName.YEAR, always(), getConstraint(CronFieldName.YEAR))
                    );
        }
        if (daysOfYearCronField == null) {
            final FieldConstraints constraints = getConstraint(CronFieldName.DAY_OF_YEAR);
            daysOfYearCronField = new CronField(CronFieldName.DAY_OF_YEAR, lowestAssigned ? FieldExpression.questionMark() : always(),
                    constraints);
        }

        return new SingleExecutionTime(cronDefinition,
                yearsValueGenerator, daysOfWeekCronField, daysOfMonthCronField, daysOfYearCronField,
                months, hours, minutes, seconds
        );
    }

    private TimeNode timeNodeLowest(final CronFieldName name, final int lower, final int higher) {
        final FieldConstraints constraints = getConstraint(name);
        return new TimeNode(
                FieldValueGeneratorFactory.forCronField(
                        new CronField(name, new On(new IntegerFieldValue(lower)), constraints)
                ).generateCandidates(lower, higher));
    }

    private TimeNode timeNodeAlways(final CronFieldName name, final int lower, final int higher) {
        return new TimeNode(
                FieldValueGeneratorFactory.forCronField(
                        new CronField(name, always(), getConstraint(name))
                ).generateCandidates(lower, higher));
    }

    private void validate(final CronFieldName name, final CronField cronField) {
        Preconditions.checkNotNull(name, "Reference CronFieldName cannot be null");
        Preconditions.checkNotNull(cronField.getField(), "CronField's CronFieldName cannot be null");
        if (!name.equals(cronField.getField())) {
            throw new IllegalArgumentException(
                    String.format("Invalid argument! Expected CronField instance for field %s but found %s", cronField.getField(), name)
            );
        }
    }

    private FieldConstraints getConstraint(final CronFieldName name) {
        return cronDefinition.getFieldDefinition(name) != null
                ? cronDefinition.getFieldDefinition(name).getConstraints()
                : FieldConstraintsBuilder.instance().forField(name).createConstraintsInstance();
    }
}
