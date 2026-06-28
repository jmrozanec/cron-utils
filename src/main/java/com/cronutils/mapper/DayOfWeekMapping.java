package com.cronutils.mapper;

import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.definition.DayOfWeekFieldDefinition;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;

public class DayOfWeekMapping {

    private final DayOfWeekFieldDefinition source;
    private final DayOfWeekFieldDefinition target;

    public DayOfWeekMapping(
            final DayOfWeekFieldDefinition source,
            final DayOfWeekFieldDefinition target) {
        this.source = source;
        this.target = target;
    }

    public IntegerFieldValue mapValue(final IntegerFieldValue value) {
        return new IntegerFieldValue(
                ConstantsMapper.weekDayMapping(
                        source.getMondayDoWValue(),
                        target.getMondayDoWValue(),
                        value.getValue()
                )
        );
    }

    public FieldValue<?> mapValue(final FieldValue<?> value) {
        if (value instanceof IntegerFieldValue) {
            return mapValue((IntegerFieldValue) value);
        }
        return value;
    }

    public FieldConstraints getTargetConstraints() {
        return target.getConstraints();
    }

    public boolean targetSupportsQuestionMark() {
        return target.getConstraints()
                .getSpecialChars()
                .contains(SpecialChar.QUESTION_MARK);
    }
}