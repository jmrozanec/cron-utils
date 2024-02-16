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

import com.cronutils.model.field.expression.*;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;

import java.util.function.Function;

import static com.cronutils.model.field.expression.FieldExpression.questionMark;

/**
 * Performs a transformation on FieldExpression values.
 * Returns a new FieldExpression instance considering a possible change
 * in new FieldExpression instance constraints.
 */
public class ValueMappingFieldExpressionVisitor implements FieldExpressionVisitor {
    private final Function<FieldValue<?>, FieldValue<?>> transform;

    public ValueMappingFieldExpressionVisitor(final Function<FieldValue<?>, FieldValue<?>> transform) {
        this.transform = transform;
    }

    @Override
    public FieldExpression visit(final Always always) {
        return always;
    }

    @Override
    public FieldExpression visit(final And and) {
        final And clone = new And();
        for (final FieldExpression expression : and.getExpressions()) {
            clone.and(expression.accept(this));
        }
        return clone;
    }

    @Override
    public FieldExpression visit(final Between between) {
        final FieldValue<?> from = transform.apply(between.getFrom());
        final FieldValue<?> to = transform.apply(between.getTo());
        return new Between(from, to);
    }

    @Override
    public FieldExpression visit(final Every every) {
        return new Every((IntegerFieldValue) transform.apply(every.getPeriod()));
    }

    @Override
    public FieldExpression visit(final On on) {
        return new On((IntegerFieldValue) transform.apply(on.getTime()), on.getSpecialChar(), on.getNth());
    }

    @Override
    public FieldExpression visit(final QuestionMark questionMark) {
        return questionMark();
    }
}

