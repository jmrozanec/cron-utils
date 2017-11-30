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

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.utils.Preconditions;

class OnDayOfMonthValueGenerator extends OnDayOfCalendarValueGenerator {

    public OnDayOfMonthValueGenerator(final CronField cronField, final int year, final int month) {
        super(cronField, year, month);
        Preconditions.checkArgument(CronFieldName.DAY_OF_MONTH.equals(cronField.getField()), "CronField does not belong to day of month");
    }

    @Override
    public int generateNextValue(final int reference) throws NoSuchValueException {
        final On on = ((On) cronField.getExpression());
        final int value = generateValue(on, year, month);

        if (value <= reference) {
            throw new NoSuchValueException();
        }
        return value;
    }

    @Override
    public int generatePreviousValue(final int reference) throws NoSuchValueException {
        final On on = ((On) cronField.getExpression());
        final int value = generateValue(on, year, month);
        if (value >= reference) {
            throw new NoSuchValueException();
        }
        return value;
    }

    @Override
    public boolean isMatch(final int value) {
        final On on = ((On) cronField.getExpression());
        try {
            return value == generateValue(on, year, month);
        } catch (final NoSuchValueException ignored) {
            //we just skip, since we generate values until we get the exception
        }
        return false;
    }

    @Override
    protected boolean matchesFieldExpressionClass(final FieldExpression fieldExpression) {
        return fieldExpression instanceof On;
    }

    private int generateValue(final On on, final int year, final int month) throws NoSuchValueException {
        final int dayOfMonth = on.getTime().getValue();
        switch (on.getSpecialChar().getValue()) {
            case L:
                final int daysBefore = on.getNth().getValue();
                return LocalDate.of(year, month, 1).lengthOfMonth() - (daysBefore > 0 ? daysBefore : 0);
            case W: // First work day of the week
                final LocalDate doM = LocalDate.of(year, month, dayOfMonth);
                if (doM.getDayOfWeek() == DayOfWeek.SATURDAY) { //dayOfWeek is Saturday!
                    if (dayOfMonth == 1) { //first day in month is Saturday! We execute on Monday
                        return 3;
                    }
                    return dayOfMonth - 1;
                }
                if (doM.getDayOfWeek() == DayOfWeek.SUNDAY && (dayOfMonth + 1) <= doM.lengthOfMonth()) {
                    return dayOfMonth + 1;
                }
                return dayOfMonth;  // first day of week is a weekday
            case LW:
                final LocalDate lastDayOfMonth = LocalDate.of(year, month, LocalDate.of(year, month, 1).lengthOfMonth());
                final int dow = lastDayOfMonth.getDayOfWeek().getValue();
                final int diff = dow - 5;
                if (diff > 0) {
                    return lastDayOfMonth.minusDays(diff).getDayOfMonth();
                }
                return lastDayOfMonth.getDayOfMonth();
            default:
                throw new NoSuchValueException();
        }
    }
}
