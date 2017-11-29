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

package com.cronutils.model.time.generator;

import java.util.ArrayList;
import java.util.List;

import com.cronutils.model.field.CronField;

abstract class OnDayOfCalendarValueGenerator extends FieldValueGenerator {
    protected int year;
    protected int month;

    OnDayOfCalendarValueGenerator(final CronField cronField, final int year, final int month) {
        super(cronField);
        this.year = year;
        this.month = month;
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(final int start, final int end) {
        final List<Integer> values = new ArrayList<>();
        try {
            int reference = generateNextValue(start);
            while (reference < end) {
                values.add(reference);
                reference = generateNextValue(reference);
            }
        } catch (final NoSuchValueException ignored) { /*NOP*/ }
        return values;
    }
}
