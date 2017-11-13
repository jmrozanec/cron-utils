package com.cronutils.descriptor.refactor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.model.field.expression.visitor.FieldExpressionVisitor;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.utils.Preconditions;
import com.cronutils.utils.VisibleForTesting;

class SecondsDescriptor implements FieldExpressionVisitor {
    protected ResourceBundle bundle;

    SecondsDescriptor(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Given a CronFieldExpression, provide a String with a human readable description.
     * Will identify CronFieldExpression subclasses and delegate.
     *
     * @param fieldExpression - CronFieldExpression instance - not null
     * @return human readable description - String
     */
    public String describe(FieldExpression fieldExpression) {
        return describe(fieldExpression, false);
    }

    /**
     * Given a CronFieldExpression, provide a String with a human readable description.
     * Will identify CronFieldExpression subclasses and delegate.
     *
     * @param fieldExpression - CronFieldExpression instance - not null
     * @param and             - boolean expression that indicates if description should fit an "and" context
     * @return human readable description - String
     */
    protected String describe(FieldExpression fieldExpression, boolean and) {
        Preconditions.checkNotNull(fieldExpression, "CronFieldExpression should not be null!");
        if (fieldExpression instanceof Always) {
            return describe((Always) fieldExpression, and);
        }
        if (fieldExpression instanceof And) {
            return describe((And) fieldExpression);
        }
        if (fieldExpression instanceof Between) {
            return describe((Between) fieldExpression, and);
        }
        if (fieldExpression instanceof Every) {
            return describe((Every) fieldExpression, and);
        }
        if (fieldExpression instanceof On) {
            return describe((On) fieldExpression, and);
        }
        return "";
    }

    /**
     * Provide a human readable description for Always instance.
     *
     * @param always - Always
     * @return human readable description - String
     */
    protected String describe(Always always, boolean and) {
        return bundle.getString("every");
    }

    /**
     * Provide a human readable description for And instance.
     *
     * @param and - And
     * @return human readable description - String
     */
    protected String describe(And and) {
        List<FieldExpression> expressions = new ArrayList<>();
        List<FieldExpression> onExpressions = new ArrayList<>();
        for (FieldExpression fieldExpression : and.getExpressions()) {
            if (fieldExpression instanceof On) {
                onExpressions.add(fieldExpression);
            } else {
                expressions.add(fieldExpression);
            }
        }
        StringBuilder builder = new StringBuilder();
        if (!onExpressions.isEmpty()) {
            builder.append(bundle.getString("at"));
            createAndDescription(builder, onExpressions);
        }
        if (!expressions.isEmpty()) {
            createAndDescription(builder, expressions);
        }

        return builder.toString();
    }

    /**
     * Provide a human readable description for Between instance.
     *
     * @param between - Between
     * @return human readable description - String
     */
    protected String describe(Between between, boolean and) {
        return new StringBuilder()
                .append(
                        MessageFormat.format(
                                bundle.getString("between_x_and_y"),
                                nominalValue(between.getFrom()),
                                nominalValue(between.getTo())
                        )
                )
                .append(" ").toString();
    }

    protected String describe(Every every, boolean and) {
        return null;
    }

    /**
     * Provide a human readable description for On instance.
     *
     * @param on - On
     * @return human readable description - String
     */
    protected String describe(On on, boolean and) {
        if (and) {
            return nominalValue(on.getTime());
        }
        return String.format("%s %s ", bundle.getString("at"), nominalValue(on.getTime())) + "%s";
    }

    /**
     * Given an int, will return a nominal value. Example:
     * 1 in weeks context, may mean "Monday",
     * so nominal value for 1 would be "Monday"
     * Default will return int as String
     *
     * @param fieldValue - some FieldValue
     * @return String
     */
    protected String nominalValue(FieldValue fieldValue) {
        Preconditions.checkNotNull(fieldValue, "FieldValue must not be null");
        if (fieldValue instanceof IntegerFieldValue) {
            return "" + ((IntegerFieldValue) fieldValue).getValue();
        }
        return fieldValue.toString();
    }

    /**
     * Creates human readable description for And element.
     *
     * @param builder     - StringBuilder instance to which description will be appended
     * @param expressions - field expressions
     * @return same StringBuilder instance as parameter
     */
    @VisibleForTesting
    StringBuilder createAndDescription(StringBuilder builder, List<FieldExpression> expressions) {
        if ((expressions.size() - 2) >= 0) {
            for (int j = 0; j < expressions.size() - 2; j++) {
                builder.append(String.format(" %s, ", describe(expressions.get(j), true)));
            }
            builder.append(String.format(" %s ", describe(expressions.get(expressions.size() - 2), true)));
        }
        builder.append(String.format(" %s ", bundle.getString("and")));
        builder.append(describe(expressions.get(expressions.size() - 1), true));
        return builder;
    }

    @Override
    public FieldExpression visit(FieldExpression expression) {
        return null;
    }

    @Override
    public Always visit(Always always) {
        return null;
    }

    @Override
    public And visit(And and) {
        return null;
    }

    @Override
    public Between visit(Between between) {
        return null;
    }

    /**
     * Provide a human readable description for Every instance.
     *
     * @param every - Every
     * @return human readable description - String
     */
    @Override
    public Every visit(Every every) {
        String description;
        if (every.getPeriod().getValue() > 1) {
            description = String.format("%s %s ", bundle.getString("every"), nominalValue(every.getPeriod())) + " %p ";
        } else {
            description = bundle.getString("every") + " %s ";
        }
        //TODO save the description?
        return every;
    }

    @Override
    public On visit(On on) {
        return null;
    }

    @Override
    public QuestionMark visit(QuestionMark questionMark) {
        return null;
    }
}
