package com.cronutils.model.field.expression;

/**
 * Represents a question mark (?) value on cron expression field.
 */
public final class QuestionMark extends FieldExpression {
    @SuppressWarnings("deprecation")
    static final QuestionMark INSTANCE = new QuestionMark();

    /**
     * Should be package private and not be instantiated elsewhere. Class should become package private too.
     *
     * @deprecated rather use {@link FieldExpression#questionMark()}
     */
    @Deprecated
    public QuestionMark() {
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
