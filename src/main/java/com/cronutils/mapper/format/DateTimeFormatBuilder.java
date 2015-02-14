package com.cronutils.mapper.format;

import org.apache.commons.lang3.Validate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;
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
