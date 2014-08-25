package com.cronutils.model.field;

import com.cronutils.model.field.constraint.FieldConstraintsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
/**
 * Represents a conjunction of cron expressions for a field.
 */
public class And extends FieldExpression {

    private List<FieldExpression> expressions;

    public And() {
        super(FieldConstraintsBuilder.instance().createConstraintsInstance());
        expressions = new ArrayList<FieldExpression>();
    }

    @Override
    public And and(FieldExpression exp) {
        expressions.add(exp);
        return this;
    }

    @Override
    public String asString() {
        StringBuilder builder = new StringBuilder();
        for(int j=0; j< expressions.size(); j++){
            builder.append(expressions.get(j).asString());
            if(j < expressions.size() -1){
                builder.append(",");
            }
        }
        return builder.toString();
    }

    public List<FieldExpression> getExpressions() {
        return Collections.unmodifiableList(expressions);
    }
}
