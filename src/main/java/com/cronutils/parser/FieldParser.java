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

import com.cronutils.StringValidations;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.*;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import com.cronutils.utils.Preconditions;
import com.cronutils.utils.StringUtils;
import com.cronutils.utils.VisibleForTesting;

import java.util.regex.Pattern;

import static com.cronutils.model.field.value.SpecialChar.*;

/**
 * Parses a field from a cron expression.
 */
public class FieldParser {
	private static final String SLASH = "/";
	private static final String W_STRING = "W";
	private static final String EMPTY_STRING = "";
	private static final String LW_STRING = "LW";
	private static final String HASH_TAG = "#";
	private static final String L_STRING = "L";
	private static final String QUESTION_MARK_STRING = "?";
	private static final String ASTERISK = "*";
	private static final char[] SPECIAL_CHARS_MINUS_STAR = new char[] { '/', '-', ',' };// universally supported

	private static final Pattern L_PATTERN = Pattern.compile("[0-9]L", Pattern.CASE_INSENSITIVE);
	private static final Pattern W_PATTERN = Pattern.compile("[0-9]W", Pattern.CASE_INSENSITIVE);

	private FieldConstraints fieldConstraints;

	public FieldParser(FieldConstraints constraints) {
		this.fieldConstraints = Preconditions.checkNotNull(constraints, "FieldConstraints must not be null");
	}

	/**
	 * Parse given expression for a single cron field
	 * 
	 * @param expression
	 *            - String
	 * @return CronFieldExpression object that with interpretation of given String parameter
	 */
	public FieldExpression parse(String expression) {
		if (!StringUtils.containsAny(expression, SPECIAL_CHARS_MINUS_STAR)) {
			if(expression.contains(QUESTION_MARK_STRING) && !fieldConstraints.getSpecialChars().contains(QUESTION_MARK)){
				throw new IllegalArgumentException("Invalid expression: " + expression);
			}
			return noSpecialCharsNorStar(expression);
		} else {
			String[] array = expression.split(",");
			if (array.length > 1) {
				return commaSplitResult(array);
			} else {
				String[] betWeenArray = expression.split("-");
				return dashSplitResult(expression, betWeenArray);
			}
		}
	}

	private FieldExpression dashSplitResult(String expression, String[] betWeenArray) {
		if (betWeenArray.length > 1) {
			return parseBetween(betWeenArray);
		} else {
			return slashSplit(expression, expression.split(SLASH));
		}
	}

	private FieldExpression commaSplitResult(String[] array) {
		And and = new And();
		for (String exp : array) {
			and.and(parse(exp));
		}
		return and;
	}

	private FieldExpression slashSplit(String expression, String[] values) {
		if (values.length == 2) {
			String start = values[0];
			String value = values[1];
			return asteriskOrempty(start, value);
		} else if (values.length == 1) {
			throw new IllegalArgumentException("Missing steps for expression: " + expression);
		} else {
			throw new IllegalArgumentException("Invalid expression: " + expression);
		}
	}

	private FieldExpression asteriskOrempty(String start, String value) {
		String trimmedStart = start.trim();
		if (ASTERISK.equals(trimmedStart) || EMPTY_STRING.equals(start.trim())) {
			return new Every(new IntegerFieldValue(Integer.parseInt(value)));
		} else {
			return new Every(new On(new IntegerFieldValue(Integer.parseInt(start))), new IntegerFieldValue(Integer.parseInt(value)));
		}
	}

	private FieldExpression noSpecialCharsNorStar(String expression) {
		if (ASTERISK.equals(expression)) {// all crons support asterisk
			return new Always();
		} else {
			if (QUESTION_MARK_STRING.equals(expression)) {
				return new QuestionMark();
			}
			return parseOn(expression);
		}
	}

	@VisibleForTesting
	protected FieldExpression parseBetween(String[] array) {
		if(array[0].isEmpty() || array[1].isEmpty()){
			throw new IllegalArgumentException(String.format("Invalid expression! Expression: %s-%s does not describe a range. Negative numbers are not allowed.", array[0], array[1]));
		}
		if (array[1].contains(SLASH)) {
			String[] every = array[1].split(SLASH);
			return new Every(new Between(map(array[0]), map(every[0])), mapToIntegerFieldValue(every[1]));
		} else {
			return new Between(map(array[0]), map(array[1]));
		}
	}

