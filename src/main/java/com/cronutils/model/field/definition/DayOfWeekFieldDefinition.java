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

package com.cronutils.model.field.definition;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;

public class DayOfWeekFieldDefinition extends FieldDefinition {

    private static final long serialVersionUID = 8684844402711204711L;
    private final WeekDay mondayDoWValue;

    /**
     * Constructor.
     *
     * @param fieldName      - CronFieldName; name of the field
     *                       if null, a NullPointerException will be raised.
     * @param optional       - optional tag
     * @param constraints    - FieldConstraints, constraints;
     * @param mondayDoWValue - day of week convention for field
     */
    public DayOfWeekFieldDefinition(final CronFieldName fieldName, final FieldConstraints constraints, final boolean optional, final WeekDay mondayDoWValue) {
        super(fieldName, constraints, optional);
        this.mondayDoWValue = mondayDoWValue;
    }

    public WeekDay getMondayDoWValue() {
        return mondayDoWValue;
    }
}

