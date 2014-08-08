package com.cron.utils.descriptor;

import com.cron.utils.parser.field.CronFieldExpression;
import com.cron.utils.parser.field.On;
import com.google.common.base.Function;
import org.joda.time.DateTime;

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
class DescriptionStrategyFactory {
    public static DescriptionStrategy daysOfWeekInstance(final ResourceBundle bundle, final CronFieldExpression expression) {
        final Function<Integer, String> nominal = new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) {
                return new DateTime().withDayOfWeek(integer).dayOfWeek().getAsText(bundle.getLocale());
            }
        };

        NominalDescriptionStrategy dow = new NominalDescriptionStrategy(bundle, nominal, expression);

        dow.addDescription(new Function<CronFieldExpression, String>() {
            @Override
            public String apply(CronFieldExpression cronFieldExpression) {
                if (cronFieldExpression instanceof On) {
                    On on = (On) cronFieldExpression;
                    switch (on.getSpecialChar()) {
                        case HASH:
                            return String.format("%s %s %s ", nominal.apply(on.getTime()), on.getNth(), bundle.getString("of_every_month"));
                        case L:
                            return String.format("%s %s %s ", bundle.getString("last"), nominal.apply(on.getTime()), bundle.getString("of_every_month"));
                        default:
                            return "";
                    }
                }
                return "";
            }
        });
        return dow;
    }

    public static DescriptionStrategy daysOfMonthInstance(final ResourceBundle bundle, final CronFieldExpression expression) {
        NominalDescriptionStrategy dow = new NominalDescriptionStrategy(bundle, null, expression);

        dow.addDescription(new Function<CronFieldExpression, String>() {
            @Override
            public String apply(CronFieldExpression cronFieldExpression) {
                if (cronFieldExpression instanceof On) {
                    On on = (On) cronFieldExpression;
                    switch (on.getSpecialChar()) {
                        case W:
                            return String.format("%s %s %s ", bundle.getString("the_nearest_weekday_to_the"), on.getTime(), bundle.getString("of_the_month"));
                        case L:
                            return bundle.getString("last_day_of_month");
                        default:
                            return "";
                    }
                }
                return "";
            }
        });
        return dow;
    }

    public static DescriptionStrategy monthsInstance(final ResourceBundle bundle, final CronFieldExpression expression) {
        return new NominalDescriptionStrategy(
                bundle,
                new Function<Integer, String>() {
                    @Override
                    public String apply(Integer integer) {
                        return new DateTime().withMonthOfYear(integer).monthOfYear().getAsText(bundle.getLocale());
                    }
                },
                expression
        );
    }

    public static DescriptionStrategy plainInstance(ResourceBundle bundle, final CronFieldExpression expression) {
        return new NominalDescriptionStrategy(bundle, null, expression);
    }

    public static DescriptionStrategy hhMMssInstance(ResourceBundle bundle, final CronFieldExpression hours,
                                                     final CronFieldExpression minutes, final CronFieldExpression seconds) {
        return new TimeDescriptionStrategy(bundle, hours, minutes, seconds);
    }
}
