package com.cronutils.model.time;

import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.Always;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.On;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.time.generator.FieldValueGenerator;
import com.cronutils.model.time.generator.FieldValueGeneratorFactory;
import org.apache.commons.lang3.Validate;
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
/**
 * Builds required components to get previous/next execution to certain reference date
 */
class ExecutionTimeBuilder {
    private CronDefinition cronDefinition;
    private FieldValueGenerator yearsValueGenerator;
    private CronField daysOfWeekCronField;
    private CronField daysOfMonthCronField;

    private TimeNode months;
    private TimeNode hours;
    private TimeNode minutes;
    private TimeNode seconds;

    ExecutionTimeBuilder(CronDefinition cronDefinition){
        this.cronDefinition = cronDefinition;
        seconds = new TimeNode(
                FieldValueGeneratorFactory.forCronField(
                        new CronField(
                                CronFieldName.SECOND,
                                new On(
                                        FieldConstraintsBuilder.instance()
                                                .forField(CronFieldName.SECOND)
                                                .createConstraintsInstance(), "0")
                        )
                ).generateCandidates(0,59));
        yearsValueGenerator = FieldValueGeneratorFactory.forCronField(
                new CronField(
                        CronFieldName.YEAR,
                        new Always(
                                FieldConstraintsBuilder.instance()
                                        .forField(CronFieldName.YEAR)
                                        .createConstraintsInstance()
                        )
                )
        );
    }

    ExecutionTimeBuilder forSecondsMatching(CronField cronField){
        validate(CronFieldName.SECOND, cronField);
        seconds = new TimeNode(FieldValueGeneratorFactory.forCronField(cronField).generateCandidates(0,59));
        return this;
    }

    ExecutionTimeBuilder forMinutesMatching(CronField cronField){
        validate(CronFieldName.MINUTE, cronField);
        minutes = new TimeNode(FieldValueGeneratorFactory.forCronField(cronField).generateCandidates(0,59));
        return this;
    }

    ExecutionTimeBuilder forHoursMatching(CronField cronField){
        validate(CronFieldName.HOUR, cronField);
        hours = new TimeNode(FieldValueGeneratorFactory.forCronField(cronField).generateCandidates(0,59));
        return this;
    }

    ExecutionTimeBuilder forMonthsMatching(CronField cronField){
        validate(CronFieldName.MONTH, cronField);
        months = new TimeNode(FieldValueGeneratorFactory.forCronField(cronField).generateCandidates(1,12));
        return this;
    }

    ExecutionTimeBuilder forYearsMatching(CronField cronField){
        validate(CronFieldName.YEAR, cronField);
        yearsValueGenerator = FieldValueGeneratorFactory.forCronField(cronField);
        return this;
    }

    ExecutionTimeBuilder forDaysOfWeekMatching(CronField cronField){
        validate(CronFieldName.DAY_OF_WEEK, cronField);
        daysOfWeekCronField = cronField;
        return this;
    }

    ExecutionTimeBuilder forDaysOfMonthMatching(CronField cronField){
        validate(CronFieldName.DAY_OF_MONTH, cronField);
        daysOfMonthCronField = cronField;
        return this;
    }

    ExecutionTime build(){
        return new ExecutionTime(cronDefinition,
                yearsValueGenerator, daysOfWeekCronField, daysOfMonthCronField,
                months, hours, minutes, seconds
        );
    }

    private void validate(CronFieldName name, CronField cronField){
        Validate.notNull(name, "Reference CronFieldName cannot be null");
        Validate.notNull(cronField.getField(), "CronField's CronFieldName cannot be null");
        if(!name.equals(cronField.getField())){
            throw new IllegalArgumentException(
                    String.format("Invalid argument! Expected CronField instance for field %s but found %s", cronField.getField(), name)
            );
        }
    }
}
