package com.cronutils.model.field.definition;

import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import org.apache.commons.lang3.Validate;

public class DayOfWeekFieldDefinition extends FieldDefinition {
    private int mondayDoWValue;

    /**
     * Constructor
     *
     * @param fieldName   - CronFieldName; name of the field
     *                    if null, a NullPointerException will be raised.
     * @param constraints - FieldConstraints, constraints;
     */
    public DayOfWeekFieldDefinition(CronFieldName fieldName, FieldConstraints constraints, int mondayDoWValue) {
        super(fieldName, constraints);
        constraints.validateInRange(mondayDoWValue);
        this.mondayDoWValue = mondayDoWValue;
    }

    public int getMondayDoWValue() {
        return mondayDoWValue;
    }
}
