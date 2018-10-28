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

package com.cronutils.descriptor;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.cronutils.model.Cron;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.utils.Preconditions;

/**
 * Provides human readable description for a given cron.
 */
public class CronDescriptor {

    public static final Locale DEFAULT_LOCALE = Locale.UK;
    private static final String BUNDLE = "CronUtilsI18N";
    private final ResourceBundle resourceBundle;

    /**
     * Constructor creating a descriptor for given Locale.
     *
     * @param locale - Locale in which descriptions are given
     */
    private CronDescriptor(final Locale locale) {
        resourceBundle = ResourceBundle.getBundle(BUNDLE, locale);
    }

    /**
     * Default constructor. Considers Locale.UK as default locale
     */
    private CronDescriptor() {
        resourceBundle = ResourceBundle.getBundle(BUNDLE, DEFAULT_LOCALE);
    }

    /**
     * Provide a description of given CronFieldParseResult list.
     *
     * @param cron - Cron instance, never null
     *             if null, will throw NullPointerException
     * @return description - String
     */
    public String describe(final Cron cron) {
        Preconditions.checkNotNull(cron, "Cron must not be null");
        final Map<CronFieldName, CronField> expressions = cron.retrieveFieldsAsMap();
        final Map<CronFieldName, FieldDefinition> fieldDefinitions = cron.getCronDefinition().retrieveFieldDefinitionsAsMap();

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
     * Provide description for hours, minutes and seconds.
     *
     * @param fields - fields to describe;
     * @return description - String
     */
    public String describeHHmmss(final Map<CronFieldName, CronField> fields) {
        return DescriptionStrategyFactory.hhMMssInstance(
                resourceBundle,
                fields.containsKey(CronFieldName.HOUR) ? fields.get(CronFieldName.HOUR).getExpression() : null,
                fields.containsKey(CronFieldName.MINUTE) ? fields.get(CronFieldName.MINUTE).getExpression() : null,
                fields.containsKey(CronFieldName.SECOND) ? fields.get(CronFieldName.SECOND).getExpression() : null
        ).describe();
    }

    /**
     * Provide description for day of month.
     *
     * @param fields - fields to describe;
     * @return description - String
     */
    public String describeDayOfMonth(final Map<CronFieldName, CronField> fields) {
        final String description = DescriptionStrategyFactory.daysOfMonthInstance(
                resourceBundle,
                fields.containsKey(CronFieldName.DAY_OF_MONTH) ? fields.get(CronFieldName.DAY_OF_MONTH).getExpression() : null
        ).describe();
        return addTimeExpressions(description, resourceBundle.getString("day"), resourceBundle.getString("days"));
    }

    /**
     * Provide description for month.
     *
     * @param fields - fields to describe;
     * @return description - String
     */
    public String describeMonth(final Map<CronFieldName, CronField> fields) {
        final String description = DescriptionStrategyFactory.monthsInstance(
                resourceBundle,
                fields.containsKey(CronFieldName.MONTH) ? fields.get(CronFieldName.MONTH).getExpression() : null
        ).describe();

        return addTimeExpressions(description, resourceBundle.getString("month"), resourceBundle.getString("months"));
    }

    private String addTimeExpressions(final String description, final String singular, final String plural) {
        return description
                .replaceAll("%s", singular)
                .replaceAll("%p", plural);
    }

    /**
     * Provide description for day of week.
     *
     * @param fields - fields to describe;
     * @return description - String
     */
    public String describeDayOfWeek(final Map<CronFieldName, CronField> fields, final Map<CronFieldName, FieldDefinition> definitions) {

        final String description = DescriptionStrategyFactory.daysOfWeekInstance(
                resourceBundle,
                fields.containsKey(CronFieldName.DAY_OF_WEEK) ? fields.get(CronFieldName.DAY_OF_WEEK).getExpression() : null,
                definitions.containsKey(CronFieldName.DAY_OF_WEEK) ? definitions.get(CronFieldName.DAY_OF_WEEK) : null
        ).describe();
        return addExpressions(description, resourceBundle.getString("day"), resourceBundle.getString("days"));
    }

    private String addExpressions(final String description, final String singular, final String plural) {
        return description
                .replaceAll("%s", singular)
                .replaceAll("%p", plural);
    }

    /**
     * Provide description for a year.
     *
     * @param fields - fields to describe;
     * @return description - String
     */
    public String describeYear(final Map<CronFieldName, CronField> fields) {
        final String description =
                DescriptionStrategyFactory.plainInstance(
                        resourceBundle,
                        fields.containsKey(CronFieldName.YEAR) ? fields.get(CronFieldName.YEAR).getExpression() : null
                ).describe();
        return addExpressions(description, resourceBundle.getString("year"), resourceBundle.getString("years"));
    }

    /**
     * Creates an instance with UK locale.
     *
     * @return CronDescriptor - never null.
     */
    public static CronDescriptor instance() {
        return new CronDescriptor();
    }

    /**
     * Creates and instance with given locale.
     *
     * @param locale - Locale in which descriptions will be given
     * @return CronDescriptor - never null.
     */
    public static CronDescriptor instance(final Locale locale) {
        return new CronDescriptor(locale);
    }

    /**
     * Gets the current resource bundle that is in use to allow custom reuse of text phrases.
     *
     * @return ResourceBundle - never null.
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
}
