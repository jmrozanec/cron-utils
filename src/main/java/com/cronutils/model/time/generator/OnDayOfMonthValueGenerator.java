package com.cronutils.model.time.generator;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.utils.Preconditions;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
class OnDayOfMonthValueGenerator extends FieldValueGenerator {
    private int year;
    private int month;

    public OnDayOfMonthValueGenerator(CronField cronField, int year, int month) {
        super(cronField);
        Preconditions.checkArgument(CronFieldName.DAY_OF_MONTH.equals(cronField.getField()), "CronField does not belong to day of" +
                " month");
        this.year = year;
        this.month = month;
    }

    @Override
    public int generateNextValue(int reference) throws NoSuchValueException {
        On on = ((On) cronField.getExpression());
        int value = generateValue(on, year, month);

        if (value <= reference) {
            throw new NoSuchValueException();
        }
        return value;
    }

    @Override
    public int generatePreviousValue(int reference) throws NoSuchValueException {
        On on = ((On) cronField.getExpression());
        int value = generateValue(on, year, month);
        if (value >= reference) {
            throw new NoSuchValueException();
        }
        return value;
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
        List<Integer> values = new ArrayList<>();
        try {
            int reference = generateNextValue(start);
            while (reference < end) {
                values.add(reference);
                reference = generateNextValue(reference);
            }
        } catch (NoSuchValueException e) {
        }
        return values;
    }

    @Override
    public boolean isMatch(int value) {
        On on = ((On) cronField.getExpression());
        try {
            return value == generateValue(on, year, month);
        } catch (NoSuchValueException ignored) {
        }//we just skip, since we generate values until we get the exception
        return false;
    }

    @Override
    protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
        return fieldExpression instanceof On;
    }

    private int generateValue(On on, int year, int month) throws NoSuchValueException {
        int dayOfMonth = on.getTime().getValue();
        switch (on.getSpecialChar().getValue()) {
            case L:
                return LocalDate.of(year, month, 1).lengthOfMonth();
            case W: // First work day of the week
                LocalDate doM = LocalDate.of(year, month, dayOfMonth);
                if (doM.getDayOfWeek() == DayOfWeek.SATURDAY) {//dayOfWeek is Saturday!
                    if (dayOfMonth == 1) {//first day in month is Saturday! We execute on Monday
                        return 3;
                    }
                    return dayOfMonth - 1;
                }
                if (doM.getDayOfWeek() == DayOfWeek.SUNDAY) { // dayOfWeek is Sunday
                    if ((dayOfMonth + 1) <= doM.lengthOfMonth()) {
                        return dayOfMonth + 1;
                    }
                }
                return dayOfMonth;  // first day of week is a weekday            
            case LW:
                LocalDate lastDayOfMonth = LocalDate.of(year, month, LocalDate.of(year, month, 1).lengthOfMonth());
                int dow = lastDayOfMonth.getDayOfWeek().getValue();
                int diff = dow - 5;
                if (diff > 0) {
                    return lastDayOfMonth.minusDays(diff).getDayOfMonth();
                }
                return lastDayOfMonth.getDayOfMonth();
            default:
                throw new NoSuchValueException();
        }
    }
}
