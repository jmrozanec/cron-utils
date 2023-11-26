/*
 * Copyright 2014 jmrozanec
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

import com.cronutils.StringValidations;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.*;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import com.cronutils.utils.VisibleForTesting;

public class ValidationFieldExpressionVisitor implements FieldExpressionVisitor {
    private final FieldConstraints constraints;
    private final StringValidations stringValidations;

    public ValidationFieldExpressionVisitor(final FieldConstraints constraints) {
        this.constraints = constraints;
        stringValidations = new StringValidations(constraints);
    }

    protected ValidationFieldExpressionVisitor(final FieldConstraints constraints, final StringValidations stringValidation) {
        this.constraints = constraints;
        stringValidations = stringValidation;
    }

    private void checkUnsupportedChars(final FieldExpression expression) {
        final String unsupportedChars = stringValidations.removeValidChars(expression.asString());
        if (unsupportedChars.isEmpty())
            return;
        throw new IllegalArgumentException(
                String.format("Invalid chars in expression! Expression: %s Invalid chars: %s",
                        expression.asString(), unsupportedChars)
        );
    }

    @Override
    public Always visit(final Always always) {
        this.checkUnsupportedChars(always);
        return always;
    }

    @Override
    public And visit(final And and) {
        this.checkUnsupportedChars(and);
        for (final FieldExpression expression : and.getExpressions()) {
            expression.accept(this);
        }
        return and;
    }

    @Override
    public Between visit(final Between between) {
        this.checkUnsupportedChars(between);
        preConditions(between);

        if (isIntegerStrictRange(between)) {
            final int from = ((IntegerFieldValue) between.getFrom()).getValue();
            final int to = ((IntegerFieldValue) between.getTo()).getValue();
            if (from > to) {
                throw new IllegalArgumentException(String.format("Invalid range! [%s,%s]", from, to));
            }
        }

        return between;
    }

    @Override
    public Every visit(final Every every) {
        this.checkUnsupportedChars(every);
        if (every.getExpression() != null)
            every.getExpression().accept(this);
        constraints.isPeriodInRange(every.getPeriod());
        return every;
    }

    @Override
    public On visit(final On on) {
        this.checkUnsupportedChars(on);
        if (!isDefault(on.getTime())) {
        	constraints.isInRange(on.getTime());
        }
        if (!isDefault(on.getNth())) {
        	constraints.isInRange(on.getNth());
        }
        return on;
    }

    @Override
    public QuestionMark visit(final QuestionMark questionMark) {
        this.checkUnsupportedChars(questionMark);
        return questionMark;
    }

    private void preConditions(final Between between) {
    	constraints.isInRange(between.getFrom());
    	constraints.isInRange(between.getTo());
        if (isSpecialCharNotL(between.getFrom()) || isSpecialCharNotL(between.getTo())) {
            throw new IllegalArgumentException("No special characters allowed in range, except for 'L'");
        }
    }

    @VisibleForTesting
    protected boolean isDefault(final FieldValue<?> fieldValue) {
        return fieldValue instanceof IntegerFieldValue && ((IntegerFieldValue) fieldValue).getValue() == -1;
    }

    protected boolean isSpecialCharNotL(final FieldValue<?> fieldValue) {
        return fieldValue instanceof SpecialCharFieldValue && !SpecialChar.L.equals(fieldValue.getValue());
    }
    
    private boolean isIntegerStrictRange(final Between between) {
    	return constraints.isStrictRange() 
    			&& between.getFrom() instanceof IntegerFieldValue 
    			&& between.getTo() instanceof IntegerFieldValue;
    }
}
