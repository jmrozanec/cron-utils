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

package com.cronutils.model.field.expression;

import com.cronutils.mapper.ConstantsMapper;
import com.cronutils.mapper.WeekDay;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.DayOfWeekFieldDefinition;

public enum Weekdays {
    MONDAY(1), TUESDAY(2), WEDNESDAY(3), THURSDAY(4), FRIDAY(5), SATURDAY(6), SUNDAY(7);

    private int weekday;

    Weekdays(int weekday) {
        this.weekday = weekday;
    }

    public int getWeekday() {
        return getWeekday(ConstantsMapper.CRONTAB_WEEK_DAY);
    }

    public int getWeekday(WeekDay weekDay) {
        return ConstantsMapper.weekDayMapping(ConstantsMapper.CRONTAB_WEEK_DAY, weekDay, this.weekday);
    }

    public int getWeekday(CronDefinition cronDefinition){
        return getWeekday(((DayOfWeekFieldDefinition)cronDefinition.getFieldDefinition(CronFieldName.DAY_OF_WEEK)).getMondayDoWValue());
    }
}
