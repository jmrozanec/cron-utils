package com.cronutils.model.field.definition;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;

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
public class DayOfWeekFieldDefinition extends FieldDefinition {
    private WeekDay mondayDoWValue;

    /**
     * Constructor
     *
     * @param fieldName   - CronFieldName; name of the field
     *                    if null, a NullPointerException will be raised.
     * @param constraints - FieldConstraints, constraints;
     * @param mondayDoWValue - day of week convention for field
     *
     */
    public DayOfWeekFieldDefinition(CronFieldName fieldName, FieldConstraints constraints, WeekDay mondayDoWValue) {
        super(fieldName, constraints);
        this.mondayDoWValue = mondayDoWValue;
    }

    public WeekDay getMondayDoWValue() {
        return mondayDoWValue;
    }
}

