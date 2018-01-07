/*
 * Copyright 2014 jmrozanec
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
package com.cronutils.utils;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

public class DateUtils {

    public static int workdaysCount(ZonedDateTime startDate, int days, List<ZonedDateTime> holidays, WeekendPolicy weekendPolicy){
        ZonedDateTime endDate = startDate.plusDays(days);
        Collections.sort(holidays);
        holidays = holidaysInRange(startDate, endDate, holidays);
        int daysToWeekend = WeekendPolicy.daysToWeekend(weekendPolicy, startDate);
        int daysFromWeekend = WeekendPolicy.daysToWeekend(weekendPolicy, endDate);
        long daysBetween = Duration.between(startDate, endDate).toDays();
        System.out.println(String.format("%s %s %s", daysToWeekend, daysFromWeekend, daysBetween));//TODO
        if(daysBetween < (daysToWeekend+daysFromWeekend+holidays.size())){
            //TODO complete
        }else{
            //TODO complete
        }

        return 0;
    }

    private static List<ZonedDateTime> holidaysInRange(ZonedDateTime startDate, ZonedDateTime endDate, List<ZonedDateTime> holidays){
        if(holidays.isEmpty()){
            return holidays;
        }
        int idxstart = findStartIdx(0, holidays.size()-1, startDate, holidays);
        int idxend = findEndIdx(0, holidays.size()-1, endDate, holidays);
        return holidays.subList(idxstart, idxend);
    }

    private static int findStartIdx(int startidx, int endidx, ZonedDateTime startDate, List<ZonedDateTime> holidays){
        if(startidx==endidx){
            return startidx;
        }
        int pivot = (endidx-startidx)/2;
        if(holidays.get(pivot).equals(startDate)){
            return pivot;
        }
        if(holidays.get(pivot).isBefore(startDate)){
            return findStartIdx(pivot, endidx, startDate, holidays);
        }else{
            return findStartIdx(startidx, pivot, startDate, holidays);
        }
    }

    private static int findEndIdx(int startidx, int endidx, ZonedDateTime endDate, List<ZonedDateTime> holidays){
        if(startidx==endidx){
            return startidx;
        }
        int pivot = (endidx-startidx)/2;
        if(holidays.get(pivot).equals(endDate)){
            return pivot;
        }
        if(holidays.get(pivot).isBefore(endDate)){
            return findEndIdx(pivot, endidx, endDate, holidays);
        }else{
            return findEndIdx(startidx, pivot, endDate, holidays);
        }
    }
}
