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
 * Visitor for custom actions performed on FieldExpression instances.
 *
 * <p>
 * If a method needs to modify some value, it should return a new instance.
 * This way we ensure immutability is preserved.
 *
 * @see FieldExpressionVisitorAdaptor
 */
public interface FieldExpressionVisitor {

    /**
     * Performs action on Always instance.
     *
     * @param always - Always instance, never null
     * @return FieldExpression instance, never null
     */
    FieldExpression visit(Always always);

    /**
     * Performs action on And instance.
     *
     * @param and - And instance, never null
     * @return FieldExpression instance, never null
     */
    FieldExpression visit(And and);

    /**
     * Performs action on Between instance.
     *
     * @param between - Between instance, never null
     * @return FieldExpression instance, never null
     */
    FieldExpression visit(Between between);

    /**
     * Performs action on Every instance.
     *
     * @param every - Every instance, never null
     * @return FieldExpression instance, never null
     */
    FieldExpression visit(Every every);

    /**
     * Performs action on On instance.
     *
     * @param on - On instance, never null
     * @return FieldExpression instance, never null
     */
    FieldExpression visit(On on);

    /**
     * Performs action on QuestionMark instance.
     *
     * @param questionMark - QuestionMark instance, never null
     * @return FieldExpression instance, never null
     */
    FieldExpression visit(QuestionMark questionMark);
}

