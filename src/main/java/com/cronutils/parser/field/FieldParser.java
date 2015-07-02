package com.cronutils.parser.field;

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.*;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

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
                constraints.validateAllCharsValid(expression);
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
                    String value = expression.split("/")[1];
                    constraints.validateAllCharsValid(value);
                    return new Every(constraints, new IntegerFieldValue(Integer.parseInt(value)));
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
            constraints.validateAllCharsValid(from);
            constraints.validateAllCharsValid(to);
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
        SpecialCharFieldValue specialChar = new SpecialCharFieldValue(SpecialChar.NONE);
        IntegerFieldValue nth = new IntegerFieldValue(-1);
        IntegerFieldValue time = new IntegerFieldValue(-1);
        String expression = exp;
        if (exp.contains("#")) {
            specialChar = new SpecialCharFieldValue(SpecialChar.HASH);
            String[] array = exp.split("#");
            nth = mapToIntegerFieldValue(array[1]);
            if (array[0].isEmpty()) {
                throw new IllegalArgumentException("Time should be specified!");
            }
            expression = array[0];
        }
        if (exp.contains("LW")) {
            specialChar = new SpecialCharFieldValue(SpecialChar.LW);
            exp = exp.replace("LW", "");
            if ("".equals(exp)) {
                expression = null;
            } else {
                expression = exp;
            }
        }
        if (exp.contains("L")) {
            specialChar = new SpecialCharFieldValue(SpecialChar.L);
            exp = exp.replace("L", "");
            if ("".equals(exp)) {
                expression = null;
            } else {
                expression = exp;
            }
        }
        if (exp.contains("W")) {
            specialChar = new SpecialCharFieldValue(SpecialChar.W);
            expression = exp.replace("W", "");
        }
        constraints.validateSpecialCharAllowed(specialChar.getValue());
        if(expression!=null){
            return new On(constraints, mapToIntegerFieldValue(expression), specialChar, nth);
        }else{
            return new On(constraints, time, specialChar, nth);
        }
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
