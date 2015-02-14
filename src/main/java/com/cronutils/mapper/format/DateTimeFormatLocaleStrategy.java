package com.cronutils.mapper.format;

import org.joda.time.DateTimeZone;

import java.util.Map;
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
public abstract class DateTimeFormatLocaleStrategy {
    protected Map<String, String> dayOfWeek;
    protected Map<String, String> months;

    public String retrievePattern(String expression){
        if(isTimezone(expression.replaceAll("[^A-Za-z0-9/_:\\-\\+ ]", ""))){
            return timezonePattern(expression);
        }
        expression = expression.toLowerCase();
        if(expression.contains("/")){
            return parseDateSlashes(expression);
        }
        if(expression.contains(":")){
            return parseTimeWithColons(expression);
        }

        String clean = expression.replaceAll("[^A-Za-z0-9 ]", "");
        if(isNumberPattern(clean)){
            return expression.replace(clean, numberPattern(clean));
        }
        if(dayOfWeek.containsKey(clean)){
            return expression.replace(clean, dayOfWeek.get(clean));
        }
        if(months.containsKey(clean)){
            return expression.replace(clean, months.get(clean));
        }

        return expression;
    }

    protected abstract String parseDateSlashes(String expression);

    private String parseTimeWithColons(String expression){
        String hour = "HH";
        String pattern = "%s";
        if(expression.contains("am") || expression.contains("pm")){
            hour = "hh";
            pattern = "%s a";
        }
        String [] parts = expression.split(":");
        if(parts.length==2){
            return String.format(pattern, String.format("%s:mm", hour));
        }
        return String.format(pattern, String.format("%s:mm:ss", hour));
    }

    private boolean isNumberPattern(String string){
        try{
            Integer.parseInt(string);
        }catch (NumberFormatException e){
            return false;
        }
        return true;
    }

    private String numberPattern(String string){
        switch (string.length()){
            case 4:
                return "YYYY";
            case 2:
                return "dd";
            default:
                return "d";
        }
    }

    private boolean isTimezone(String string){
        try{
            DateTimeZone.forID(string);
        }catch (IllegalArgumentException e){
            return false;
        }
        return true;
    }

    private String timezonePattern(String string){
        return "Z";
    }
}
