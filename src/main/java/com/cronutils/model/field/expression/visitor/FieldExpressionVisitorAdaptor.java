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

/**
 * Adaptor calss for the {@link FieldExpressionVisitor} interface.
 */
public class FieldExpressionVisitorAdaptor implements FieldExpressionVisitor {

    @Override
    public FieldExpression visit(Always always) {
        return this.caseDefault(always);
    }

    @Override
    public FieldExpression visit(And and) {
        return this.caseDefault(and);
    }

    @Override
    public FieldExpression visit(Between between) {
        return this.caseDefault(between);
    }

    @Override
    public FieldExpression visit(Every every) {
        return this.caseDefault(every);
    }

    @Override
    public FieldExpression visit(On on) {
        return this.caseDefault(on);
    }

    @Override
    public FieldExpression visit(QuestionMark questionMark) {
        return this.caseDefault(questionMark);
    }

    /**
     * Internal roll-up method.
     *
     * <p>
     * The implementation in {@link FieldExpressionVisitorAdaptor} returns its argument.
     */
    protected <T extends FieldExpression> T caseDefault(T expression) {
        return expression;
    }
}

