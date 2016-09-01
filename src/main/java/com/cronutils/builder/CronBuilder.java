/*
 * Copyright 2015 jmrozanec
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
package com.cronutils.builder;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.visitor.ValidationFieldExpressionVisitor;
import com.cronutils.utils.VisibleForTesting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.cronutils.model.field.CronFieldName.*;
import static com.cronutils.utils.Preconditions.checkState;

public class CronBuilder {

	private final Map<CronFieldName, CronField> fields = new HashMap<>();
	private CronDefinition definition;

	private CronBuilder(CronDefinition definition) {
		this.definition = definition;
	}

	public static CronBuilder cron(CronDefinition definition) {
		return new CronBuilder(definition);
	}

	public CronBuilder withYear(FieldExpression expression) {
		return addField(YEAR, expression);
	}

	public CronBuilder withDoM(FieldExpression expression) {
		return addField(DAY_OF_MONTH, expression);
	}

	public CronBuilder withMonth(FieldExpression expression) {
		return addField(MONTH, expression);
	}

	public CronBuilder withDoW(FieldExpression expression) {
		return addField(DAY_OF_WEEK, expression);
	}

	public CronBuilder withHour(FieldExpression expression) {
		return addField(HOUR, expression);
	}

	public CronBuilder withMinute(FieldExpression expression) {
		return addField(MINUTE, expression);
	}

	public CronBuilder withSecond(FieldExpression expression) {
		return addField(SECOND, expression);
	}

	public Cron instance() {
		return new Cron(definition, new ArrayList<>(fields.values())).validate();
	}

	@VisibleForTesting
	CronBuilder addField(CronFieldName name, FieldExpression expression) {
		checkState(definition != null, "CronBuilder not initialized.");

		FieldConstraints constraints = definition.getFieldDefinition(name).getConstraints();
		expression.accept(new ValidationFieldExpressionVisitor(constraints, definition.isStrictRanges()));
		fields.put(name, new CronField(name, expression, constraints));

		return this;
	}
}
