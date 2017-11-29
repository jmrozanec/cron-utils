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

package com.cronutils.mapper;

import java.io.Serializable;

import com.cronutils.Function;
import com.cronutils.utils.Preconditions;
import com.cronutils.utils.VisibleForTesting;

@VisibleForTesting
public class WeekDay implements Serializable {

    private static final long serialVersionUID = -1542525283511798919L;
    private final int mondayDoWValue;
    private final boolean firstDayZero;

    public WeekDay(final int mondayDoWValue, final boolean firstDayZero) {
        Preconditions.checkArgument(mondayDoWValue >= 0, "Monday Day of Week value must be greater or equal to zero");
        this.mondayDoWValue = mondayDoWValue;
        this.firstDayZero = firstDayZero;
    }

    public int getMondayDoWValue() {
        return mondayDoWValue;
    }

    public boolean isFirstDayZero() {
        return firstDayZero;
    }

    /**
     * Maps given WeekDay to representation hold by this instance.
     *
     * @param targetWeekDayDefinition - referred weekDay
     * @param dayOfWeek               - day of week to be mapped.
     *                                Value corresponds to this instance mapping.
     * @return - int result
     */
    public int mapTo(final int dayOfWeek, final WeekDay targetWeekDayDefinition) {
        if (firstDayZero && targetWeekDayDefinition.isFirstDayZero()) {
            return bothSameStartOfRange(0, 6, this, targetWeekDayDefinition).apply(dayOfWeek);
        }
        if (!firstDayZero && !targetWeekDayDefinition.isFirstDayZero()) {
            return bothSameStartOfRange(1, 7, this, targetWeekDayDefinition).apply(dayOfWeek);
        }
        //start range is different for each case. We need to normalize ranges
        if (targetWeekDayDefinition.isFirstDayZero()) {
            //my range is 1-7. I normalize ranges, get the "zero" mapping and turn result into original scale
            return mapTo(dayOfWeek, new WeekDay(targetWeekDayDefinition.getMondayDoWValue() + 1, false)) - 1;
        } else {
            //my range is 0-6. I normalize ranges, get the "one" mapping and turn result into original scale
            return mapTo(dayOfWeek, new WeekDay(targetWeekDayDefinition.getMondayDoWValue() - 1, true)) + 1;
        }
    }

    private Function<Integer, Integer> bothSameStartOfRange(final int startRange, final int endRange, final WeekDay source, final WeekDay target) {
        return integer -> {
            final int diff = target.getMondayDoWValue() - source.getMondayDoWValue();
            int result = integer;
            if (diff == 0) {
                return integer;
            }
            if (diff < 0) {
                result = integer + diff;
                final int distanceToStartRange = startRange - result;
                if (result < startRange) {
                    result = endRange + 1 - distanceToStartRange;
                }
            }
            if (diff > 0) {
                result = integer + diff;
                if (result > endRange) {
                    result -= endRange;
                }
            }
            return result;
        };
    }
}
