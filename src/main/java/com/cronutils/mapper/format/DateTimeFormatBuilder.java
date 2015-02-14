package com.cronutils.mapper.format;

import org.apache.commons.lang3.Validate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

public class DateTimeFormatBuilder {

    private Locale locale;

    DateTimeFormatBuilder(){
        locale = Locale.US;
    }

    public DateTimeFormatBuilder usingLocale(Locale locale){
        this.locale = locale;
        return this;
    }

    public DateTimeFormatter createPatternFor(String expression){
        DateTimeFormatLocaleStrategy localeStrategy = createLocaleStrategyInstance();
        Validate.notBlank(expression);
        expression = expression.replaceAll("\\s+", " ");
        expression = expression.replace(" AM", "AM").replace(" am", "am").replace(" PM", "PM").replace(" pm", "pm");
        String[]parts = expression.split(" ");
        StringBuilder builder = new StringBuilder();
        for(String part : parts){
            builder.append(String.format("%s ", localeStrategy.retrievePattern(part)));
        }
        return DateTimeFormat.forPattern(builder.toString().trim());
    }

    private DateTimeFormatLocaleStrategy createLocaleStrategyInstance(){
        return new MDYDateTimeFormatLocaleStrategy();
    }
}
