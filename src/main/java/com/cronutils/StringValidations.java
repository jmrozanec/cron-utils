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

package com.cronutils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.utils.VisibleForTesting;

import static com.cronutils.model.field.value.SpecialChar.L;
import static com.cronutils.model.field.value.SpecialChar.LW;
import static com.cronutils.model.field.value.SpecialChar.W;

public class StringValidations {
    private static final String ESCAPED_END = ")\\b";
    private static final String ESCAPED_START = "\\b(";
    private static final SpecialChar[] SPECIAL_CHARS = new SpecialChar[] { L, LW, W };
    private static final Pattern NUMS_AND_CHARS_PATTERN = Pattern.compile("[#\\?/\\*0-9]");

    private final Pattern stringToIntKeysPattern;
    private final Pattern lwPattern;

    public StringValidations(final FieldConstraints constraints) {
        lwPattern = buildLWPattern(constraints.getSpecialChars());
        stringToIntKeysPattern = buildStringToIntPattern(constraints.getStringMappingKeySet());
    }

    @VisibleForTesting
    Pattern buildStringToIntPattern(final Set<String> strings) {
        return buildWordsPattern(strings);
    }

    @VisibleForTesting
    public String removeValidChars(final String exp) {
        final Matcher numsAndCharsMatcher = NUMS_AND_CHARS_PATTERN.matcher(exp.toUpperCase());
        final Matcher stringToIntKeysMatcher = stringToIntKeysPattern.matcher(numsAndCharsMatcher.replaceAll(""));
        final Matcher specialWordsMatcher = lwPattern.matcher(stringToIntKeysMatcher.replaceAll(""));
        return specialWordsMatcher.replaceAll("").replaceAll("\\s+", "").replaceAll(",", "").replaceAll("-", "");
    }

    @VisibleForTesting
    Pattern buildLWPattern(final Set<SpecialChar> specialChars) {
        final Set<String> scs = new HashSet<>();
        for (final SpecialChar sc : SPECIAL_CHARS) {
            if (specialChars.contains(sc)) {
                scs.add(sc.name());
            }
        }
        return buildWordsPattern(scs);
    }

    @VisibleForTesting
    Pattern buildWordsPattern(final Set<String> words) {
        final StringBuilder builder = new StringBuilder(ESCAPED_START);
        final Iterator<String> iterator = words.iterator();

        if (!iterator.hasNext()) {
            builder.append(ESCAPED_END);
            return Pattern.compile(builder.toString());
        }
        final String next = iterator.next();
        builder.append(next);
        while (iterator.hasNext()) {
            builder.append("|");
            builder.append(iterator.next());
        }
        builder.append(ESCAPED_END);
        return Pattern.compile(builder.toString());
    }
}
