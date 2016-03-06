package com.cronutils.parser.field;

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.*;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.regex.Pattern;

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
/**
 * Parses a field from a cron expression.
 */
public class FieldParser {
    private final char[] specialCharsMinusStar = new char[]{'/', '-', ','};//universally supported
    private Pattern lPattern = Pattern.compile("[0-9]L", Pattern.CASE_INSENSITIVE);
    private Pattern wPattern = Pattern.compile("[0-9]W", Pattern.CASE_INSENSITIVE);
    private FieldConstraints constraints;

    /**
     * Constructor.
     * @param constraints - FieldConstraints for field.
     */
    public FieldParser(FieldConstraints constraints) {
        this.constraints = Validate.notNull(constraints, "FieldConstraints cannot be null");
    }

    /**
     * Parse given expression for a single cron field
     * @param expression - String
     * @return CronFieldExpression object that with interpretation of given String parameter
     */
    public FieldExpression parse(String expression) {
        if (!StringUtils.containsAny(expression, specialCharsMinusStar)) {
            if ("*".equals(expression)) {//all crons support asterisk
                return new Always(constraints);
            } else {
                if("?".equals(expression)){
                    if(constraints.isSpecialCharAllowed(SpecialChar.QUESTION_MARK)){
                        return new QuestionMark(constraints);
                    } else {
                        throw new IllegalArgumentException("Invalid expression: " + expression);
                    }
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
                        String value = values[1];
                        constraints.validateAllCharsValid(value);
                        return new Every(constraints, new IntegerFieldValue(Integer.parseInt(value)));
                    }else if(values.length == 1){
                        throw new IllegalArgumentException("Missing steps for expression: " + expression);
                    }else {
                        throw new IllegalArgumentException("Invalid expression: " + expression);
                    }
                }
            }
        }
    }

    private Between parseBetween(String[]array){
        if (array[1].contains("/")) {
            String[] every = array[1].split("/");

            return
                    new Between(
                            constraints,
                            map(constraints, array[0]),
                            map(constraints, every[0]),
                            mapToIntegerFieldValue(every[1])
                    );
        } else {
            String from = array[0];
            String to = array[1];
            return
                    new Between(
                            constraints,
                            map(constraints, from),
                            map(constraints, to)
                    );
        }
    }

    private On parseOn(String exp){
        constraints.validateAllCharsValid(exp);
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
                constraints,
                mapToIntegerFieldValue(exp),
                new SpecialCharFieldValue(SpecialChar.NONE),
                new IntegerFieldValue(-1)
        );
    }

    private On parseOnWithHash(String exp){
        SpecialCharFieldValue specialChar = new SpecialCharFieldValue(SpecialChar.HASH);
        String[] array = exp.split("#");
        IntegerFieldValue nth = mapToIntegerFieldValue(array[1]);
        if (array[0].isEmpty()) {
            throw new IllegalArgumentException("Time should be specified!");
        }
        return new On(constraints, mapToIntegerFieldValue(array[0]), specialChar, nth);
    }

    private On parseOnWithQuestionMark(String exp){
        SpecialCharFieldValue specialChar = new SpecialCharFieldValue(SpecialChar.QUESTION_MARK);
        exp = exp.replace("?", "");
        if("".equals(exp)){
            return new On(constraints, new IntegerFieldValue(-1), specialChar, new IntegerFieldValue(-1));
        }else{
            throw new IllegalArgumentException(String.format("Expected: '?', found: %s", exp));
        }
    }

    private On parseOnWithLW(String exp){
        SpecialCharFieldValue specialChar = new SpecialCharFieldValue(SpecialChar.LW);
        exp = exp.replace("LW", "");
        if("".equals(exp)){
            return new On(constraints, new IntegerFieldValue(-1), specialChar, new IntegerFieldValue(-1));
        }else{
            throw new IllegalArgumentException(String.format("Expected: LW, found: %s", exp));
        }
    }

    private On parseOnWithL(String exp){
        SpecialCharFieldValue specialChar = new SpecialCharFieldValue(SpecialChar.L);
        exp = exp.replace("L", "");
        IntegerFieldValue time = new IntegerFieldValue(-1);
        if(!"".equals(exp)){
            time = mapToIntegerFieldValue(exp);
        }
        return new On(constraints, time, specialChar, new IntegerFieldValue(-1));
    }

    private On parseOnWithW(String exp){
        return new On(
                constraints,
                mapToIntegerFieldValue(exp.replace("W", "")),
                new SpecialCharFieldValue(SpecialChar.W),
                new IntegerFieldValue(-1)
        );
    }

    private IntegerFieldValue mapToIntegerFieldValue(String string){
        constraints.validateAllCharsValid(string);
        try{
            return new IntegerFieldValue(constraints.stringToInt(string));
        }catch (NumberFormatException e){
            throw new IllegalArgumentException(String.format("Invalid value. Expected some integer, found %s", string));
        }
    }

    private FieldValue map(FieldConstraints constraints, String string){
        constraints.validateAllCharsValid(string);
        for(SpecialChar sc : SpecialChar.values()){
            if(sc.toString().equals(string)){
                return new SpecialCharFieldValue(sc);
            }
        }
        return new IntegerFieldValue(constraints.stringToInt(string));
    }
}
