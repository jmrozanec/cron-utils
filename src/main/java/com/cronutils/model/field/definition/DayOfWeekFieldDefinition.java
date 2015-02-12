package com.cronutils.model.field.definition;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;

public class DayOfWeekFieldDefinition extends FieldDefinition {
    private WeekDay mondayDoWValue;

    /**
     * Constructor
     *
     * @param fieldName   - CronFieldName; name of the field
     *                    if null, a NullPointerException will be raised.
     * @param constraints - FieldConstraints, constraints;
     */
    public DayOfWeekFieldDefinition(CronFieldName fieldName, FieldConstraints constraints, WeekDay mondayDoWValue) {
        super(fieldName, constraints);
        constraints.validateInRange(mondayDoWValue.getMondayDoWValue());
        this.mondayDoWValue = mondayDoWValue;
    }

    public WeekDay getMondayDoWValue() {
        return mondayDoWValue;
    }
}
