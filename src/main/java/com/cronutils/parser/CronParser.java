package com.cronutils.parser;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.parser.field.CronParserField;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
 * Parser for cron expressions
 */
public class CronParser {
    private CronDefinition cronDefinition;
    private Map<Integer, List<CronParserField>> expressions;

    /**
     * Constructor
     * @param cronDefinition - cronDefinition of cron expressions to be parsed
     *                   if null, a NullPointerException will be raised.
     */
    public CronParser(CronDefinition cronDefinition) {
        expressions = Maps.newHashMap();
        this.cronDefinition = Validate.notNull(cronDefinition, "CronDefinition must not be null");
        buildPossibleExpressions(cronDefinition);
    }

    /**
     * Build possible cron expressions from definitions.
     * One is built for sure. A second one may be build if last field is optional.
     * @param cronDefinition - cron definition instance
     */
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

    /**
     * Parse string with cron expression
     * @param expression - cron expression, never null
     * @return Cron instance, corresponding to cron expression received
     */
    public Cron parse(String expression) {
        Validate.notNull(expression, "Expression must not be null");
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
            return new Cron(cronDefinition, results);
        } else {
            throw new IllegalArgumentException(String.format("Cron expression contains %s parts but we expect one of %s", expressionParts.length, expressions.keySet()));
        }
    }
}
