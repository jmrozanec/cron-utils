package com.cronutils.model.field.constraint;

import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
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
 * Holds information on valid values for a field
 * and allows to perform mappings and validations.
 * Example of information for valid field values: int range,
 * valid special characters, valid nominal values.
 * Example for mappings: conversions from nominal values to integers
 * and integer-integer mappings if more than one integer
 * represents the same concept.
 */
public class FieldConstraints {
    private Map<String, Integer> stringMapping;
    private Map<Integer, Integer> intMapping;
    private Set<SpecialChar> specialChars;
    private int startRange;
    private int endRange;

    /**
     * @param specialChars - allowed special chars
     * @param startRange - lowest possible value
     * @param endRange - highest possible value
     */
    public FieldConstraints(Map<String, Integer> stringMapping, Map<Integer, Integer> intMapping, Set<SpecialChar> specialChars, int startRange, int endRange) {
        this.stringMapping = Collections.unmodifiableMap(Validate.notNull(stringMapping, "String mapping must not be null"));
        this.intMapping = Collections.unmodifiableMap(Validate.notNull(intMapping, "Integer mapping must not be null"));
        this.specialChars = Collections.unmodifiableSet(Validate.notNull(specialChars, "Special (non-standard) chars set must not be null"));
        this.startRange = startRange;
        this.endRange = endRange;
    }

    public int getStartRange() {
        return startRange;
    }

    public int getEndRange() {
        return endRange;
    }

    public Set<SpecialChar> getSpecialChars() {
        return specialChars;
    }

    /**
     * Check if given number is greater or equal to start range and minor or equal to end range
     * @param value - to be checked
     */
    public boolean isInRange(int value) {
        return value >= getStartRange() && value <= getEndRange();
    }

    public Map<String, Integer> getStringMapping() {
        return stringMapping;
    }

    public Map<Integer, Integer> getIntMapping() {
        return intMapping;
    }
}

