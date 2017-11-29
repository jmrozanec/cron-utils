/*
 * Copyright 2014 jmrozanec
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

package com.cronutils.model.field.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a conjunction of cron expressions for a field.
 */
public class And extends FieldExpression {

    private static final long serialVersionUID = -3406340596495131941L;
    private final List<FieldExpression> expressions;

    public And() {
        expressions = new ArrayList<>();
    }

    @Override
    public And and(final FieldExpression exp) {
        expressions.add(exp);
        return this;
    }

    @Override
    public String asString() {
        final StringBuilder builder = new StringBuilder();
        for (int j = 0; j < expressions.size() - 1; j++) {
            builder.append(expressions.get(j).asString());
            builder.append(",");
        }
        builder.append(expressions.get(expressions.size() - 1).asString());
        return builder.toString();
    }

    public List<FieldExpression> getExpressions() {
        return Collections.unmodifiableList(expressions);
    }
}
