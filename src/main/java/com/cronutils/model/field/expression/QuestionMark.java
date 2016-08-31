package com.cronutils.model.field.expression;

/**
 * Represents a question mark (?) value on cron expression field
 */
public class QuestionMark extends FieldExpression {

    public QuestionMark(){}

    @Override
    public String asString() {
        return "?";
    }
}
