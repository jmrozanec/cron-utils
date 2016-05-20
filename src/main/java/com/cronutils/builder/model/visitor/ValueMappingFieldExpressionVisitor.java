package com.cronutils.builder.model.visitor;

import com.cronutils.builder.model.expression.*;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.google.common.base.Function;

/*
 * Copyright 2015 jmrozanec
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Performs a transformation on FieldExpression values.
 * Returns a new FieldExpression instance considering a possible change
 * in new FieldExpression instance constraints.
 */
public class ValueMappingFieldExpressionVisitor implements FieldExpressionVisitor {
    private Function<FieldValue, FieldValue> transform;

    public ValueMappingFieldExpressionVisitor(Function<FieldValue, FieldValue> transform){
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
        return new Between(from, to, between.getEvery().getTime());
    }

    @Override
    public Every visit(Every every) {
        return new Every((IntegerFieldValue)transform.apply(every.getTime()));
    }

    @Override
    public On visit(On on) {
        return new On((IntegerFieldValue)transform.apply(on.getTime()), on.getSpecialChar(), on.getNth());
    }

    @Override
    public QuestionMark visit(QuestionMark questionMark) {
        return new QuestionMark();
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
        if(expression instanceof QuestionMark){
            return visit((QuestionMark)expression);
        }
        return expression;
    }
}

