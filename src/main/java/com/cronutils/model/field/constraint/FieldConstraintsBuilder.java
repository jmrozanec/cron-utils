package com.cronutils.model.field.constraint;

import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.value.SpecialChar;

import java.util.HashMap;
import java.util.HashSet;
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

/**
 * FieldConstraints builder.
 */
public class FieldConstraintsBuilder {
    private Map<String, Integer> stringMapping;
    private Map<Integer, Integer> intMapping;
    private int startRange;
    private int endRange;
    private Set<SpecialChar> specialChars;

    /**
     * Constructor
     */
    private FieldConstraintsBuilder() {
        stringMapping = new HashMap<>();
        intMapping = new HashMap<>();
        startRange = 0;//no negatives!
        endRange = Integer.MAX_VALUE;
        specialChars = new HashSet<>();
        specialChars.add(SpecialChar.NONE);
    }

    /**
     * Creates range constraints according to CronFieldName parameter
     * @param field - CronFieldName
     * @return FieldConstraintsBuilder instance
     */
    public FieldConstraintsBuilder forField(CronFieldName field) {
        switch (field) {
            case SECOND:
            case MINUTE:
                endRange = 59;
                return this;
            case HOUR:
                endRange = 23;
                return this;
            case DAY_OF_WEEK:
                stringMapping = daysOfWeekMapping();
                endRange = 6;
                return this;
            case DAY_OF_MONTH:
                startRange = 1;
                endRange = 31;
                return this;
            case MONTH:
                stringMapping = monthsMapping();
                startRange = 1;
                endRange = 12;
                return this;
            default:
                return this;
        }
    }

    /**
     * Adds hash support
     * @return same FieldConstraintsBuilder instance
     */
    public FieldConstraintsBuilder addHashSupport() {
        specialChars.add(SpecialChar.HASH);
        return this;
    }

    /**
     * Adds L support
     * @return same FieldConstraintsBuilder instance
     */
    public FieldConstraintsBuilder addLSupport() {
        specialChars.add(SpecialChar.L);
        return this;
    }

    /**
     * Adds W support
     * @return same FieldConstraintsBuilder instance
     */
    public FieldConstraintsBuilder addWSupport() {
        specialChars.add(SpecialChar.W);
        return this;
    }

    /**
     * Adds LW support
     * @return same FieldConstraintsBuilder instance
     */
    public FieldConstraintsBuilder addLWSupport() {
        specialChars.add(SpecialChar.LW);
        return this;
    }

    /**
     * Adds question mark (?) support
     * @return same FieldConstraintsBuilder instance
     */
    public FieldConstraintsBuilder addQuestionMarkSupport() {
        specialChars.add(SpecialChar.QUESTION_MARK);
        return this;
    }

    /**
     * Adds integer to integer mapping. Source should be greater than destination;
     * @param source - some int
     * @param dest - some int
     * @return same FieldConstraintsBuilder instance
     */
    public FieldConstraintsBuilder withIntValueMapping(int source, int dest){
        intMapping.put(source, dest);
        return this;
    }

    /**
     * Allows to set a range of valid values for field.
     * @param startRange - start range value
     * @param endRange - end range value
     * @return same FieldConstraintsBuilder instance
     */
    public FieldConstraintsBuilder withValidRange(int startRange, int endRange){
        this.startRange = startRange;
        this.endRange = endRange;
        return this;
    }

    /**
     * Shifts integer representation of weekday/month names
     * @param shiftSize - size of the shift
     * @return same FieldConstraintsBuilder instance
     */
    public FieldConstraintsBuilder withShiftedStringMapping(int shiftSize){
        for(String key : this.stringMapping.keySet()) {
            Integer value = this.stringMapping.get(key);
            value += shiftSize;
            if(value > endRange) {
                value -= endRange;
            }
            if(value < startRange) {
                value += (startRange - endRange);
            }
            this.stringMapping.put(key, value);
        }
        return this;
    }

    /**
     * Creates FieldConstraints instance based on previously built parameters
     * @return new FieldConstraints instance
     */
    public FieldConstraints createConstraintsInstance(){
        return new FieldConstraints(stringMapping, intMapping, specialChars, startRange, endRange);
    }

    /**
     * Creates days of week mapping
     * @return Map<String, Integer> where strings are weekday names in EEE format,
     * and integers correspond to their 1-7 mappings
     */
    private static Map<String, Integer> daysOfWeekMapping() {
        Map<String, Integer> stringMapping = new HashMap<>();
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
    private static Map<String, Integer> monthsMapping() {
        Map<String, Integer> stringMapping = new HashMap<>();
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

    /**
     * Creates a FieldConstraintsBuilder instance;
     * @return new FieldConstraintsBuilder instance
     */
    public static FieldConstraintsBuilder instance(){
        return new FieldConstraintsBuilder();
    }
}

