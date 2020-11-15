

package com.cronutils.model.field.expression;

import com.cronutils.model.field.value.FieldValue;

/**
 * Represents a range in a cron expression.
 */
public class Between extends FieldExpression {

    private static final long serialVersionUID = 549075258664100474L;
    private final FieldValue<?> from;
    private final FieldValue<?> to;

    public Between(final Between between) {
        this(between.getFrom(), between.getTo());
    }

    public Between(final FieldValue<?> from, final FieldValue<?> to) {
        this.from = from;
        this.to = to;
    }

    public FieldValue<?> getFrom() {
        return from;
    }

    public FieldValue<?> getTo() {
        return to;
    }

    @Override
    public String asString() {
        return String.format("%s-%s", from, to);
    }
}
