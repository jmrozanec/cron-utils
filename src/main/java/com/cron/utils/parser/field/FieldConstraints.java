package com.cron.utils.parser.field;

import com.cron.utils.CronParameter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
public class FieldConstraints {
    private Map<String, Integer> stringMapping;
    private Map<Integer, Integer> intMapping;
    private int startRange;
    private int endRange;
    private Set<SpecialChar> specialChars;

    public FieldConstraints() {
        stringMapping = new HashMap<String, Integer>();
        intMapping = new HashMap<Integer, Integer>();
        startRange = 0;//no negatives!
        endRange = Integer.MAX_VALUE;
        specialChars = Sets.newHashSet();
        specialChars.add(SpecialChar.NONE);
    }

    public FieldConstraints registerStringToIntMapping(Map<String, Integer> mapping) {
        this.stringMapping = mapping;
        return this;
    }

    public FieldConstraints registerIntToIntMapping(Map<Integer, Integer> mapping) {
        this.intMapping = mapping;
        return this;
    }

    public FieldConstraints setValidationRange(int start, int end) {
        this.startRange = start;
        this.endRange = end;
        return this;
    }

    public FieldConstraints supportHash(){
        specialChars.add(SpecialChar.HASH);
        return this;
    }

    public FieldConstraints supportL(){
        specialChars.add(SpecialChar.L);
        return this;
    }

    public FieldConstraints supportW(){
        specialChars.add(SpecialChar.W);
        return this;
    }

    public int stringToInt(String exp) {
        if (stringMapping.containsKey(exp)) {
            return stringMapping.get(exp);
        } else {
            return Integer.parseInt(exp);
        }
    }

    public int intToInt(Integer exp) {
        if (intMapping.containsKey(exp)) {
            return intMapping.get(exp);
        }
        return exp;
    }

    public int validateInRange(int number) {
        if (number >= startRange && number <= endRange) {
            return number;
        }
        throw new RuntimeException("Invalid range");
    }

    public void validateSpecialCharAllowed(SpecialChar specialChar){
        if(!specialChars.contains(specialChar)){
            throw new RuntimeException(String.format("Special char %s not supported!", specialChar));
        }
    }

    public static FieldConstraints forField(CronParameter field) {
        switch (field) {
            case SECOND:
            case MINUTE:
                return new FieldConstraints().setValidationRange(0, 59);
            case HOUR:
                return new FieldConstraints().setValidationRange(0, 23);
            case DAY_OF_WEEK:
                Map<Integer, Integer> intMapping = Maps.newHashMap();
                intMapping.put(7, 0);
                return new FieldConstraints()
                        .registerStringToIntMapping(daysOfWeekMapping())
                        .registerIntToIntMapping(intMapping)
                        .setValidationRange(0, 6);
            case DAY_OF_MONTH:
                return new FieldConstraints().setValidationRange(1, 31);
            case MONTH:
                return new FieldConstraints()
                        .registerStringToIntMapping(monthsMapping())
                        .setValidationRange(1, 12);
            default:
                return nullConstraints();
        }
    }

    public static FieldConstraints nullConstraints() {
        return new FieldConstraints();
    }

    private static Map<String, Integer> daysOfWeekMapping(){
        Map<String, Integer> stringMapping = Maps.newHashMap();
        stringMapping.put("MON",1);
        stringMapping.put("TUE",2);
        stringMapping.put("WED",3);
        stringMapping.put("THU",4);
        stringMapping.put("FRI",5);
        stringMapping.put("SAT",6);
        stringMapping.put("SUN",7);
        return stringMapping;
    }

    private static Map<String, Integer> monthsMapping(){
        Map<String, Integer> stringMapping = Maps.newHashMap();
        stringMapping.put("JAN",1);
        stringMapping.put("FEB",2);
        stringMapping.put("MAR",3);
        stringMapping.put("APR",4);
        stringMapping.put("MAY",5);
        stringMapping.put("JUN",6);
        stringMapping.put("JUL",7);
        stringMapping.put("AUG",8);
        stringMapping.put("SEP",9);
        stringMapping.put("OCT",10);
        stringMapping.put("NOV",11);
        stringMapping.put("DEC",12);
        return stringMapping;
    }
}
