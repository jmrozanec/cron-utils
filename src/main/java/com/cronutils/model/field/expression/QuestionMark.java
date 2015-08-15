package com.cronutils.model.field.expression;

import com.cronutils.model.field.constraint.FieldConstraints;

/**
 * Represents a question mark (?) value on cron expression field
 */
public class QuestionMark extends FieldExpression {

    public QuestionMark(FieldConstraints constraints) {
        super(constraints);
    }

    private QuestionMark(QuestionMark always) {
        this(always.getConstraints());
    }

    @Override
    public String asString() {
        return "?";
    }
}
