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
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;

class OnFieldValueGenerator extends FieldValueGenerator {
    public OnFieldValueGenerator(final CronField cronField) {
        super(cronField);
    }

    @Override
    public int generateNextValue(final int reference) throws NoSuchValueException {
        final int time = ((On) cronField.getExpression()).getTime().getValue();
        if (time <= reference) {
            throw new NoSuchValueException();
        }
        return time;
    }

    @Override
    public int generatePreviousValue(final int reference) throws NoSuchValueException {
        final int time = ((On) cronField.getExpression()).getTime().getValue();
        if (time >= reference) {
            throw new NoSuchValueException();
        }
        return time;
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(final int start, final int end) {
        final List<Integer> values = new ArrayList<>();
        final int time = ((On) cronField.getExpression()).getTime().getValue();
        if (time > start && time < end) {
            values.add(time);
        }
        return values;
    }

    @Override
    public boolean isMatch(final int value) {
        return ((On) cronField.getExpression()).getTime().getValue() == value;
    }

    @Override
    protected boolean matchesFieldExpressionClass(final FieldExpression fieldExpression) {
        return fieldExpression instanceof On;
    }
}
