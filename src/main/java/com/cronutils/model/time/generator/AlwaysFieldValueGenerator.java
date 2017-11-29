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
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.FieldExpression;

class AlwaysFieldValueGenerator extends FieldValueGenerator {
    public AlwaysFieldValueGenerator(final CronField cronField) {
        super(cronField);
    }

    @Override
    public int generateNextValue(final int reference) throws NoSuchValueException {
        final int newvalue = reference + 1;
        if (newvalue <= cronField.getConstraints().getEndRange()) {
            return newvalue;
        } else {
            throw new NoSuchValueException();
        }
    }

    @Override
    public int generatePreviousValue(final int reference) throws NoSuchValueException {
        final int newvalue = reference - 1;
        if (newvalue >= cronField.getConstraints().getStartRange()) {
            return newvalue;
        } else {
            throw new NoSuchValueException();
        }
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(final int start, final int end) {
        final List<Integer> values = new ArrayList<>();
        for (int j = start + 1; j < end; j++) {
            values.add(j);
        }
        return values;
    }

    @Override
    public boolean isMatch(final int value) {
        return cronField.getField() == CronFieldName.DAY_OF_WEEK || cronField.getConstraints().isInRange(value);
        //TODO for DoW we do not have enough information to reject.
        //TODO We should think of a better strategy to propagate context information that would allow us perform validations.
    }

    @Override
    protected boolean matchesFieldExpressionClass(final FieldExpression fieldExpression) {
        return fieldExpression instanceof Always;
    }
}
