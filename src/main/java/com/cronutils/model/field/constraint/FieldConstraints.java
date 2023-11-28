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

package com.cronutils.model.field.constraint;

import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.utils.Preconditions;
import com.cronutils.utils.VisibleForTesting;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Holds information on valid values for a field and allows to perform mappings and validations. Example of information for valid field
 * values: int range, valid special characters, valid nominal values. Example for mappings: conversions from nominal values to integers and
 * integer-integer mappings if more than one integer represents the same concept.
 */
public class FieldConstraints implements Serializable {

    private static final long serialVersionUID = -9112124669329704710L;
    private final Map<String, Integer> stringMapping;
    private final Map<Integer, Integer> intMapping;
    private final Set<SpecialChar> specialChars;
    private final Integer startRange;
    private final Integer endRange;
    private final boolean strictRange;

    /**
     * Constructor.
     *
     * @param specialChars - allowed special chars.
     * @param startRange   - lowest possible value
     * @param endRange     - highest possible value
     * @param strictRange  - if we shall consider strict ranges for this field - regardless global strict ranges criteria
     */
    public FieldConstraints(final Map<String, Integer> stringMapping, final Map<Integer, Integer> intMapping, final Set<SpecialChar> specialChars, final int startRange,
            final int endRange, final boolean strictRange) {
        this.stringMapping = Collections.unmodifiableMap(Preconditions.checkNotNull(stringMapping, "String mapping must not be null"));
        this.intMapping = Collections.unmodifiableMap(Preconditions.checkNotNull(intMapping, "Integer mapping must not be null"));
        this.specialChars = Collections.unmodifiableSet(Preconditions.checkNotNull(specialChars, "Special (non-standard) chars set must not be null"));
        this.startRange = startRange;
        this.endRange = endRange;
        this.strictRange = strictRange;
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
     * Check if given number is greater or equal to start range and minor or equal to end range.
     *
     * @param fieldValue - to be validated
     * @throws IllegalArgumentException - if not in range
     */
    @VisibleForTesting
    public void isInRange(final FieldValue<?> fieldValue) {
        if (fieldValue instanceof IntegerFieldValue) {
            final int value = ((IntegerFieldValue) fieldValue).getValue();
            if (!isInRange(value)) {
                throw new IllegalArgumentException(String.format("Value %s not in range [%s, %s]", value, getStartRange(), getEndRange()));
            }
        }
    }

    /**
     * Check if given number is greater or equal to start range and minor or equal to end range.
     *
     * @param value - to be checked
     */
    public boolean isInRange(final int value) {
        return value >= getStartRange() && value <= getEndRange();
    }

    /**
     * Check if given period is compatible with range.
     *
     * @param fieldValue - to be validated
     * @throws IllegalArgumentException - if not in range
     */
    @VisibleForTesting
    public void isPeriodInRange(final FieldValue<?> fieldValue) {
        if (fieldValue instanceof IntegerFieldValue) {
            final int value = ((IntegerFieldValue) fieldValue).getValue();
            if (!isPeriodInRange(value)) {
                throw new IllegalArgumentException(
                        String.format("Period %s not in range [%s, %s]", value, getStartRange(), getEndRange()));
            }
        }
    }
    
    /**
     * Check if given period is compatible with the given range.
     *
     * @param period - to be checked
     * @return {@code true} if period is compatible, {@code false} otherwise.
     */
    public boolean isPeriodInRange(final int period) {
        return period > 0 && period <= (getEndRange() - getStartRange() +1) && period <= getEndRange();
    }

    public Set<String> getStringMappingKeySet() {
        return stringMapping.keySet();
    }

    public Integer getStringMappingValue(final String exp) {
        return stringMapping.get(exp);
    }

    public Integer getIntMappingValue(final Integer exp) {
        return intMapping.get(exp);
    }

    public boolean isStrictRange() {
        return strictRange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldConstraints that = (FieldConstraints) o;
        return strictRange == that.strictRange && stringMapping.equals(that.stringMapping)
                && intMapping.equals(that.intMapping) && specialChars.equals(that.specialChars)
                && startRange.equals(that.startRange) && endRange.equals(that.endRange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringMapping, intMapping, specialChars, startRange, endRange, strictRange);
    }
}
