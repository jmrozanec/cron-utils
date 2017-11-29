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

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.utils.Preconditions;

class AndDayOfWeekValueGenerator extends FieldValueGenerator {
    private final int year;
    private final int month;
    private final WeekDay mondayDoWValue;

    public AndDayOfWeekValueGenerator(final CronField cronField, final int year, final int month, final WeekDay mondayDoWValue) {
        super(cronField);
        Preconditions.checkArgument(CronFieldName.DAY_OF_WEEK.equals(cronField.getField()), "CronField does not belong to day of week");
        this.year = year;
        this.month = month;
        this.mondayDoWValue = mondayDoWValue;
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(final int start, final int end) {
        final List<Integer> values = new ArrayList<>();
        final And and = (And) cronField.getExpression();

        for (final FieldExpression expression : and.getExpressions()) {
            final CronField cronField = new CronField(CronFieldName.DAY_OF_WEEK, expression, this.cronField.getConstraints());
            final List<Integer> candidatesList = FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(
                    cronField, year, month, mondayDoWValue
            ).generateCandidates(start, end);

            // add them to the master list
            if (candidatesList != null) {
                values.addAll(candidatesList);
            }
        }

        return values;
    }

    @Override
    protected boolean matchesFieldExpressionClass(final FieldExpression fieldExpression) {
        return fieldExpression instanceof And;
    }

    @Override
    public int generateNextValue(final int reference) throws NoSuchValueException {
        // This method does not logically work.
        return 0;
    }

    @Override
    public int generatePreviousValue(final int reference) throws NoSuchValueException {
        // This method does not logically work.
        return 0;
    }

    @Override
    public boolean isMatch(final int value) {
        // This method does not logically work.
        return false;
    }

}
