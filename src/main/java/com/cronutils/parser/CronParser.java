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
        final String noExtraSpaceExpression = expression.replaceAll("\\s+", " ").trim();
        if (StringUtils.isEmpty(noExtraSpaceExpression)) {
            throw new IllegalArgumentException("Empty expression!");
        }

        if(expression.startsWith("@")){
        	Cron cron = parseNicknameExpression(expression);
        	if(cron != null) {
        		return cron;
        	}
        }

        if(expression.contains("||")) {
        	return parseCompositeExpression(expression);
        }
        if(expression.contains("|")){
        	return parseMultipleExpression(expression);
        }else{
        	return parseSingleExpression(noExtraSpaceExpression);
        }
    }
    
    /**
     * Parse string with nickname cron expressions
     * @param expression cron expression, never null
     * @return Cron instance
     * @throws java.lang.IllegalArgumentException if expression does not match cron definition
     */
    private Cron parseNicknameExpression(final String expression) {
    	Set<CronNicknames> cronNicknames = cronDefinition.getCronNicknames();
        if(cronNicknames.isEmpty()){
            throw new IllegalArgumentException("Nicknames not supported!");
        }
        switch (expression){
            case "@yearly":
                return validateAndReturnSupportedCronNickname(expression, cronNicknames, CronNicknames.YEARLY, CronBuilder.yearly(cronDefinition));
            case "@annually":
                return validateAndReturnSupportedCronNickname(expression, cronNicknames, CronNicknames.ANNUALLY, CronBuilder.annually(cronDefinition));
            case "@monthly":
                return validateAndReturnSupportedCronNickname(expression, cronNicknames, CronNicknames.MONTHLY, CronBuilder.monthly(cronDefinition));
            case "@weekly":
                return validateAndReturnSupportedCronNickname(expression, cronNicknames, CronNicknames.WEEKLY, CronBuilder.weekly(cronDefinition));
            case "@daily":
                return validateAndReturnSupportedCronNickname(expression, cronNicknames, CronNicknames.DAILY, CronBuilder.daily(cronDefinition));
            case "@midnight":
                return validateAndReturnSupportedCronNickname(expression, cronNicknames, CronNicknames.MIDNIGHT, CronBuilder.midnight(cronDefinition));
            case "@hourly":
                return validateAndReturnSupportedCronNickname(expression, cronNicknames, CronNicknames.HOURLY, CronBuilder.hourly(cronDefinition));
            case "@reboot":
                return validateAndReturnSupportedCronNickname(expression, cronNicknames, CronNicknames.REBOOT, CronBuilder.reboot(cronDefinition));
            default:
            	return null;
        }
    }
    
    /**
     * Parse string with composite cron expressions 
     * @param expression cron expression, never null
     * @return Cron instance
     * @throws java.lang.IllegalArgumentException if expression does not match cron definition
     */
    private Cron parseCompositeExpression(final String expression) {
        List<Cron> crons = Arrays.stream(expression.split("\\|\\|")).map(this::parse).collect(Collectors.toList());
        return new CompositeCron(crons);
    }
    
    /**
     * Parse string with multiple cron expressions 
     * @param expression cron expression, never null
     * @return Cron instance
     * @throws java.lang.IllegalArgumentException if expression does not match cron definition
     */
    private Cron parseMultipleExpression(final String expression) {
        List<String> crons = new ArrayList<>();
        int cronscount = Arrays.stream(expression.split("\\s+")).mapToInt(s->s.split("\\|").length).max().orElse(0);
        for(int j=0; j<cronscount; j++){
            StringBuilder builder = new StringBuilder();
            for(String s : expression.split("\\s+")){
                if(s.contains("|")){
                    builder.append(String.format("%s ", s.split("\\|")[j]));
                }else{
                    builder.append(String.format("%s ", s));
                }
            }
            crons.add(builder.toString().trim());
        }
        return new CompositeCron(crons.stream().map(this::parse).collect(Collectors.toList()));
    }
    
    /**
     * Parse string with single cron expression 
     * @param noExtraSpaceExpression cleaned cron expression, never null
     * @return Cron instance
     * @throws java.lang.IllegalArgumentException if expression does not match cron definition
     */
    private Cron parseSingleExpression(final String noExtraSpaceExpression) {
        final String[] expressionParts = noExtraSpaceExpression.toUpperCase().split(" ");
        final int expressionLength = expressionParts.length;
        String fieldWithTrailingCommas = Arrays.stream(expressionParts).filter(x -> x.endsWith(",")).findAny().orElse(null);
        if(fieldWithTrailingCommas!=null){
            throw new IllegalArgumentException(String.format("Invalid field value! Trailing commas not permitted! '%s'", fieldWithTrailingCommas));
        }
        final List<CronParserField> fields = expressions.get(expressionLength);
        if (fields == null) {
            throw new IllegalArgumentException(
                    String.format("Cron expression contains %s parts but we expect one of %s", expressionLength, expressions.keySet()));
        }
        try {
            final int size = expressionParts.length;
            final List<CronField> results = new ArrayList<>(size + 1);
            for (int j = 0; j < size; j++) {
                results.add(fields.get(j).parse(expressionParts[j]));
            }
            return new SingleCron(cronDefinition, results).validate();
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Failed to parse cron expression. %s", e.getMessage()), e);
        }
    }
}

