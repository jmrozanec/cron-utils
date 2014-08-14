package com.cron.utils.parser;

import com.cron.utils.model.Cron;
import com.cron.utils.model.CronDefinition;
import com.cron.utils.model.FieldDefinition;
import com.cron.utils.parser.field.CronField;
import com.cron.utils.parser.field.CronParserField;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

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
public class CronParser {
    private Map<Integer, List<CronParserField>> expressions;

    public CronParser(CronDefinition definition) {
        expressions = Maps.newHashMap();
        buildPossibleExpressions(definition);
    }

    private void buildPossibleExpressions(CronDefinition cronDefinition) {
        List<CronParserField> expression = new ArrayList<CronParserField>();
        for(FieldDefinition fieldDefinition : cronDefinition.getFieldDefinitions()){
            expression.add(new CronParserField(fieldDefinition.getFieldName(), fieldDefinition.getConstraints()));
        }
        Collections.sort(expression, CronParserField.createFieldTypeComparator());
        expressions.put(expression.size(), expression);

        if (cronDefinition.isLastFieldOptional()) {
            List<CronParserField> shortExpression = new ArrayList<CronParserField>();
            shortExpression.addAll(expression);
            shortExpression.remove(shortExpression.size() - 1);
            expressions.put(shortExpression.size(), shortExpression);
        }
    }

    public Cron parse(String expression) {
        if (StringUtils.isEmpty(expression)) {
            throw new IllegalArgumentException("Empty expression!");
        }
        expression = expression.toUpperCase();
        expression = expression.replace("?", "*");
        String[] expressionParts = expression.split(" ");
        if (expressions.containsKey(expressionParts.length)) {
            List<CronField> results = new ArrayList<CronField>();
            List<CronParserField> fields = expressions.get(expressionParts.length);
            for (int j = 0; j < fields.size(); j++) {
                results.add(fields.get(j).parse(expressionParts[j]));
            }
            return new Cron(results);
        } else {
            throw new IllegalArgumentException("Expressions size do not match registered options!");
        }
    }
}
