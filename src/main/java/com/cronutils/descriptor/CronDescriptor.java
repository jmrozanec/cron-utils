package com.cronutils.descriptor;

import com.cronutils.model.Cron;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.utils.Preconditions;

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
        Preconditions.checkNotNull(cron, "Cron must not be null");
        Map<CronFieldName, CronField> expressions = cron.retrieveFieldsAsMap();
        Map<CronFieldName, FieldDefinition> fieldDefinitions = cron.getCronDefinition().retrieveFieldDefinitionsAsMap();
        
        return
                new StringBuilder()
                        .append(describeHHmmss(expressions)).append(" ")
                        .append(describeDayOfMonth(expressions)).append(" ")
                        .append(describeMonth(expressions)).append(" ")
                        .append(describeDayOfWeek(expressions, fieldDefinitions)).append(" ")
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
        String description = DescriptionStrategyFactory.daysOfMonthInstance(
                bundle,
                fields.containsKey(CronFieldName.DAY_OF_MONTH) ? fields.get(CronFieldName.DAY_OF_MONTH).getExpression() : null
        ).describe();
        return addTimeExpressions(description, bundle.getString("day"), bundle.getString("days"));
    }

    /**
     * Provide description for month
     * @param fields - fields to describe;
     * @return description - String
     */
    private String describeMonth(Map<CronFieldName, CronField> fields) {
        String description = DescriptionStrategyFactory.monthsInstance(
                bundle,
                fields.containsKey(CronFieldName.MONTH) ? fields.get(CronFieldName.MONTH).getExpression() : null
        ).describe();

        return addTimeExpressions(description, bundle.getString("month"), bundle.getString("months"));
    }

    private String addTimeExpressions(String description, String singular, String plural){
        return description
                .replaceAll("%s", singular)
                .replaceAll("%p", plural);
    }

    /**
     * Provide description for day of week
     * @param fields - fields to describe;
     * @return description - String
     */
    private String describeDayOfWeek(Map<CronFieldName, CronField> fields, Map<CronFieldName, FieldDefinition> definitions) {
    	
        String description = DescriptionStrategyFactory.daysOfWeekInstance(
		        bundle,
		        fields.containsKey(CronFieldName.DAY_OF_WEEK) ? fields.get(CronFieldName.DAY_OF_WEEK).getExpression() : null,
		        definitions.containsKey(CronFieldName.DAY_OF_WEEK) ? definitions.get(CronFieldName.DAY_OF_WEEK) : null 
		).describe();
		return this.addExpressions(description, bundle.getString("day"), bundle.getString("days"));
    }

    private String addExpressions(String description, String singular, String plural){
        return description
                .replaceAll("%s", singular)
                .replaceAll("%p", plural);
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
