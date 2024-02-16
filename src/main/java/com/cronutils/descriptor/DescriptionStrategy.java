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

package com.cronutils.descriptor;

import com.cronutils.model.field.expression.*;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.utils.Preconditions;
import com.cronutils.utils.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * Description strategy to handle cases on how to present cron information in a
 * human readable format.
 */
abstract class DescriptionStrategy {
	private static final String EVERY = "every";
	private static final String WHITE_SPACE = " ";
	protected Function<Integer, String> nominalValueFunction;
	protected ResourceBundle bundle;

	public DescriptionStrategy(final ResourceBundle bundle) {
		this.bundle = bundle;
		nominalValueFunction = integer -> WHITE_SPACE + integer;
	}

	/**
	 * Provide a human readable description.
	 *
	 * @return human readable description - String
	 */
	public abstract String describe();

	/**
	 * Given a {@linkplain FieldExpression}, provide a {@linkplain String} with a
	 * human readable description. Will identify {@linkplain FieldExpression}
	 * subclasses and delegate.
	 *
	 * @param fieldExpression - CronFieldExpression instance - not null
	 * @return human readable description - String
	 */
	protected String describe(final FieldExpression fieldExpression) {
		return describe(fieldExpression, false);
	}

	/**
	 * Given a {@linkplain FieldExpression}, provide a {@linkplain String} with a
	 * human readable description. Will identify {@linkplain FieldExpression}
	 * subclasses and delegate.
	 *
	 * @param fieldExpression - CronFieldExpression instance - not null
	 * @param and             - boolean expression that indicates if description
	 *                        should fit an "and" context
	 * @return human readable description - String
	 */
	protected String describe(final FieldExpression fieldExpression, final boolean and) {
		Preconditions.checkNotNull(fieldExpression, "CronFieldExpression should not be null!");
		if (fieldExpression instanceof Always) {
			return describe((Always) fieldExpression, and);
		}
		if (fieldExpression instanceof And) {
			return describe((And) fieldExpression);
		}
		if (fieldExpression instanceof Between) {
			return describe((Between) fieldExpression, and);
		}
		if (fieldExpression instanceof Every) {
			return describe((Every) fieldExpression, and);
		}
		if (fieldExpression instanceof On) {
			return describe((On) fieldExpression, and);
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Provide a human readable description for Always instance.
	 *
	 * @param always - Always
	 * @return human readable description - String
	 */
	protected String describe(final Always always, final boolean and) {
		return StringUtils.EMPTY;
	}

	/**
	 * Provide a human readable description for And instance.
	 *
	 * @param and - And
	 * @return human readable description - String
	 */
	protected String describe(final And and) {
		final List<FieldExpression> expressions = new ArrayList<>();
		final List<FieldExpression> onExpressions = new ArrayList<>();
		for (final FieldExpression fieldExpression : and.getExpressions()) {
			if (fieldExpression instanceof On) {
				onExpressions.add(fieldExpression);
			} else {
				expressions.add(fieldExpression);
			}
		}
		final StringBuilder builder = new StringBuilder();
		if (!onExpressions.isEmpty()) {
			if(onExpressions.size()==1){
				builder.append(bundle.getString("at"));
				builder.append(" %s ");
				createAndDescription(builder, onExpressions);
			}else{
				builder.append(bundle.getString("at"));
				createAndDescription(builder, onExpressions).append(" replace_plural ");
			}
		}

		if (!expressions.isEmpty()) {
			if(!onExpressions.isEmpty()){
				builder.append(String.format(" %s ", bundle.getString("and")));
			}
			createAndDescription(builder, expressions);
		}

		return builder.toString();
	}

	/**
	 * Provide a human readable description for Between instance.
	 *
	 * @param between - Between
	 * @return human readable description - String
	 */
	protected String describe(final Between between, final boolean and) {
		return bundle.getString(EVERY) + " %s " + MessageFormat.format(bundle.getString("between_x_and_y"),
				nominalValue(between.getFrom()), nominalValue(between.getTo())) + WHITE_SPACE;
	}

	/**
	 * Provide a human readable description for Every instance.
	 *
	 * @param every - Every
	 * @return human readable description - String
	 */
	protected String describe(final Every every, final boolean and) {
		String description;
		if (every.getPeriod().getValue() > 1) {
			description = String.format("%s %s ", bundle.getString(EVERY), nominalValue(every.getPeriod()))
					+ " replace_plural ";
		} else {
			description = bundle.getString(EVERY) + " %s ";
		}
		if (every.getExpression() instanceof Between) {
			final Between between = (Between) every.getExpression();
			description += MessageFormat.format(bundle.getString("between_x_and_y"), nominalValue(between.getFrom()),
					nominalValue(between.getTo())) + WHITE_SPACE;
		}
		return description;
	}

	/**
	 * Provide a human readable description for On instance.
	 *
	 * @param on - On
	 * @return human readable description - String
	 */
	protected String describe(final On on, final boolean and) {
		if (and) {
			return nominalValue(on.getTime());
		}
		return String.format("%s %s ", bundle.getString("at"), nominalValue(on.getTime())) + "%s";
	}

	/**
	 * Given an int, will return a nominal value. Example: 1 in weeks context, may
	 * mean "Monday", so nominal value for 1 would be "Monday" Default will return
	 * int as String
	 *
	 * @param fieldValue - some FieldValue
	 * @return String
	 */
	protected String nominalValue(final FieldValue<?> fieldValue) {
		Preconditions.checkNotNull(fieldValue, "FieldValue must not be null");
		if (fieldValue instanceof IntegerFieldValue) {
			return nominalValueFunction.apply(((IntegerFieldValue) fieldValue).getValue());
		}
		return fieldValue.toString();
	}

	/**
	 * Creates human readable description for And element.
	 *
	 * @param builder     - StringBuilder instance to which description will be
	 *                    appended
	 * @param expressions - field expressions
	 * @return same StringBuilder instance as parameter
	 */
	private StringBuilder createAndDescription(final StringBuilder builder, final List<FieldExpression> expressions) {
		if(expressions.size()>1){
			for (int j = 0; j < expressions.size() - 2; j++) {
				builder.append(String.format(" %s, ", describe(expressions.get(j), true)));
			}
			builder.append(String.format(" %s ", describe(expressions.get(expressions.size() - 2), true)));
			builder.append(String.format(" %s ", bundle.getString("and")));
		}

		builder.append(String.format(" %s ", describe(expressions.get(expressions.size() - 1), true)));
		return builder;
	}
}
