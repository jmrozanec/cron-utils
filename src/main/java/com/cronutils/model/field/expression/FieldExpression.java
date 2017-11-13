package com.cronutils.model.field.expression;

import java.io.Serializable;

import com.cronutils.model.field.expression.visitor.FieldExpressionVisitor;
import com.cronutils.utils.Preconditions;

/*
 * Copyright 2015 jmrozanec
 * 
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
public abstract class FieldExpression implements Serializable {

    public And and(FieldExpression exp) {
        return new And().and(this).and(exp);
    }

    /**
     * Represents FieldExpression as string.
     *
     * @return String representation, never null.
     */
    public abstract String asString();

    /**
     * Accept a visitor to perform some action on the instance. Current instance is cloned, so that we ensure immutability. Clone of this
     * instance is returned after visitor.visit(clone) was invoked.
     *
     * @param visitor - FieldExpressionVisitor instance, never null
     * @return FieldExpression copied instance with visitor action performed.
     */
    public final FieldExpression accept(FieldExpressionVisitor visitor) {
        Preconditions.checkNotNull(visitor, "FieldExpressionVisitor must not be null");
        return visitor.visit(this);
    }

    public static FieldExpression always() {
        return Always.INSTANCE;
    }

    public static FieldExpression questionMark() {
        return QuestionMark.INSTANCE;
    }
}
