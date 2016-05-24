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

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.*;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.regex.Pattern;

/**
 * Parses a field from a cron expression.
 */
public class FieldParser {
    private final char[] specialCharsMinusStar = new char[]{'/', '-', ','};//universally supported
    private Pattern lPattern = Pattern.compile("[0-9]L", Pattern.CASE_INSENSITIVE);
    private Pattern wPattern = Pattern.compile("[0-9]W", Pattern.CASE_INSENSITIVE);
    private FieldConstraints fieldConstraints;

    public FieldParser(FieldConstraints constraints) {
        this.fieldConstraints = Validate.notNull(constraints, "FieldConstraints must not be null");
    }

    /**
     * Parse given expression for a single cron field
     * @param expression - String
     * @return CronFieldExpression object that with interpretation of given String parameter
     */
    public FieldExpression parse(String expression) {
        if (!StringUtils.containsAny(expression, specialCharsMinusStar)) {
            if ("*".equals(expression)) {//all crons support asterisk
                return new Always();
            } else {
                if("?".equals(expression)){
                    return new QuestionMark();
                }
                return parseOn(expression);
            }
        } else {
            String[] array = expression.split(",");
            if (array.length > 1) {
                And and = new And();
                for (String exp : array) {
                    and.and(parse(exp));
                }
                return and;
            } else {
                array = expression.split("-");
                if (array.length > 1) {
                    return parseBetween(array);
                } else {
                    String[] values = expression.split("/");
                    if(values.length == 2) {
                        String start = values[0];
                        String value = values[1];
                        if("*".equals(start.trim()) || "".equals(start.trim())){
                            return new Every(new IntegerFieldValue(Integer.parseInt(value)));
                        }else{
                            return new Every(
                                    new On(new IntegerFieldValue(Integer.parseInt(start))),
                                    new IntegerFieldValue(Integer.parseInt(value))
                            );
                        }
                    }else if(values.length == 1){
                        throw new IllegalArgumentException("Missing steps for expression: " + expression);
                    }else {
                        throw new IllegalArgumentException("Invalid expression: " + expression);
                    }
                }
            }
        }
    }

    @VisibleForTesting
    FieldExpression parseBetween(String[]array){
        if (array[1].contains("/")) {
            String[] every = array[1].split("/");
            return new Every(new Between(map(array[0]), map(every[0])), mapToIntegerFieldValue(every[1]));
        } else {
            return new Between(map(array[0]), map(array[1]));
        }
    }

    @VisibleForTesting
    On parseOn(String exp){
        if("?".equals(exp)){
            return parseOnWithQuestionMark(exp);
        }
        if (exp.contains("#")) {
            return parseOnWithHash(exp);
        }
        if (exp.contains("LW")) {
            return parseOnWithLW(exp);
        }
        if (lPattern.matcher(exp).find()||exp.equalsIgnoreCase("L")) {
            return parseOnWithL(exp);
        }
        if (wPattern.matcher(exp).find()) {
            return parseOnWithW(exp);
        }
        return new On(
                mapToIntegerFieldValue(exp),
                new SpecialCharFieldValue(SpecialChar.NONE),
                new IntegerFieldValue(-1)
        );
    }

    @VisibleForTesting
    On parseOnWithHash(String exp){
        SpecialCharFieldValue specialChar = new SpecialCharFieldValue(SpecialChar.HASH);
        String[] array = exp.split("#");
        IntegerFieldValue nth = mapToIntegerFieldValue(array[1]);
        if (array[0].isEmpty()) {
            throw new IllegalArgumentException("Time should be specified!");
        }
        return new On(mapToIntegerFieldValue(array[0]), specialChar, nth);
    }

    @VisibleForTesting
    On parseOnWithQuestionMark(String exp){
        SpecialCharFieldValue specialChar = new SpecialCharFieldValue(SpecialChar.QUESTION_MARK);
        exp = exp.replace("?", "");
        if("".equals(exp)){
            return new On(new IntegerFieldValue(-1), specialChar, new IntegerFieldValue(-1));
        }else{
            throw new IllegalArgumentException(String.format("Expected: '?', found: %s", exp));
        }
    }

    @VisibleForTesting
    On parseOnWithLW(String exp){
        SpecialCharFieldValue specialChar = new SpecialCharFieldValue(SpecialChar.LW);
        exp = exp.replace("LW", "");
        if("".equals(exp)){
            return new On(new IntegerFieldValue(-1), specialChar, new IntegerFieldValue(-1));
        }else{
            throw new IllegalArgumentException(String.format("Expected: LW, found: %s", exp));
        }
    }

    @VisibleForTesting
    On parseOnWithL(String exp){
        SpecialCharFieldValue specialChar = new SpecialCharFieldValue(SpecialChar.L);
        exp = exp.replace("L", "");
        IntegerFieldValue time = new IntegerFieldValue(-1);
        if(!"".equals(exp)){
            time = mapToIntegerFieldValue(exp);
        }
        return new On(time, specialChar, new IntegerFieldValue(-1));
    }

    @VisibleForTesting
    On parseOnWithW(String exp){
        return new On(
                mapToIntegerFieldValue(exp.replace("W", "")),
                new SpecialCharFieldValue(SpecialChar.W),
                new IntegerFieldValue(-1)
        );
    }

    @VisibleForTesting
    IntegerFieldValue mapToIntegerFieldValue(String string){
        try{
            return new IntegerFieldValue(intToInt(stringToInt(string)));
        }catch (NumberFormatException e){
            throw new IllegalArgumentException(String.format("Invalid value. Expected some integer, found %s", string));
        }
    }

    @VisibleForTesting
    FieldValue map(String string){
        for(SpecialChar sc : SpecialChar.values()){
            if(sc.toString().equals(string)){
                return new SpecialCharFieldValue(sc);
            }
        }
        return new IntegerFieldValue(stringToInt(string));
    }

    /**
     * Maps string expression to integer.
     * If no mapping is found, will try to parse String as Integer
     * @param exp - expression to be mapped
     * @return integer value for string expression
     */
    int stringToInt(String exp) {
        if (fieldConstraints.getStringMapping().containsKey(exp)) {
            return fieldConstraints.getStringMapping().get(exp);
        } else {
            try{
                return Integer.parseInt(exp);
            }catch (NumberFormatException e){
                throw new IllegalArgumentException(e);
            }
        }
    }

    /**
     * Maps integer values to another integer equivalence.
     * Always consider mapping higher integers to lower once.
     * Ex.: if 0 and 7 mean the same, map 7 to 0.
     * @param exp - integer to be mapped
     * @return Mapping integer. If no mapping int is found, will return exp
     */
    int intToInt(Integer exp) {
        if (fieldConstraints.getIntMapping().containsKey(exp)) {
            return fieldConstraints.getIntMapping().get(exp);
        }
        return exp;
    }
}