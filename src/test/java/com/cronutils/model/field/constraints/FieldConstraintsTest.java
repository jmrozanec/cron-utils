/*
 * Copyright 2015 jmrozanec
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

package com.cronutils.model.field.constraints;

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FieldConstraintsTest {

    private Map<String, Integer> stringMapping;
    private Map<Integer, Integer> intMapping;
    private Set<SpecialChar> specialCharSet;
    private int startRange;
    private int endRange;
    private boolean strictRange;
    private FieldConstraints fieldConstraints;

    @BeforeEach
    public void setUp() {
        intMapping = Collections.emptyMap();
        stringMapping = Collections.emptyMap();
        specialCharSet = Collections.emptySet();
        startRange = 0;
        endRange = 59;
        strictRange = true;
        fieldConstraints = new FieldConstraints(Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), startRange, endRange, true);
    }

    @Test
    public void testConstructorStringMappingNull() {
        assertThrows(NullPointerException.class, () -> new FieldConstraints(null, intMapping, specialCharSet, startRange, endRange, strictRange));
    }

    @Test
    public void testConstructorIntMappingNull() {
        assertThrows(NullPointerException.class, () -> new FieldConstraints(stringMapping, null, specialCharSet, startRange, endRange, strictRange));
    }

    @Test
    public void testSpecialCharsSetNull() {
        assertThrows(NullPointerException.class, () -> new FieldConstraints(stringMapping, intMapping, null, startRange, endRange, strictRange));
    }
    
    @Test
    public void testIsInRangeForFieldValue() {
        final SpecialCharFieldValue nonIntegerFieldValue = new SpecialCharFieldValue(SpecialChar.LW);
        fieldConstraints.isInRange(nonIntegerFieldValue);

        final IntegerFieldValue integerValue = new IntegerFieldValue(5);
        fieldConstraints.isInRange(integerValue);
    }

    @Test
    public void testIsInRangeForFieldValueOORangeStrict() {
        final IntegerFieldValue integerValue = new IntegerFieldValue(999);
        assertThrows(IllegalArgumentException.class, () -> fieldConstraints.isInRange(integerValue));
    }

}
