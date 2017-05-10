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

	private static final String OORANGE = "Value %s not in range [%s, %s]";
	private static final String EMPTY_STRING = "";

	private FieldConstraints constraints;
	private StringValidations stringValidations;
	private boolean strictRanges;

	public ValidationFieldExpressionVisitor(FieldConstraints constraints, boolean strictRanges) {
		this.constraints = constraints;
		this.stringValidations = new StringValidations(constraints);
		this.strictRanges = strictRanges;
	}

	protected ValidationFieldExpressionVisitor(FieldConstraints constraints, StringValidations stringValidation, boolean strictRanges) {
        this.constraints = constraints;
        this.stringValidations = stringValidation;
        this.strictRanges = strictRanges;
    }

	
	@Override
	public FieldExpression visit(FieldExpression expression) {
		String unsupportedChars = stringValidations.removeValidChars(expression.asString());
		if (EMPTY_STRING.equals(unsupportedChars)) {
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
		}
		throw new IllegalArgumentException(
				String.format("Invalid chars in expression! Expression: %s Invalid chars: %s",
						expression.asString(), unsupportedChars)
		);
	}

	@Override
	public Always visit(Always always) {
		return always;
	}

    @Override
    public And visit(And and) {
        for(FieldExpression expression: and.getExpressions()) {
            visit(expression);
        }
        return and;
    }

	@Override
	public Between visit(Between between) {
		preConditions(between);

		if (strictRanges && between.getFrom() instanceof IntegerFieldValue && between.getTo() instanceof IntegerFieldValue) {
			int from = ((IntegerFieldValue) between.getFrom()).getValue();
			int to = ((IntegerFieldValue) between.getTo()).getValue();
			if (from > to) {
				throw new IllegalArgumentException(String.format("Invalid range! [%s,%s]", from, to));
			}
		}

		return between;
	}

	private void preConditions(Between between) {
		isInRange(between.getFrom());
		isInRange(between.getTo());
		if (isSpecialCharNotL(between.getFrom()) || isSpecialCharNotL(between.getTo())) {
			throw new IllegalArgumentException("No special characters allowed in range, except for 'L'");
		}
	}

	@Override
	public Every visit(Every every) {
		if (every.getExpression() instanceof Between) {
			visit((Between) every.getExpression());
		}
		if (every.getExpression() instanceof On) {
			visit((On) every.getExpression());
		}
		isPeriodInRange(every.getPeriod());
		return every;
	}

	@Override
	public On visit(On on) {
		if (!isDefault(on.getTime())) {
			isInRange(on.getTime());
		}
		if (!isDefault(on.getNth())) {
			isInRange(on.getNth());
		}
		return on;
	}

	@Override
	public QuestionMark visit(QuestionMark questionMark) {
		return questionMark;
	}

	/**
	 * Check if given number is greater or equal to start range and minor or equal to end range
	 * 
	 * @param fieldValue
	 *            - to be validated
	 * @throws IllegalArgumentException
	 *             - if not in range
	 */
	@VisibleForTesting
	protected void isInRange(FieldValue<?> fieldValue) {
		if (fieldValue instanceof IntegerFieldValue) {
			int value = ((IntegerFieldValue) fieldValue).getValue();
			if (!constraints.isInRange(value)) {
				throw new IllegalArgumentException(String.format(OORANGE, value, constraints.getStartRange(), constraints.getEndRange()));
			}
		}
	}
	
	/**
     * Check if given period is compatible with range
     * 
     * @param fieldValue
     *            - to be validated
     * @throws IllegalArgumentException
     *             - if not in range
     */
    @VisibleForTesting
    protected void isPeriodInRange(FieldValue<?> fieldValue) {
        if (fieldValue instanceof IntegerFieldValue) {
            int value = ((IntegerFieldValue) fieldValue).getValue();
            if (!constraints.isPeriodInRange(value)) {
                throw new IllegalArgumentException(String.format("Period %s not in range (0, %s]", value, constraints.getEndRange()-constraints.getStartRange()));
            }
        }
    }

	@VisibleForTesting
	protected boolean isDefault(FieldValue<?> fieldValue) {
		return fieldValue instanceof IntegerFieldValue && ((IntegerFieldValue) fieldValue).getValue() == -1;
	}

	protected boolean isSpecialCharNotL(FieldValue<?> fieldValue) {
		return fieldValue instanceof SpecialCharFieldValue && !SpecialChar.L.equals(fieldValue.getValue());
	}
}
