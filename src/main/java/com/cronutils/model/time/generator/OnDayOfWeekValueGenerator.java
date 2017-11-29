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

import com.cronutils.mapper.ConstantsMapper;
import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.utils.Preconditions;

class OnDayOfWeekValueGenerator extends OnDayOfCalendarValueGenerator {

    private static final On ON_SATURDAY = new On(new IntegerFieldValue(7));
    private final WeekDay mondayDoWValue;

    public OnDayOfWeekValueGenerator(final CronField cronField, final int year, final int month, final WeekDay mondayDoWValue) {
        super(cronField, year, month);
        Preconditions.checkArgument(CronFieldName.DAY_OF_WEEK.equals(cronField.getField()), "CronField does not belong to day of week");
        this.mondayDoWValue = mondayDoWValue;
    }

    @Override
    public int generateNextValue(final int reference) throws NoSuchValueException {
        final On on = ((On) cronField.getExpression());
        final int value = generateValue(on, year, month, reference);
        if (value <= reference) {
            throw new NoSuchValueException();
        }
        return value;
    }

    @Override
    public int generatePreviousValue(final int reference) throws NoSuchValueException {
        final On on = ((On) cronField.getExpression());
        final int value = generateValue(on, year, month, reference);
        if (value >= reference) {
            throw new NoSuchValueException();
        }
        return value;
    }

    @Override
    public boolean isMatch(final int value) {
        final On on = ((On) cronField.getExpression());
        try {
            return value == generateValue(on, year, month, value - 1);
        } catch (final NoSuchValueException ignored) {
            //we just skip, since we generate values until we get the exception
        }
        return false;
    }

    @Override
    protected boolean matchesFieldExpressionClass(final FieldExpression fieldExpression) {
        return fieldExpression instanceof On;
    }

    private int generateValue(final On on, final int year, final int month, final int reference) throws NoSuchValueException {
        switch (on.getSpecialChar().getValue()) {
            case HASH:
                return generateHashValues(on, year, month);
            case L:
                return on.getTime().getValue() == -1 ? /* L by itself simply means “7” or “SAT” */
                        generateNoneValues(ON_SATURDAY, year, month, reference) :
                        generateLValues(on, year, month);
            case NONE:
                return generateNoneValues(on, year, month, reference);
            default:
                throw new NoSuchValueException();
        }
    }

    private int generateHashValues(final On on, final int year, final int month) {
        final DayOfWeek dowForFirstDoM = LocalDate.of(year, month, 1).getDayOfWeek();//1-7
        final int requiredDoW = ConstantsMapper.weekDayMapping(mondayDoWValue, ConstantsMapper.JAVA8, on.getTime().getValue());//to normalize to jdk8-time value
        final int requiredNth = on.getNth().getValue();
        int baseDay = 1;//day 1 from given month
        final int diff = dowForFirstDoM.getValue() - requiredDoW;
        if (diff < 0) {
            baseDay = baseDay + Math.abs(diff);
        }
        if (diff > 0) {
            baseDay = baseDay + 7 - diff;
        }
        return (requiredNth - 1) * 7 + baseDay;
    }

    private int generateLValues(final On on, final int year, final int month) throws NoSuchValueException {
        final int lastDoM = LocalDate.of(year, month, 1).lengthOfMonth();
        final LocalDate lastDoMDateTime = LocalDate.of(year, month, lastDoM);
        final int dowForLastDoM = lastDoMDateTime.getDayOfWeek().getValue();//1-7
        final int requiredDoW = ConstantsMapper.weekDayMapping(mondayDoWValue, ConstantsMapper.JAVA8, on.getTime().getValue());//to normalize to jdk8-time value
        final int dowDiff = dowForLastDoM - requiredDoW;

        if (dowDiff == 0) {
            return lastDoMDateTime.getDayOfMonth();
        }
        if (dowDiff < 0) {
            return lastDoMDateTime.minusDays(dowForLastDoM + (long)(7 - requiredDoW)).getDayOfMonth();
        }
        if (dowDiff > 0) {
            return lastDoMDateTime.minusDays(dowDiff).getDayOfMonth();
        }
        throw new NoSuchValueException();
    }

    /**
     * Generate valid days of the month for the days of week expression. This method requires that you
     * pass it a -1 for the reference value when starting to generate a sequence of day values. That allows
     * it to handle the special case of which day of the month is the initial matching value.
     *
     * @param on        The expression object giving us the particular day of week we need.
     * @param year      The year for the calculation.
     * @param month     The month for the calculation.
     * @param reference This value must either be -1 indicating you are starting the sequence generation or an actual
     *                  day of month that meets the day of week criteria. So a value previously returned by this method.
     * @return
     */
    private int generateNoneValues(final On on, final int year, final int month, final int reference) {
        // the day of week the first of the month is on
        final int dowForFirstDoM = LocalDate.of(year, month, 1).getDayOfWeek().getValue();// 1-7
        // the day of week we need, normalize to jdk8time
        final int requiredDoW = ConstantsMapper.weekDayMapping(mondayDoWValue, ConstantsMapper.JAVA8, on.getTime().getValue());
        // the first day of the month
        int baseDay = 1;// day 1 from given month
        // the difference between the days of week
        final int diff = dowForFirstDoM - requiredDoW;
        // //base day remains the same if diff is zero
        if (diff < 0) {
            baseDay = baseDay + Math.abs(diff);
        }
        if (diff > 0) {
            baseDay = baseDay + 7 - diff;
        }
        // if baseDay is greater than the reference, we are returning the initial matching day value
        //Fix issue #92
        if (reference < 1) {
            return baseDay;
        }
        while (baseDay <= reference) {
            baseDay += 7;
        }
        return baseDay;
    }
}
