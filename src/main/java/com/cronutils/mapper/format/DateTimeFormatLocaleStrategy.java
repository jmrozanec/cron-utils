package com.cronutils.mapper.format;

import org.joda.time.DateTimeZone;

import java.util.Map;

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
