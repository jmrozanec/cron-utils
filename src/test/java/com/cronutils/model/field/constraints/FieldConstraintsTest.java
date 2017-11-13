package com.cronutils.model.field.constraints;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.value.SpecialChar;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
public class FieldConstraintsTest {

    private Map<String, Integer> stringMapping;
    private Map<Integer, Integer> intMapping;
    private Set<SpecialChar> specialCharSet;
    private int startRange;
    private int endRange;

    @Before
    public void setUp() throws Exception {
        intMapping = Maps.newHashMap();
        stringMapping = Maps.newHashMap();
        specialCharSet = Sets.newHashSet();
        startRange = 0;
        endRange = 59;
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorStringMappingNull() throws Exception {
        new FieldConstraints(null, intMapping, specialCharSet, startRange, endRange);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorIntMappingNull() throws Exception {
        new FieldConstraints(stringMapping, null, specialCharSet, startRange, endRange);
    }

    @Test(expected = NullPointerException.class)
    public void testSpecialCharsSetNull() throws Exception {
        new FieldConstraints(stringMapping, intMapping, null, startRange, endRange);
    }
}
