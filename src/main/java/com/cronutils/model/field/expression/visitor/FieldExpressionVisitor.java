package com.cronutils.model.field.expression.visitor;

import com.cronutils.model.field.expression.*;

public interface FieldExpressionVisitor {

    FieldExpression visit(FieldExpression expression);

    Always visit(Always always);
    And visit(And and);
    Between visit(Between between);
    Every visit(Every every);
    On visit(On on);
}
