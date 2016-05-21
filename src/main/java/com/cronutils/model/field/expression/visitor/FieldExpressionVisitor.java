package com.cronutils.model.field.expression.visitor;

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

import com.cronutils.model.field.expression.*;

/**
 * Visitor for custom actions performed on FieldExpression instances
 */
public interface FieldExpressionVisitor {
    /**
     * Performs an action using given FieldExpression instance.
     * If requires to modify some value,
     * should return a new instance with those values.
     * This way we ensure immutability is preserved.
     * @param expression - FieldExpression, never null
     * @return FieldExpression instance, never null
     */
    FieldExpression visit(FieldExpression expression);

    /**
     * Performs action on Always instance
     * @param always - Always instance, never null
     * @return FieldExpression instance, never null
     */
    FieldExpression visit(Always always);

    /**
     * Performs action on And instance
     * @param and - And instance, never null
     * @return FieldExpression instance, never null
     */
    FieldExpression visit(And and);

    /**
     * Performs action on Between instance
     * @param between - Between instance, never null
     * @return FieldExpression instance, never null
     */
    FieldExpression visit(Between between);

    /**
     * Performs action on Every instance
     * @param every - Every instance, never null
     * @return FieldExpression instance, never null
     */
    FieldExpression visit(Every every);

    /**
     * Performs action on On instance
     * @param on - On instance, never null
     * @return FieldExpression instance, never null
     */
    FieldExpression visit(On on);

    /**
     * Performs action on QuestionMark instance
     * @param questionMark - QuestionMark instance, never null
     * @return FieldExpression instance, never null
     */
    FieldExpression visit(QuestionMark questionMark);
}

