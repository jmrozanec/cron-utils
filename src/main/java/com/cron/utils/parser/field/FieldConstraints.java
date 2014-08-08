package com.cron.utils.parser.field;

import com.cron.utils.CronParameter;

import java.util.HashMap;
import java.util.Map;

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

    public FieldConstraints() {
        stringMapping = new HashMap<String, Integer>();
        intMapping = new HashMap<Integer, Integer>();
        startRange = 0;//no negatives!
        endRange = Integer.MAX_VALUE;
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

    public static FieldConstraints forField(CronParameter field) {
        switch (field) {
            case SECOND:
            case MINUTE:
                return new FieldConstraints().setValidationRange(0, 59);
            case HOUR:
                return new FieldConstraints().setValidationRange(0, 23);
            case DAY_OF_WEEK:
                Map<Integer, Integer> intMapping = new HashMap<Integer, Integer>();
                intMapping.put(7, 0);
                return new FieldConstraints().registerIntToIntMapping(intMapping).setValidationRange(0, 6);
            case DAY_OF_MONTH:
                return new FieldConstraints().setValidationRange(1, 31);
            case MONTH:
                return new FieldConstraints().setValidationRange(1, 12);
            default:
                return nullConstraints();
        }
    }

    public static FieldConstraints nullConstraints() {
        return new FieldConstraints();
    }
}
