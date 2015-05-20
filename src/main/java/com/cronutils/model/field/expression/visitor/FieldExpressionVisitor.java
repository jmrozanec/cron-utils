package com.cronutils.model.field.expression.visitor;

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
     * @return Always instance, never null
     */
    Always visit(Always always);

    /**
     * Performs action on And instance
     * @param and - And instance, never null
     * @return And instance, never null
     */
    And visit(And and);

    /**
     * Performs action on Between instance
     * @param between - Between instance, never null
     * @return Between instance, never null
     */
    Between visit(Between between);

    /**
     * Performs action on Every instance
     * @param every - Every instance, never null
     * @return Every instance, never null
     */
    Every visit(Every every);

    /**
     * Performs action on On instance
     * @param on - On instance, never null
     * @return On instance, never null
     */
    On visit(On on);
}
