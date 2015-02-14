package com.cronutils.mapper.format;

import com.google.common.collect.Maps;
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
public class MDYDateTimeFormatLocaleStrategy extends DateTimeFormatLocaleStrategy {

    public MDYDateTimeFormatLocaleStrategy(){
        initDayOfWeekMap();
        initMonthsMap();
    }

    @Override
    protected String parseDateSlashes(String expression) {
        return "MM/dd/YY";
    }

    private void initDayOfWeekMap(){
        dayOfWeek = Maps.newHashMap();
        dayOfWeek.put("sunday", "EEEE");
        dayOfWeek.put("sun", "E");
        dayOfWeek.put("saturday", "EEEE");
        dayOfWeek.put("sat", "E");
        dayOfWeek.put("friday", "EEEE");
        dayOfWeek.put("fri", "E");
        dayOfWeek.put("thursday", "EEEE");
        dayOfWeek.put("thu", "E");
        dayOfWeek.put("wednesday", "EEEE");
        dayOfWeek.put("wed", "E");
        dayOfWeek.put("tuesday", "EEEE");
        dayOfWeek.put("tue", "E");
        dayOfWeek.put("monday", "EEEE");
        dayOfWeek.put("mon", "E");
    }

    private void initMonthsMap(){
        months = Maps.newHashMap();
        months.put("january", "MMMM");
        months.put("jan", "MMM");
        months.put("february", "MMMM");
        months.put("feb", "MMM");
        months.put("march", "MMMM");
        months.put("mar", "MMM");
        months.put("april", "MMMM");
        months.put("apr", "MMM");
        months.put("may", "MMMM");
        months.put("may", "MMM");
        months.put("june", "MMMM");
        months.put("jun", "MMM");
        months.put("july", "MMMM");
        months.put("jul", "MMM");
        months.put("august", "MMMM");
        months.put("aug", "MMM");
        months.put("september", "MMMM");
        months.put("sep", "MMM");
        months.put("october", "MMMM");
        months.put("oct", "MMM");
        months.put("november", "MMMM");
        months.put("nov", "MMM");
        months.put("december", "MMMM");
        months.put("dec", "MMM");
    }
}
