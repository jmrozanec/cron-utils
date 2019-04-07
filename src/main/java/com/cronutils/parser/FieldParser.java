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

import java.util.regex.Pattern;

import com.cronutils.StringValidations;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import com.cronutils.utils.Preconditions;
import com.cronutils.utils.StringUtils;
import com.cronutils.utils.VisibleForTesting;

import static com.cronutils.model.field.expression.FieldExpression.always;
import static com.cronutils.model.field.expression.FieldExpression.questionMark;
import static com.cronutils.model.field.value.SpecialChar.HASH;
import static com.cronutils.model.field.value.SpecialChar.L;
import static com.cronutils.model.field.value.SpecialChar.LW;
import static com.cronutils.model.field.value.SpecialChar.NONE;
import static com.cronutils.model.field.value.SpecialChar.QUESTION_MARK;
import static com.cronutils.model.field.value.SpecialChar.W;

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
    private static final String ASTERISK_ALWAYS_VALUE = "1";

    private final FieldConstraints fieldConstraints;

    public FieldParser(final FieldConstraints constraints) {
        fieldConstraints = Preconditions.checkNotNull(constraints, "FieldConstraints must not be null");
    }

    /**
     * Parse given expression for a single cron field.
     *
     * @param expression - String
     * @return CronFieldExpression object that with interpretation of given String parameter
     */
    public FieldExpression parse(final String expression) {
        if (!StringUtils.containsAny(expression, SPECIAL_CHARS_MINUS_STAR)) {
            if (expression.contains(QUESTION_MARK_STRING) && !fieldConstraints.getSpecialChars().contains(QUESTION_MARK)) {
                throw new IllegalArgumentException("Invalid expression: " + expression);
            }

            return noSpecialCharsNorStar(expression);
        } else {
            final String[] array = expression.split(",");
            if (array.length > 1) {
                return commaSplitResult(array);
            } else {
                final String[] splitted = expression.split("-");
                if (expression.contains("-") && splitted.length != 2) {
                    throw new IllegalArgumentException("Missing values for range: " + expression);
                }
                return splitted[0].equalsIgnoreCase(L_STRING)
                        ? parseOnWithL(splitted[0], mapToIntegerFieldValue(splitted[1]))
                        : dashSplitResult(expression, splitted);
            }
        }
    }

    private FieldExpression dashSplitResult(final String expression, final String[] betweenArray) {
        if (betweenArray.length > 1) {
            return parseBetween(betweenArray);
        } else {
            return slashSplit(expression, expression.split(SLASH));
        }
    }

    private FieldExpression commaSplitResult(final String[] array) {
        final And and = new And();
        for (final String exp : array) {
            and.and(parse(exp));
        }
        return and;
    }

    private FieldExpression slashSplit(final String expression, final String[] values) {
        if (values.length == 2) {
            final String start = values[0];
            final String value = values[1];
            return asteriskOrempty(start, value);
        } else if (values.length == 1) {
            throw new IllegalArgumentException("Missing steps for expression: " + expression);
        } else {
            throw new IllegalArgumentException("Invalid expression: " + expression);
        }
    }

    private FieldExpression asteriskOrempty(final String start, final String value) {
        final String trimmedStart = start.trim();
        if (ASTERISK.equals(trimmedStart) && value.equals(ASTERISK_ALWAYS_VALUE)) {
            return noSpecialCharsNorStar(start);
        }
        if (ASTERISK.equals(trimmedStart) || EMPTY_STRING.equals(start.trim())) {
            return new Every(new IntegerFieldValue(Integer.parseInt(value)));
        } else {
            return new Every(new On(new IntegerFieldValue(Integer.parseInt(start))), new IntegerFieldValue(Integer.parseInt(value)));
        }
    }

    private FieldExpression noSpecialCharsNorStar(final String expression) {
        if (ASTERISK.equals(expression)) { // all crons support asterisk
            return always();
        } else {
            if (QUESTION_MARK_STRING.equals(expression)) {
                return questionMark();
            }
            return parseOn(expression);
        }
    }

    @VisibleForTesting
    protected FieldExpression parseBetween(final String[] array) {
        if (array[0].isEmpty() || array[1].isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Invalid expression! Expression: %s-%s does not describe a range. Negative numbers are not allowed.", array[0], array[1]));
        }
        if (array[1].contains(SLASH)) {
            final String[] every = array[1].split(SLASH);
            return new Every(new Between(map(array[0]), map(every[0])), mapToIntegerFieldValue(every[1]));
        } else {
            return new Between(map(array[0]), map(array[1]));
        }
    }

    @VisibleForTesting
    protected On parseOn(final String exp) {
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
    protected On parseOnWithHash(final String exp) {
        if (!fieldConstraints.getSpecialChars().contains(HASH)) {
            throw new IllegalArgumentException("Invalid expression: " + exp);
        }
        final SpecialCharFieldValue specialChar = new SpecialCharFieldValue(HASH);
        final String[] array = exp.split(HASH_TAG);
        final IntegerFieldValue nth = mapToIntegerFieldValue(array[1]);
        if (array[0].isEmpty()) {
            throw new IllegalArgumentException("Time should be specified!");
        }
        return new On(mapToIntegerFieldValue(array[0]), specialChar, nth);
    }

    @VisibleForTesting
    protected On parseOnWithQuestionMark(final String exp) {
        final SpecialCharFieldValue specialChar = new SpecialCharFieldValue(QUESTION_MARK);
        final String questionMarkExpression = exp.replace(QUESTION_MARK_STRING, EMPTY_STRING);
        if (EMPTY_STRING.equals(questionMarkExpression)) {
            return new On(new IntegerFieldValue(-1), specialChar, new IntegerFieldValue(-1));
        } else {
            throw new IllegalArgumentException(String.format("Expected: '?', found: %s", questionMarkExpression));
        }
    }

    @VisibleForTesting
    protected On parseOnWithLW(final String exp) {
        final SpecialCharFieldValue specialChar = new SpecialCharFieldValue(LW);
        final String lwExpression = exp.replace(LW_STRING, EMPTY_STRING);
        if (EMPTY_STRING.equals(lwExpression)) {
            return new On(new IntegerFieldValue(-1), specialChar, new IntegerFieldValue(-1));
        } else {
            throw new IllegalArgumentException(String.format("Expected: LW, found: %s", lwExpression));
        }
    }

    @VisibleForTesting
    protected On parseOnWithL(final String exp) {
        return parseOnWithL(exp, new IntegerFieldValue(-1));
    }

    protected On parseOnWithL(final String exp, final IntegerFieldValue daysBefore) {
        final SpecialCharFieldValue specialChar = new SpecialCharFieldValue(L);
        final String expression = exp.replace(L_STRING, EMPTY_STRING);
        IntegerFieldValue time = new IntegerFieldValue(-1);
        if (!EMPTY_STRING.equals(expression)) {
            time = mapToIntegerFieldValue(expression);
        }
        return new On(time, specialChar, daysBefore);
    }

    @VisibleForTesting
    protected On parseOnWithW(final String exp) {
        return new On(mapToIntegerFieldValue(exp.replace(W_STRING, EMPTY_STRING)), new SpecialCharFieldValue(W), new IntegerFieldValue(-1));
    }

    @VisibleForTesting
    protected IntegerFieldValue mapToIntegerFieldValue(final String string) {
        try {
            return new IntegerFieldValue(intToInt(stringToInt(string)));
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Invalid value. Expected some integer, found %s", string));
        }
    }

    @VisibleForTesting
    protected FieldValue<?> map(final String string) {
        for (final SpecialChar sc : SpecialChar.values()) {
            if (sc.toString().equals(string)) {
                return new SpecialCharFieldValue(sc);
            }
        }
        return new IntegerFieldValue(stringToInt(string));
    }

    /**
     * Maps string expression to integer. If no mapping is found, will try to parse String as Integer
     *
     * @param exp - expression to be mapped
     * @return integer value for string expression
     */
    @VisibleForTesting
    protected int stringToInt(final String exp) {
        final Integer value = fieldConstraints.getStringMappingValue(exp);
        if (value != null) {
            return value;
        } else {
            try {
                return Integer.parseInt(exp);
            } catch (final NumberFormatException e) {
                final String invalidChars = new StringValidations(fieldConstraints).removeValidChars(exp);
                throw new IllegalArgumentException(String.format("Invalid chars in expression! Expression: %s Invalid chars: %s", exp, invalidChars));
            }
        }
    }

    /**
     * Maps integer values to another integer equivalence. Always consider mapping higher integers to lower once. Ex.: if 0 and 7 mean the
     * same, map 7 to 0.
     *
     * @param exp - integer to be mapped
     * @return Mapping integer. If no mapping int is found, will return exp
     */
    @VisibleForTesting
    protected int intToInt(final Integer exp) {
        final Integer value = fieldConstraints.getIntMappingValue(exp);
        if (value != null) {
            return value;
        }
        return exp;
    }
}
