package com.cron.utils.descriptor;

import com.cron.utils.CronFieldName;
import com.cron.utils.parser.field.CronFieldExpression;
import com.cron.utils.parser.field.CronFieldParseResult;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

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

/**
 * Provides human readable description for a given cron
 */
public class CronDescriptor {

    public static final Locale DEFAULT_LOCALE = Locale.UK;
    private static final String BUNDLE = "CronUtilsI18N";
    private ResourceBundle bundle;

    /**
     * Constructor creating a descriptor for given Locale
     * @param locale - Locale in which descriptions are given
     */
    private CronDescriptor(Locale locale) {
        bundle = ResourceBundle.getBundle(BUNDLE, locale);
    }

    /**
     * Default constructor. Considers Locale.UK as default locale
     */
    private CronDescriptor() {
        bundle = ResourceBundle.getBundle(BUNDLE, DEFAULT_LOCALE);
    }

    /**
     * Provide a description of given CronFieldParseResult list
     * @param fields - CronFieldParseResult list
     * @return description - String
     */
    public String describe(List<CronFieldParseResult> fields) {
        Map<CronFieldName, CronFieldExpression> expressions = Maps.newHashMap();
        for (CronFieldParseResult result : fields) {
            expressions.put(result.getField(), result.getExpression());
        }
        return
                new StringBuilder().append(describeHHmmss(expressions)).append(" ")
                        .append(describeDayOfMonth(expressions)).append(" ")
                        .append(describeMonth(expressions)).append(" ")
                        .append(describeDayOfWeek(expressions)).append(" ")
                        .append(describeYear(expressions))
                        .toString().replaceAll("\\s+", " ").trim();

    }

    /**
     * Provide description for hours, minutes and seconds
     * @param expressions - expressions to describe;
     * @return description - String
     */
    private String describeHHmmss(Map<CronFieldName, CronFieldExpression> expressions) {
        return DescriptionStrategyFactory.hhMMssInstance(
                bundle,
                expressions.get(CronFieldName.HOUR),
                expressions.get(CronFieldName.MINUTE),
                expressions.get(CronFieldName.SECOND)
        ).describe();
    }

    /**
     * Provide description for day of month
     * @param expressions - expressions to describe;
     * @return description - String
     */
    private String describeDayOfMonth(Map<CronFieldName, CronFieldExpression> expressions) {
        return String.format(
                DescriptionStrategyFactory.daysOfMonthInstance(
                        bundle,
                        expressions.get(CronFieldName.DAY_OF_MONTH)
                ).describe(), bundle.getString("day"));
    }

    /**
     * Provide description for month
     * @param expressions - expressions to describe;
     * @return description - String
     */
    private String describeMonth(Map<CronFieldName, CronFieldExpression> expressions) {
        return String.format(
                DescriptionStrategyFactory.monthsInstance(
                        bundle,
                        expressions.get(CronFieldName.MONTH)
                ).describe(),
                bundle.getString("month"));
    }

    /**
     * Provide description for day of week
     * @param expressions - expressions to describe;
     * @return description - String
     */
    private String describeDayOfWeek(Map<CronFieldName, CronFieldExpression> expressions) {
        return String.format(
                DescriptionStrategyFactory.daysOfWeekInstance(
                        bundle,
                        expressions.get(CronFieldName.DAY_OF_WEEK)
                ).describe(),
                bundle.getString("day"));
    }

    /**
     * Provide description for a year
     * @param expressions - expressions to describe;
     * @return description - String
     */
    private String describeYear(Map<CronFieldName, CronFieldExpression> expressions) {
        return String.format(
                DescriptionStrategyFactory.plainInstance(
                        bundle,
                        expressions.get(CronFieldName.YEAR)
                ).describe(),
                bundle.getString("year"));
    }

    /**
     * Creates an instance with UK locale
     * @return CronDescriptor - never null.
     */
    public static CronDescriptor instance() {
        return new CronDescriptor();
    }

    /**
     * Creates and instance with given locale
     * @param locale - Locale in which descriptions will be given
     * @return CronDescriptor - never null.
     */
    public static CronDescriptor instance(Locale locale) {
        return new CronDescriptor(locale);
    }
}
