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

import com.cronutils.Function;
import com.cronutils.model.field.definition.DayOfWeekFieldDefinition;
import com.cronutils.model.field.definition.FieldDefinition;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.SpecialChar;

import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

class DescriptionStrategyFactory {

    private DescriptionStrategyFactory() {
    }

    /**
     * Names the position an nth day of week occupies, falling back to the plain number for
     * positions no bundle spells out.
     */
    private static String ordinal(final int nth, final ResourceBundle bundle) {
        final String key = "nth_" + nth;
        return bundle.containsKey(key) ? bundle.getString(key) : String.valueOf(nth);
    }

    /**
     * Joins a list of plain On values as "a, b and c", so a list can reuse the same phrasing
     * as the single value case.
     *
     * @return the joined values, or null if the expression is not a list of at least two plain On values
     */
    private static String joinPlainValues(final FieldExpression expression, final Function<Integer, String> nominal, final ResourceBundle bundle) {
        if (!(expression instanceof And)) {
            return null;
        }
        final List<String> values = new ArrayList<>();
        for (final FieldExpression each : ((And) expression).getExpressions()) {
            if (!(each instanceof On) || ((On) each).getSpecialChar().getValue() != SpecialChar.NONE) {
                return null;
            }
            values.add(nominal.apply(((On) each).getTime().getValue()));
        }
        if (values.size() < 2) {
            return null;
        }
        final String last = values.remove(values.size() - 1);
        return String.join(", ", values) + " " + bundle.getString("and") + " " + last;
    }

    private static boolean isPlainOn(final FieldExpression expression) {
        return expression instanceof On && ((On) expression).getSpecialChar().getValue() == SpecialChar.NONE;
    }

    /**
     * Creates description strategy for days of week.
     *
     * @param bundle     - locale
     * @param expression - CronFieldExpression
     * @return - DescriptionStrategy instance, never null
     */
    public static DescriptionStrategy daysOfWeekInstance(final ResourceBundle bundle, final FieldExpression expression, final FieldDefinition definition) {

        final Function<Integer, String> nominal = integer -> {
            final int diff = definition instanceof DayOfWeekFieldDefinition
                    ? DayOfWeek.MONDAY.getValue() - ((DayOfWeekFieldDefinition) definition).getMondayDoWValue().getMondayDoWValue()
                    : 0;
            return DayOfWeek.of(integer + diff < 1 ? 7 : integer + diff).getDisplayName(TextStyle.FULL, bundle.getLocale());
        };

        final NominalDescriptionStrategy dow = new NominalDescriptionStrategy(bundle, nominal, expression).withSelfDescribingValues();

        dow.addDescription(fieldExpression -> {
            if (fieldExpression instanceof On) {
                final On on = (On) fieldExpression;
                switch (on.getSpecialChar().getValue()) {
                    case HASH:
                        return MessageFormat.format(bundle.getString("on_nth_day_of_week_x"),
                                ordinal(on.getNth().getValue(), bundle), nominal.apply(on.getTime().getValue()));
                    case L:
                        return MessageFormat.format(bundle.getString("on_last_day_of_week_x"), nominal.apply(on.getTime().getValue()));
                    case NONE:
                        return MessageFormat.format(bundle.getString("on_day_of_week_x"), nominal.apply(on.getTime().getValue()));
                    default:
                        return "";
                }
            }
            final String joined = joinPlainValues(fieldExpression, nominal, bundle);
            return joined == null ? "" : MessageFormat.format(bundle.getString("on_day_of_week_x"), joined);
        });
        return dow;
    }

