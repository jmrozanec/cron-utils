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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.cronutils.mapper.ConstantsMapper;
import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.Every;
import com.cronutils.utils.Preconditions;

/**
 * Generates matching days for a given year and month for a given day of week cron field with an every expression.
 */
class EveryDayOfWeekValueGenerator extends EveryFieldValueGenerator {
    private final int lastDayOfMonth;
    private int year;
    private int month;
    private final Set<DayOfWeek> dowValidValues;

    EveryDayOfWeekValueGenerator(final CronField cronField, final int year, final int month, WeekDay mondayDoWValue) {
        super(cronField);
        Preconditions.checkArgument(CronFieldName.DAY_OF_WEEK.equals(cronField.getField()), "CronField does not belong to day of week");
        this.year = year;
        this.month = month;
        final LocalDate date = LocalDate.of(year, month, 1);
        lastDayOfMonth = date.lengthOfMonth();

        // from is set by EveryFieldValueGenerator to be the first day of the week to start counting from
        // and to is set by EveryFieldValueGenerator to be the last day of the week
        // in the case of from-to/period (ex. MON-FRI/2)
        final Every every = (Every) cronField.getExpression();
        int period = every.getPeriod().getValue();
        Preconditions.checkArgument(period > 0 && period < 8, "Cron Expression for day of week has an invalid period.");
        dowValidValues = getValidDays(mondayDoWValue, period, from, to);
    }

    private static Set<DayOfWeek> getValidDays(WeekDay mondayDoWValue, int period, int from, int to) {
        List<DayOfWeek> validDays = new ArrayList<>(7);
        for (int day = from; day <= to; day += period) {
            // Convert from cron day of the week to Java DayOfWeek
            int javaDay = ConstantsMapper.weekDayMapping(mondayDoWValue, ConstantsMapper.JAVA8, day);
            validDays.add(DayOfWeek.of(javaDay));
        }
        return EnumSet.copyOf(validDays);
    }

    @Override
    public int generateNextValue(final int reference) throws NoSuchValueException {
        int day = reference;
        do {
            day++;
        } while (!isMatch(day) && day <= lastDayOfMonth);
        if (day > lastDayOfMonth) {
            throw new NoSuchValueException();
        }
        return day;
    }

    @Override
    public int generatePreviousValue(final int reference) throws NoSuchValueException {
        int day = reference;
        do {
            day--;
        } while (!isMatch(day) && day > 0);
        if (day <= 0) {
            throw new NoSuchValueException();
        }
        return day;
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
        // start is the day of month to start from
        List<Integer> candidates = new ArrayList<>();
        int reference = start;
        try {
            while (reference < end) {
                reference = generateNextValue(reference);
                if (reference < end) {
                    candidates.add(reference);
                }
            }
        } catch (NoSuchValueException ignored) {
            // next generated value would be beyond the end of the month, so just ignore it and finish
        }
        return candidates;
    }

    @Override
    public boolean isMatch(final int value) {
        // value is the day of the month
        if (value > lastDayOfMonth || value < 1) {
            return false;
        }
        DayOfWeek dayOfWeek = LocalDate.of(year, month, value).getDayOfWeek();
        return dowValidValues.contains(dayOfWeek);
    }
}
