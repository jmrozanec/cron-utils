package com.cron.utils.parser;

import com.cron.utils.parser.field.CronField;
import com.cron.utils.parser.field.CronFieldParseResult;
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
    private Map<Integer, List<CronField>> expressions;

    public CronParser(Set<CronField> fieldsSet, boolean lastFieldOptional) {
        expressions = new HashMap<Integer, List<CronField>>();
        buildPossibleExpressions(fieldsSet, lastFieldOptional);
    }

    private void buildPossibleExpressions(Set<CronField> fieldsSet, boolean lastFieldOptional) {
        List<CronField> expression = new ArrayList<CronField>();
        expression.addAll(fieldsSet);
        Collections.sort(expression, CronField.createFieldTypeComparator());
        expressions.put(expression.size(), expression);

        if (lastFieldOptional) {
            List<CronField> shortExpression = new ArrayList<CronField>();
            shortExpression.addAll(expression);
            shortExpression.remove(shortExpression.size() - 1);
            expressions.put(shortExpression.size(), shortExpression);
        }
    }

    public List<CronFieldParseResult> parse(String expression) {
        if (StringUtils.isEmpty(expression)) {
            throw new IllegalArgumentException("Empty expression!");
        }
        expression = expression.toUpperCase();
        expression = expression.replace("?", "*");
        String[] expressionParts = expression.split(" ");
        if (expressions.containsKey(expressionParts.length)) {
            List<CronFieldParseResult> results = new ArrayList<CronFieldParseResult>();
            List<CronField> fields = expressions.get(expressionParts.length);
            for (int j = 0; j < fields.size(); j++) {
                results.add(fields.get(j).parse(expressionParts[j]));
            }
            return results;
        } else {
            throw new IllegalArgumentException("Expressions size do not match registered options!");
        }
    }
}
