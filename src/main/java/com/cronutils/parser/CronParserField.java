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
package com.cronutils.parser;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.utils.Preconditions;

import java.util.Comparator;

/**
 * Represents a cron field.
 */
public class CronParserField {

	private final CronFieldName field;
	private final FieldConstraints constraints;
	private final FieldParser parser;

	/**
	 * Constructor
	 * 
	 * @param fieldName
	 *            - CronFieldName instance
	 */
	public CronParserField(CronFieldName fieldName, FieldConstraints constraints) {
		this.field = Preconditions.checkNotNull(fieldName, "CronFieldName must not be null");
		this.constraints = Preconditions.checkNotNull(constraints, "FieldConstraints must not be null");
		this.parser = new FieldParser(constraints);
	}

	/**
	 * Returns field name
	 * 
	 * @return CronFieldName, never null
	 */
	public CronFieldName getField() {
		return field;
	}

	/**
	 * Parses a String cron expression
	 * 
	 * @param expression
	 *            - cron expression
	 * @return parse result as CronFieldParseResult instance - never null. May throw a RuntimeException if cron expression is bad.
	 */
	public CronField parse(String expression) {
		return new CronField(field, parser.parse(expression), constraints);
	}

	/**
	 * Create a Comparator that compares CronField instances using CronFieldName value.
	 * 
	 * @return Comparator for CronField instance, never null.
	 */
	public static Comparator<CronParserField> createFieldTypeComparator() {
		return (o1, o2) -> o1.getField().getOrder() - o2.getField().getOrder();
	}
}
