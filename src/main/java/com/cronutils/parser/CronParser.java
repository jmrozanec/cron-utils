package com.cronutils.parser;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.utils.Preconditions;
import com.cronutils.utils.StringUtils;

import java.util.*;

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

/**
 * Parser for cron expressions
 */
public class CronParser {

	private final Map<Integer, List<CronParserField>> expressions = new HashMap<>();;
	private CronDefinition cronDefinition;

	/**
	 * @param cronDefinition
	 *            - cronDefinition of cron expressions to be parsed if null, a NullPointerException will be raised.
	 */
	public CronParser(CronDefinition cronDefinition) {
		this.cronDefinition = Preconditions.checkNotNull(cronDefinition, "CronDefinition must not be null");
		buildPossibleExpressions(cronDefinition);
	}

	/**
	 * Build possible cron expressions from definitions. One is built for sure. A second one may be build if last field is optional.
	 * 
	 * @param cronDefinition
	 *            - cron definition instance
	 */
	private void buildPossibleExpressions(CronDefinition cronDefinition) {
		List<CronParserField> expression = new ArrayList<>();
		for (FieldDefinition fieldDefinition : cronDefinition.getFieldDefinitions()) {
			expression.add(new CronParserField(fieldDefinition.getFieldName(), fieldDefinition.getConstraints()));
		}
		Collections.sort(expression, CronParserField.createFieldTypeComparator());
		expressions.put(expression.size(), expression);

		if (cronDefinition.isLastFieldOptional()) {
			List<CronParserField> shortExpression = new ArrayList<>();
			shortExpression.addAll(expression);
			shortExpression.remove(shortExpression.size() - 1);
			expressions.put(shortExpression.size(), shortExpression);
		}
	}

	/**
	 * Parse string with cron expression
	 * 
	 * @param expression
	 *            - cron expression, never null
	 * @return Cron instance, corresponding to cron expression received
	 * @throws java.lang.IllegalArgumentException
	 *             if expression does not match cron definition
	 */
	public Cron parse(String expression) {
		Preconditions.checkNotNull(expression, "Expression must not be null");
		String replaced = expression.replaceAll("\\s+", " ").trim();
		if (StringUtils.isEmpty(replaced)) {
			throw new IllegalArgumentException("Empty expression!");
		}
		String[] expressionParts = replaced.toUpperCase().split(" ");
		int expressionLength = expressionParts.length;
		List<CronParserField> fields = expressions.get(expressionLength);
		if (fields == null) {
			throw new IllegalArgumentException(
					String.format("Cron expression contains %s parts but we expect one of %s", expressionLength, expressions.keySet()));
		}
		try{
			int size = fields.size();
			List<CronField> results = new ArrayList<>(size + 1);
			for (int j = 0; j < size; j++) {
				results.add(fields.get(j).parse(expressionParts[j]));
			}
			return new Cron(cronDefinition, results).validate();
		}catch (IllegalArgumentException e){
			throw new IllegalArgumentException(String.format("Failed to parse '%s'. %s", expression, e.getMessage()), e);
		}
	}
}
