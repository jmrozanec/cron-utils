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

package com.cronutils.model.field.expression;

import com.cronutils.model.field.expression.visitor.FieldExpressionVisitor;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.utils.Preconditions;

/**
 * Represents every x time on a cron field.
 * Usage examples:
 * - To represent a scheduling every 3 months on a specific time (the standard 0 0 0 *&#47;3 *), use the Every(3) constructor
 * - To represent a scheduling every 3 months FROM NOW, use the Every(on(now.getMonth, 3)) constructor */
public class Every extends FieldExpression {

    private static final long serialVersionUID = -1103196842332906994L;
    private final FieldExpression expression;
    private final IntegerFieldValue period;

    public Every(final IntegerFieldValue time) {
        this(Always.always(), time);
    }

    public Every(final FieldExpression expression, final IntegerFieldValue period) {
        this.expression = Preconditions.checkNotNull(expression, "Expression must not be null");
        this.period = period == null ? new IntegerFieldValue(1) : period;
    }

    public IntegerFieldValue getPeriod() {
        return period;
    }

    public FieldExpression getExpression() {
        return expression;
    }

    @Override
    public FieldExpression accept(FieldExpressionVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String asString() {
        final String expressionAsString = expression.asString();
        if ("*".equals(expressionAsString) && period.getValue() == 1) {
            return expressionAsString;
        }
        return String.format("%s/%s", expressionAsString, period);
    }
}