	@VisibleForTesting
	protected On parseOn(String exp) {
		if (QUESTION_MARK_STRING.equals(exp)) {
			return parseOnWithQuestionMark(exp);
		} else if (exp.contains(HASH_TAG)) {
			return parseOnWithHash(exp);
		} else if (exp.contains(LW_STRING)) {
			return parseOnWithLW(exp);
		} else if (L_PATTERN.matcher(exp).find() || exp.equalsIgnoreCase(L_STRING)) {
			return parseOnWithL(exp);
		} else if (W_PATTERN.matcher(exp).find()) {
			return parseOnWithW(exp);
		} else {
			return new On(mapToIntegerFieldValue(exp), new SpecialCharFieldValue(NONE), new IntegerFieldValue(-1));
		}
	}

	@VisibleForTesting
	protected On parseOnWithHash(String exp) {
		SpecialCharFieldValue specialChar = new SpecialCharFieldValue(HASH);
		String[] array = exp.split(HASH_TAG);
		IntegerFieldValue nth = mapToIntegerFieldValue(array[1]);
		if (array[0].isEmpty()) {
			throw new IllegalArgumentException("Time should be specified!");
		}
		return new On(mapToIntegerFieldValue(array[0]), specialChar, nth);
	}

	@VisibleForTesting
	protected On parseOnWithQuestionMark(String exp) {
		SpecialCharFieldValue specialChar = new SpecialCharFieldValue(QUESTION_MARK);
		String questionMarkExpression = exp.replace(QUESTION_MARK_STRING, EMPTY_STRING);
		if (EMPTY_STRING.equals(questionMarkExpression)) {
			return new On(new IntegerFieldValue(-1), specialChar, new IntegerFieldValue(-1));
		} else {
			throw new IllegalArgumentException(String.format("Expected: '?', found: %s", questionMarkExpression));
		}
	}

	@VisibleForTesting
	protected On parseOnWithLW(String exp) {
		SpecialCharFieldValue specialChar = new SpecialCharFieldValue(LW);
		String lwExpression = exp.replace(LW_STRING, EMPTY_STRING);
		if (EMPTY_STRING.equals(lwExpression)) {
			return new On(new IntegerFieldValue(-1), specialChar, new IntegerFieldValue(-1));
		} else {
			throw new IllegalArgumentException(String.format("Expected: LW, found: %s", lwExpression));
		}
	}

	@VisibleForTesting
	protected On parseOnWithL(String exp) {
		SpecialCharFieldValue specialChar = new SpecialCharFieldValue(L);
		String lExpression = exp.replace(L_STRING, EMPTY_STRING);
		IntegerFieldValue time = new IntegerFieldValue(-1);
		if (!EMPTY_STRING.equals(lExpression)) {
			time = mapToIntegerFieldValue(lExpression);
		}
		return new On(time, specialChar, new IntegerFieldValue(-1));
	}

	@VisibleForTesting
	protected On parseOnWithW(String exp) {
		return new On(mapToIntegerFieldValue(exp.replace(W_STRING, EMPTY_STRING)), new SpecialCharFieldValue(W), new IntegerFieldValue(-1));
	}

	@VisibleForTesting
	protected IntegerFieldValue mapToIntegerFieldValue(String string) {
		try {
			return new IntegerFieldValue(intToInt(stringToInt(string)));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(String.format("Invalid value. Expected some integer, found %s", string));
		}
	}

	@VisibleForTesting
	protected FieldValue<?> map(String string) {
		for (SpecialChar sc : SpecialChar.values()) {
			if (sc.toString().equals(string)) {
				return new SpecialCharFieldValue(sc);
			}
		}
		return new IntegerFieldValue(stringToInt(string));
	}

	/**
	 * Maps string expression to integer. If no mapping is found, will try to parse String as Integer
	 * 
	 * @param exp
	 *            - expression to be mapped
	 * @return integer value for string expression
	 */
	@VisibleForTesting
	protected int stringToInt(String exp) {
		Integer value = fieldConstraints.getStringMappingValue(exp);
		if (value != null) {
			return value;
		} else {
			try {
				return Integer.parseInt(exp);
			} catch (NumberFormatException e) {
				String invalidChars = new StringValidations(fieldConstraints).removeValidChars(exp);
				throw new IllegalArgumentException(String.format("Invalid chars in expression! Expression: %s Invalid chars: %s", exp, invalidChars));
			}
		}
	}

	/**
	 * Maps integer values to another integer equivalence. Always consider mapping higher integers to lower once. Ex.: if 0 and 7 mean the
	 * same, map 7 to 0.
	 * 
	 * @param exp
	 *            - integer to be mapped
	 * @return Mapping integer. If no mapping int is found, will return exp
	 */
	@VisibleForTesting
	protected int intToInt(Integer exp) {
		Integer value = fieldConstraints.getIntMappingValue(exp);
		if (value != null) {
			return value;
		}
		return exp;
	}
}