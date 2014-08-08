package com.cron.utils.descriptor;

import com.cron.utils.parser.field.Always;
import com.cron.utils.parser.field.CronFieldExpression;
import com.cron.utils.parser.field.FieldConstraints;
import com.google.common.base.Function;
import com.google.common.collect.Sets;

import java.util.ResourceBundle;
import java.util.Set;

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
class NominalDescriptionStrategy extends DescriptionStrategy {
    private CronFieldExpression expression;
    private Set<Function<CronFieldExpression, String>> descriptions;

    public NominalDescriptionStrategy(ResourceBundle bundle, Function<Integer, String> nominalValueFunction, CronFieldExpression expression) {
        super(bundle);
        descriptions = Sets.newHashSet();
        if (nominalValueFunction != null) {
            this.nominalValueFunction = nominalValueFunction;
        }
        if (expression != null) {
            this.expression = expression;
        } else {
            this.expression = new Always(FieldConstraints.nullConstraints());
        }
    }

    @Override
    public String describe() {
        for (Function<CronFieldExpression, String> function : descriptions) {
            if (!"".equals(function.apply(expression))) {
                return function.apply(expression);
            }
        }
        return describe(expression);
    }

    public NominalDescriptionStrategy addDescription(Function<CronFieldExpression, String> desc) {
        descriptions.add(desc);
        return this;
    }
}
