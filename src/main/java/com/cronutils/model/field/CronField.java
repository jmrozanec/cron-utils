package com.cronutils.model.field;

import java.io.Serializable;
import java.util.Comparator;

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.utils.Preconditions;

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
public class CronField implements Serializable {
    private CronFieldName field;
    private FieldExpression expression;
    private FieldConstraints constraints;

    public CronField(CronFieldName field, FieldExpression expression, FieldConstraints constraints) {
        this.field = field;
        this.expression = Preconditions.checkNotNull(expression, "FieldExpression must not be null");
        this.constraints = Preconditions.checkNotNull(constraints, "FieldConstraints must not be null");
    }

    public CronFieldName getField() {
        return field;
    }

    public FieldExpression getExpression() {
        return expression;
    }

    public FieldConstraints getConstraints() {
        return constraints;
    }

    public static Comparator<CronField> createFieldComparator() {
        return Comparator.comparingInt(o -> o.getField().getOrder());
    }

    @Override
    public String toString() {
        return "CronField{" + "field=" + field + '}';
    }
}

