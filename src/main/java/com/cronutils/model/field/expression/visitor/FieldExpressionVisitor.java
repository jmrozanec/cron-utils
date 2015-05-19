package com.cronutils.model.field.expression.visitor;

import com.cronutils.model.field.expression.*;

public interface FieldExpressionVisitor {

    void visit(FieldExpression expression);

    void visit(Always always);
    void visit(And and);
    void visit(Between between);
    void visit(Every every);
    void visit(On on);
}
