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

package com.cronutils.parser;

import com.cronutils.builder.CronBuilder;
import com.cronutils.model.CompositeCron;
import com.cronutils.model.Cron;
import com.cronutils.model.SingleCron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronNicknames;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.utils.Preconditions;
import com.cronutils.utils.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Parser for cron expressions.
 * The class is thread safe.
 */
public class CronParser {

    private final Map<Integer, List<CronParserField>> expressions = new HashMap<>();
    private final CronDefinition cronDefinition;

    /**
     * @param cronDefinition - cronDefinition of cron expressions to be parsed if null, a NullPointerException will be raised.
     */
    public CronParser(final CronDefinition cronDefinition) {
        this.cronDefinition = Preconditions.checkNotNull(cronDefinition, "CronDefinition must not be null");
        buildPossibleExpressions(cronDefinition);
    }

    /**
     * Build possible cron expressions from definitions. One is built for sure. A second one may be build if last field is optional.
     *
     * @param cronDefinition - cron definition instance
     */
    private void buildPossibleExpressions(final CronDefinition cronDefinition) {
        final List<CronParserField> sortedExpression = cronDefinition.getFieldDefinitions().stream()
                .map(this::toCronParserField)
                .sorted(CronParserField.createFieldTypeComparator())
                .collect(Collectors.toList());

        List<CronParserField> tempExpression = sortedExpression;

        while(lastFieldIsOptional(tempExpression)) {
            int expressionLength = tempExpression.size() - 1;
            ArrayList<CronParserField> possibleExpression = new ArrayList<>(tempExpression.subList(0, expressionLength));

            expressions.put(expressionLength, possibleExpression);
            tempExpression = possibleExpression;
        }

        expressions.put(sortedExpression.size(), sortedExpression);
    }

    private CronParserField toCronParserField(final FieldDefinition fieldDefinition) {
        return new CronParserField(fieldDefinition.getFieldName(), fieldDefinition.getConstraints(), fieldDefinition.isOptional());
    }

    private boolean lastFieldIsOptional(final List<CronParserField> fields) {
        return !fields.isEmpty() && fields.get(fields.size() - 1).isOptional();
    }

    private Cron validateAndReturnSupportedCronNickname(String nickname, Set<CronNicknames> cronNicknames, CronNicknames cronNickname, Cron cron){
        if(cronNicknames.contains(cronNickname)){
            return cron;
        }
        throw new IllegalArgumentException(String.format("Nickname %s not supported!", nickname));
    }

    /**
     * Parse string with cron expression.
     *
     * @param expression - cron expression, never null
     * @return Cron instance, corresponding to cron expression received
     * @throws java.lang.IllegalArgumentException if expression does not match cron definition
     */
    public Cron parse(final String expression) {
        Preconditions.checkNotNull(expression, "Expression must not be null");
        final String replaced = expression.replaceAll("\\s+", " ").trim();
        if (StringUtils.isEmpty(replaced)) {
            throw new IllegalArgumentException("Empty expression!");
        }
        if (expression.startsWith("@"))  return parseNickname(expression);
        if (expression.contains("||"))   return parseDoubleOrExpression(expression);
        if (expression.contains("|"))    return parseSingleOrExpression(expression);
        return parsePlainExpression(replaced);
    }

    private Cron resolveNickname(final String expression,
                                 final Set<CronNicknames> cronNicknames,
                                 final CronNicknames nickname,
                                 final Cron cron) {
        return validateAndReturnSupportedCronNickname(
                expression, cronNicknames, nickname, cron);
    }

    private Cron parseNickname(final String expression) {
        Set<CronNicknames> cronNicknames = cronDefinition.getCronNicknames();
        if (cronNicknames.isEmpty()) {
            throw new IllegalArgumentException("Nicknames not supported!");
        }
        switch (expression) {
            case "@yearly":   return resolveNickname(expression, cronNicknames, CronNicknames.YEARLY,   CronBuilder.yearly(cronDefinition));
            case "@annually": return resolveNickname(expression, cronNicknames, CronNicknames.ANNUALLY, CronBuilder.annually(cronDefinition));
            case "@monthly":  return resolveNickname(expression, cronNicknames, CronNicknames.MONTHLY,  CronBuilder.monthly(cronDefinition));
            case "@weekly":   return resolveNickname(expression, cronNicknames, CronNicknames.WEEKLY,   CronBuilder.weekly(cronDefinition));
            case "@daily":    return resolveNickname(expression, cronNicknames, CronNicknames.DAILY,    CronBuilder.daily(cronDefinition));
            case "@midnight": return resolveNickname(expression, cronNicknames, CronNicknames.MIDNIGHT, CronBuilder.midnight(cronDefinition));
            case "@hourly":   return resolveNickname(expression, cronNicknames, CronNicknames.HOURLY,   CronBuilder.hourly(cronDefinition));
            case "@reboot":   return resolveNickname(expression, cronNicknames, CronNicknames.REBOOT,   CronBuilder.reboot(cronDefinition));
            default: throw new IllegalArgumentException(
                    String.format("Nickname %s not supported!", expression));
        }
    }

    private Cron parseDoubleOrExpression(final String expression) {
        List<Cron> crons = Arrays.stream(expression.split("\\|\\|"))
                .map(this::parse)
                .collect(Collectors.toList());
        return new CompositeCron(crons);
    }

    private Cron parseSingleOrExpression(final String expression) {
        int cronscount = Arrays.stream(expression.split("\\s+"))
                .mapToInt(s -> s.split("\\|").length)
                .max().orElse(0);

        List<String> crons = new ArrayList<>();
        for (int j = 0; j < cronscount; j++) {
            StringBuilder builder = new StringBuilder();
            for (String s : expression.split("\\s+")) {
                builder.append(String.format("%s ",
                        s.contains("|") ? s.split("\\|")[j] : s));
            }
            crons.add(builder.toString().trim());
        }
        return new CompositeCron(crons.stream().map(this::parse).collect(Collectors.toList()));
    }

    private Cron parsePlainExpression(final String replaced) {
        final String[] expressionParts = replaced.toUpperCase().split(" ");
        final int expressionLength = expressionParts.length;

        String fieldWithTrailingCommas = Arrays.stream(expressionParts)
                .filter(x -> x.endsWith(","))
                .findAny().orElse(null);
        if (fieldWithTrailingCommas != null) {
            throw new IllegalArgumentException(
                    String.format("Invalid field value! Trailing commas not permitted! '%s'",
                            fieldWithTrailingCommas));
        }

        final List<CronParserField> fields = expressions.get(expressionLength);
        if (fields == null) {
            throw new IllegalArgumentException(
                    String.format("Cron expression contains %s parts but we expect one of %s",
                            expressionLength, expressions.keySet()));
        }
        try {
            final List<CronField> results = new ArrayList<>(expressionParts.length + 1);
            for (int j = 0; j < expressionParts.length; j++) {
                results.add(fields.get(j).parse(expressionParts[j]));
            }
            return new SingleCron(cronDefinition, results).validate();
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("Failed to parse cron expression. %s", e.getMessage()), e);
        }
    }
}

