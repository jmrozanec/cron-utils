package com.cronutils.mapper;

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
public class ConstantsMapper {
    private ConstantsMapper() {
    }

    public static final WeekDay QUARTZ_WEEK_DAY = new WeekDay(2, false);
    public static final WeekDay JAVA8 = new WeekDay(1, false);
    public static final WeekDay CRONTAB_WEEK_DAY = new WeekDay(1, true);

    /**
     * Performs weekday mapping between two weekday definitions.
     *
     * @param source  - source
     * @param target  - target weekday definition
     * @param weekday - value in source range.
     * @return int - mapped value
     */
    public static int weekDayMapping(WeekDay source, WeekDay target, int weekday) {
        return source.mapTo(weekday, target);
    }
}
