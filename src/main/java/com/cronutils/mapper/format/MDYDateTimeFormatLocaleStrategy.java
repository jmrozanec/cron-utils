package com.cronutils.mapper.format;

import com.google.common.collect.Maps;

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
