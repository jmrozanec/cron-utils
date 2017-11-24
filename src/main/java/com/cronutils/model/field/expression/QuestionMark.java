package com.cronutils.model.field.expression;

/**
 * Represents a question mark (?) value on cron expression field.
 */
public final class QuestionMark extends FieldExpression {

    static final QuestionMark INSTANCE = new QuestionMark();

    /**
     * Should be package private and not be instantiated elsewhere. Class should become package private too.
     */
    private QuestionMark() {
    }

    @Override
    public String asString() {
        return "?";
    }

    @Override
    public String toString() {
        return "QuestionMark{}";
    }
}
