package com.cronutils.model.field.expression.visitor;

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.*;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.google.common.base.Function;

/**
 * Performs a transformation on FieldExpression values.
 * Returns a new FieldExpression instance considering a possible change
 * in new FieldExpression instance constraints.
 */
public class ValueMappingFieldExpressionVisitor implements FieldExpressionVisitor {
    private FieldConstraints destinationConstraint;
    private Function<FieldValue, FieldValue> transform;

    public ValueMappingFieldExpressionVisitor(FieldConstraints destinationConstraint, Function<FieldValue, FieldValue> transform){
        this.destinationConstraint = destinationConstraint;
        this.transform = transform;
    }

    @Override
    public Always visit(Always always) {
        return always;
    }

    @Override
    public And visit(And and) {
        And clone = new And();
        for(FieldExpression expression : and.getExpressions()){
            clone.and(visit(expression));
        }
        return clone;
    }

    @Override
    public Between visit(Between between) {
        FieldValue from = transform.apply(between.getFrom());
        FieldValue to = transform.apply(between.getTo());
        return new Between(destinationConstraint, from, to, between.getEvery().getTime());
    }

    @Override
    public Every visit(Every every) {
        return new Every(destinationConstraint, (IntegerFieldValue)transform.apply(every.getTime()));
    }

    @Override
    public On visit(On on) {
        return new On(destinationConstraint, (IntegerFieldValue)transform.apply(on.getTime()), on.getSpecialChar(), on.getNth());
    }

    @Override
    public FieldExpression visit(FieldExpression expression) {
        if(expression instanceof Always){
            return visit((Always)expression);
        }
        if(expression instanceof And){
            return visit((And)expression);
        }
        if(expression instanceof Between){
            return visit((Between)expression);
        }
        if(expression instanceof Every){
            return visit((Every)expression);
        }
        if(expression instanceof On){
            return visit((On)expression);
        }
        return expression;
    }
}
