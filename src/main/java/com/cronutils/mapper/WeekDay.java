package com.cronutils.mapper;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.Validate;

//as a convention we consider MONDAY index to compare weekday conventions and perform conversions
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
@VisibleForTesting
public class WeekDay {
    private int mondayDoWValue;
    private boolean firstDayIsZero;

    public WeekDay(int mondayDoWValue, boolean firstDayIsZero){
        Validate.isTrue(mondayDoWValue>=0, "Monday Day of Week value must be greater or equal to zero");
        this.mondayDoWValue = mondayDoWValue;
        this.firstDayIsZero = firstDayIsZero;
    }

    public int getMondayDoWValue() {
        return mondayDoWValue;
    }

    /**
     * Maps given WeekDay to representation hold by this instance.
     * @param weekDay - referred weekDay
     * @param dayOfWeek - day of week to be mapped
     * @return - int result
     */
    public int map(WeekDay weekDay, int dayOfWeek){
        int result = mondayDoWValue - weekDay.getMondayDoWValue() + dayOfWeek;
        if(result==0){
            if(firstDayIsZero){
                result = 0;
            }else{
                result=7;
            }
        }
        return result;
    }
}
