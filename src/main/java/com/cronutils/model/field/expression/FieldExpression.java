package com.cronutils.model.field.expression;

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.visitor.FieldExpressionVisitor;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import org.apache.commons.lang3.Validate;

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
public abstract class FieldExpression {

    protected FieldConstraints constraints;

    public FieldExpression(FieldConstraints constraints) {
        Validate.notNull(constraints, "FieldConstraints cannot be null");
        this.constraints = constraints;
    }

    public And and(FieldExpression exp) {
        return new And().and(this).and(exp);
    }

    protected FieldConstraints getConstraints() {
        return constraints;
    }

    protected FieldValue validate(FieldValue fieldValue){
        if(fieldValue instanceof IntegerFieldValue){
            getConstraints().validateInRange(getConstraints().intToInt(((IntegerFieldValue)fieldValue).getValue()));
        }else{
            getConstraints().isSpecialCharAllowed(((SpecialCharFieldValue)fieldValue).getValue());
        }
        return fieldValue;
    }

    public abstract String asString();

    /**
     * This method performs a copy by constructor.
     * We do not force copy by constructor using FieldExpression(FieldExpression)
     * to avoid need to check correct subclass on instantiation.
     * Invocation of copy constructor is delegated to this method, so we can use it
     * at abstract class level.
     * @return new FieldExpression instance.
     */
    protected abstract FieldExpression copyInstanceByConstructor();

    /**
     * Accept a visitor to perform some action on the instance.
     * Current instance is cloned, so that we ensure immutability.
     * Clone of this instance is returned after visitor.visit(clone) was invoked.
     * @param visitor - FieldExpressionVisitor instance, never null
     * @return FieldExpression copied instance with visitor action performed.
     */
    public final FieldExpression accept(FieldExpressionVisitor visitor){
        Validate.notNull(visitor, "FieldExpressionVisitor must not be null");
        FieldExpression clone = copyInstanceByConstructor();
        visitor.visit(clone);
        return clone;
    }
}
