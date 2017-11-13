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

package com.cronutils.model.field.expression.visitor;

import com.cronutils.Function;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;

import static com.cronutils.model.field.expression.FieldExpression.questionMark;

/**
 * Performs a transformation on FieldExpression values.
 * Returns a new FieldExpression instance considering a possible change
 * in new FieldExpression instance constraints.
 */
public class ValueMappingFieldExpressionVisitor implements FieldExpressionVisitor {
    private Function<FieldValue, FieldValue> transform;

    public ValueMappingFieldExpressionVisitor(Function<FieldValue, FieldValue> transform) {
        this.transform = transform;
    }

    @Override
    public FieldExpression visit(Always always) {
        return always;
    }

    @Override
    public FieldExpression visit(And and) {
        And clone = new And();
        for (FieldExpression expression : and.getExpressions()) {
            clone.and(visit(expression));
        }
        return clone;
    }

    @Override
    public FieldExpression visit(Between between) {
        FieldValue from = transform.apply(between.getFrom());
        FieldValue to = transform.apply(between.getTo());
        return new Between(from, to);
    }

    @Override
    public FieldExpression visit(Every every) {
        return new Every((IntegerFieldValue) transform.apply(every.getPeriod()));
    }

    @Override
    public FieldExpression visit(On on) {
        return new On((IntegerFieldValue) transform.apply(on.getTime()), on.getSpecialChar(), on.getNth());
    }

    @Override
    public FieldExpression visit(QuestionMark questionMark) {
        return questionMark();
    }

    @Override
    public FieldExpression visit(FieldExpression expression) {
        if (expression instanceof Always) {
            return visit((Always) expression);
        }
        if (expression instanceof And) {
            return visit((And) expression);
        }
        if (expression instanceof Between) {
            return visit((Between) expression);
        }
        if (expression instanceof Every) {
            return visit((Every) expression);
        }
        if (expression instanceof On) {
            return visit((On) expression);
        }
        if (expression instanceof QuestionMark) {
            return visit((QuestionMark) expression);
        }
        return expression;
    }
}

