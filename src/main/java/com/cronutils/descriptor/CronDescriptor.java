package com.cronutils.descriptor;

import com.cronutils.model.Cron;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import org.apache.commons.lang3.Validate;

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
     * @param cron - Cron instance, never null
     *             if null, will throw NullPointerException
     * @return description - String
     */
    public String describe(Cron cron) {
        Validate.notNull(cron, "Cron must not be null");
        Map<CronFieldName, CronField> expressions = cron.retrieveFieldsAsMap();
        return
                new StringBuilder()
                        .append(describeHHmmss(expressions)).append(" ")
                        .append(describeDayOfMonth(expressions)).append(" ")
                        .append(describeMonth(expressions)).append(" ")
                        .append(describeDayOfWeek(expressions)).append(" ")
                        .append(describeYear(expressions))
                        .toString().replaceAll("\\s+", " ").trim();
    }

    /**
     * Provide description for hours, minutes and seconds
     * @param fields - fields to describe;
     * @return description - String
     */
    private String describeHHmmss(Map<CronFieldName, CronField> fields) {
        return DescriptionStrategyFactory.hhMMssInstance(
                bundle,
                fields.containsKey(CronFieldName.HOUR) ? fields.get(CronFieldName.HOUR).getExpression() : null,
                fields.containsKey(CronFieldName.MINUTE) ? fields.get(CronFieldName.MINUTE).getExpression() : null,
                fields.containsKey(CronFieldName.SECOND) ? fields.get(CronFieldName.SECOND).getExpression() : null
        ).describe();
    }

    /**
     * Provide description for day of month
     * @param fields - fields to describe;
     * @return description - String
     */
    private String describeDayOfMonth(Map<CronFieldName, CronField> fields) {
        return String.format(
                DescriptionStrategyFactory.daysOfMonthInstance(
                        bundle,
                        fields.containsKey(CronFieldName.DAY_OF_MONTH) ? fields.get(CronFieldName.DAY_OF_MONTH).getExpression() : null
                ).describe(), bundle.getString("day"));
    }

    /**
     * Provide description for month
     * @param fields - fields to describe;
     * @return description - String
     */
    private String describeMonth(Map<CronFieldName, CronField> fields) {
        return String.format(
                DescriptionStrategyFactory.monthsInstance(
                        bundle,
                        fields.containsKey(CronFieldName.MONTH) ? fields.get(CronFieldName.MONTH).getExpression() : null
                ).describe(),
                bundle.getString("month"));
    }

    /**
     * Provide description for day of week
     * @param fields - fields to describe;
     * @return description - String
     */
    private String describeDayOfWeek(Map<CronFieldName, CronField> fields) {
        return String.format(
                DescriptionStrategyFactory.daysOfWeekInstance(
                        bundle,
                        fields.containsKey(CronFieldName.DAY_OF_WEEK) ? fields.get(CronFieldName.DAY_OF_WEEK).getExpression() : null
                ).describe(),
                bundle.getString("day"));
    }

    /**
     * Provide description for a year
     * @param fields - fields to describe;
     * @return description - String
     */
    private String describeYear(Map<CronFieldName, CronField> fields) {
        return String.format(
                DescriptionStrategyFactory.plainInstance(
                        bundle,
                        fields.containsKey(CronFieldName.YEAR) ? fields.get(CronFieldName.YEAR).getExpression() : null
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
