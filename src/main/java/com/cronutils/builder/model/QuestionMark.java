package com.cronutils.builder.model;

/**
 * Represents a question mark (?) value on cron expression field
 */
public class QuestionMark extends FieldExpression {

    private QuestionMark(){}

    private QuestionMark(QuestionMark questionMark) {
        this();
    }

    @Override
    public String asString() {
        return "?";
    }
}
