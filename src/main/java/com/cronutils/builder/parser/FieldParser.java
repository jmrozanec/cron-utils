package com.cronutils.builder.parser;

import com.cronutils.builder.model.expression.*;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
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
    private Map<String, Integer> stringToInt;
    /**
     * Constructor.
     */
    public FieldParser() {
        stringToInt = Maps.newHashMap();
        stringToInt.putAll(daysOfWeekMapping());
        stringToInt.putAll(monthsMapping());
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
                        String value = values[1];
                        return new Every(new IntegerFieldValue(Integer.parseInt(value)));
                    }else if(values.length == 1){
                        throw new IllegalArgumentException("Missing steps for expression: " + expression);
                    }else {
                        throw new IllegalArgumentException("Invalid expression: " + expression);
                    }
                }
            }
        }
    }

    //TODO issue #81: https://github.com/jmrozanec/cron-utils/issues/81
    @VisibleForTesting
    Between parseBetween(String[]array){
        if (array[1].contains("/")) {
            String[] every = array[1].split("/");

            return
                    new Between(
                            map(array[0]),
                            map(every[0]),
                            mapToIntegerFieldValue(every[1])
                    );
        } else {
            String from = array[0];
            String to = array[1];
            return
                    new Between(
                            map(from),
                            map(to)
                    );
        }
    }

    @VisibleForTesting
    On parseOn(String exp){
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
            return new IntegerFieldValue(stringToInt.get(string));
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
        return new IntegerFieldValue(stringToInt.get(string));
    }

    /**
     * Creates days of week mapping
     * @return Map<String, Integer> where strings are weekday names in EEE format,
     * and integers correspond to their 1-7 mappings
     */
    @VisibleForTesting
    static Map<String, Integer> daysOfWeekMapping() {
        Map<String, Integer> stringMapping = Maps.newHashMap();
        stringMapping.put("MON", 1);
        stringMapping.put("TUE", 2);
        stringMapping.put("WED", 3);
        stringMapping.put("THU", 4);
        stringMapping.put("FRI", 5);
        stringMapping.put("SAT", 6);
        stringMapping.put("SUN", 7);
        return stringMapping;
    }

    /**
     * Creates months mapping
     * @return Map<String, Integer> where strings month names in EEE format,
     * and integers correspond to their 1-12 mappings
     */
    @VisibleForTesting
    static Map<String, Integer> monthsMapping() {
        Map<String, Integer> stringMapping = Maps.newHashMap();
        stringMapping.put("JAN", 1);
        stringMapping.put("FEB", 2);
        stringMapping.put("MAR", 3);
        stringMapping.put("APR", 4);
        stringMapping.put("MAY", 5);
        stringMapping.put("JUN", 6);
        stringMapping.put("JUL", 7);
        stringMapping.put("AUG", 8);
        stringMapping.put("SEP", 9);
        stringMapping.put("OCT", 10);
        stringMapping.put("NOV", 11);
        stringMapping.put("DEC", 12);
        return stringMapping;
    }
}