    /**
     * Creates description strategy for days of month.
     *
     * @param bundle     - locale
     * @param expression - CronFieldExpression
     * @return - DescriptionStrategy instance, never null
     */
    public static DescriptionStrategy daysOfMonthInstance(final ResourceBundle bundle, final FieldExpression expression) {
        final NominalDescriptionStrategy dom = new NominalDescriptionStrategy(bundle, null, expression);

        dom.addDescription(fieldExpression -> {
            if (fieldExpression instanceof On) {
                final On on = (On) fieldExpression;
                switch (on.getSpecialChar().getValue()) {
                    case W:
                        return String
                                .format("%s %s %s ", bundle.getString("the_nearest_weekday_to_the"), on.getTime().getValue(), bundle.getString("of_the_month"));
                    case L:
                        Integer daysBefore = on.getNth().getValue();
                        if (daysBefore > 1) {
                            return MessageFormat.format(bundle.getString("days_before_last_day_of_month"), daysBefore);
                        } else if (daysBefore == 1){
                            return bundle.getString("day_before_last_day_of_month");
                        } else {
                            return bundle.getString("last_day_of_month");
                        }
                    case LW:
                        return bundle.getString("last_weekday_of_month");
                    case NONE:
                        return MessageFormat.format(bundle.getString("on_day_x"), on.getTime().getValue());
                    default:
                        return "";
                }
            }
            final String joined = joinPlainValues(fieldExpression, Object::toString, bundle);
            return joined == null ? "" : MessageFormat.format(bundle.getString("on_days_x"), joined);
        });
        return dom;
    }

    /**
     * Creates description strategy for months.
     *
     * @param bundle     - locale
     * @param expression - CronFieldExpression
     * @return - DescriptionStrategy instance, never null
     */
    public static DescriptionStrategy monthsInstance(final ResourceBundle bundle, final FieldExpression expression) {
        final Function<Integer, String> nominal = integer -> Month.of(integer).getDisplayName(TextStyle.FULL, bundle.getLocale());
        final NominalDescriptionStrategy months =
                new NominalDescriptionStrategy(bundle, nominal, expression).withSelfDescribingValues();

        months.addDescription(fieldExpression -> {
            if (isPlainOn(fieldExpression)) {
                return MessageFormat.format(bundle.getString("in_month_x"), nominal.apply(((On) fieldExpression).getTime().getValue()));
            }
            final String joined = joinPlainValues(fieldExpression, nominal, bundle);
            return joined == null ? "" : MessageFormat.format(bundle.getString("in_month_x"), joined);
        });
        return months;
    }

    /**
     * Creates description strategy for years.
     *
     * @param bundle     - locale
     * @param expression - CronFieldExpression
     * @return - DescriptionStrategy instance, never null
     */
    public static DescriptionStrategy yearsInstance(final ResourceBundle bundle, final FieldExpression expression) {
        final NominalDescriptionStrategy years =
                new NominalDescriptionStrategy(bundle, null, expression).withSelfDescribingValues();

        years.addDescription(fieldExpression -> {
            // as Strings, so MessageFormat does not group four digit years into "2,005"
            if (isPlainOn(fieldExpression)) {
                return MessageFormat.format(bundle.getString("in_year_x"), String.valueOf(((On) fieldExpression).getTime().getValue()));
            }
            final String joined = joinPlainValues(fieldExpression, String::valueOf, bundle);
            return joined == null ? "" : MessageFormat.format(bundle.getString("in_year_x"), joined);
        });
        return years;
    }

    /**
     * Creates nominal description strategy.
     *
     * @param bundle     - locale
     * @param expression - CronFieldExpression
     * @return - DescriptionStrategy instance, never null
     */
    public static DescriptionStrategy plainInstance(final ResourceBundle bundle, final FieldExpression expression) {
        return new NominalDescriptionStrategy(bundle, null, expression);
    }

    /**
     * Creates description strategy for hh:mm:ss.
     *
     * @param bundle - locale
     * @return - DescriptionStrategy instance, never null
     */
    public static DescriptionStrategy hhMMssInstance(final ResourceBundle bundle, final FieldExpression hours,
            final FieldExpression minutes, final FieldExpression seconds) {
        return new TimeDescriptionStrategy(bundle, hours, minutes, seconds);
    }
}
