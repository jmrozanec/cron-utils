package com.cron.utils.descriptor;

import com.cron.utils.CronFieldName;
import com.cron.utils.parser.field.*;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.Validate;

import java.util.ResourceBundle;
import java.util.Set;

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
 * Strategy to provide a human readable description to hh:mm:ss variations
 */
class TimeDescriptionStrategy extends DescriptionStrategy {

    private CronFieldExpression hours;
    private CronFieldExpression minutes;
    private CronFieldExpression seconds;
    private Set<Function<TimeFields, String>> descriptions;
    private int defaultSeconds = 0;

    /**
     * Constructor
     * @param bundle - locale considered when creating the description
     * @param hours - CronFieldExpression for hours. If no instance is provided, an Always instance is created.
     * @param minutes - CronFieldExpression for minutes. If no instance is provided, an Always instance is created.
     * @param seconds - CronFieldExpression for seconds. If no instance is provided, an On instance is created.
     */
    TimeDescriptionStrategy(ResourceBundle bundle, CronFieldExpression hours,
                            CronFieldExpression minutes, CronFieldExpression seconds) {
        super(bundle);
        this.hours = ensureInstance(hours,
                new Always(
                        FieldConstraintsBuilder.instance()
                                .forField(CronFieldName.HOUR).createConstraintsInstance()
                )
        );
        this.minutes = ensureInstance(minutes,
                new Always(
                        FieldConstraintsBuilder.instance()
                                .forField(CronFieldName.MINUTE).createConstraintsInstance()
                )
        );
        this.seconds = ensureInstance(seconds,
                new On(
                        FieldConstraintsBuilder.instance()
                                .forField(CronFieldName.SECOND).createConstraintsInstance(),
                        "" + defaultSeconds)
        );
        descriptions = Sets.newHashSet();
        registerFunctions();
    }

    /**
     * Give an expression instance, will return it if is not null. Otherwise will return the defaultExpression;
     * @param expression - CronFieldExpression instance; may be null
     * @param defaultExpression - CronFieldExpression, never null;
     * @return
     */
    private CronFieldExpression ensureInstance(CronFieldExpression expression, CronFieldExpression defaultExpression) {
        Validate.notNull(defaultExpression, "Default expression must not be null");
        if (expression != null) {
            return expression;
        } else {
            return defaultExpression;
        }
    }

    @Override
    public String describe() {
        TimeFields fields = new TimeFields(hours, minutes, seconds);
        for (Function<TimeFields, String> function : descriptions) {
            if (!"".equals(function.apply(fields))) {
                return function.apply(fields);
            }
        }

        return describe(seconds).replaceAll("%s", bundle.getString("seconds")) + " " +
                describe(minutes).replaceAll("%s", bundle.getString("minutes")) + " " +
                describe(hours).replaceAll("%s", bundle.getString("hours"));
    }

    /**
     * Registers functions that map TimeFields to a human readable description.
     */
    private void registerFunctions() {
        //case: every second
        //case: every minute at x second
        descriptions.add(
                new Function<TimeFields, String>() {
                    @Override
                    public String apply(TimeFields timeFields) {
                        if (timeFields.hours instanceof Always &&
                                timeFields.minutes instanceof Always) {
                            if (timeFields.seconds instanceof Always) {
                                return String.format("%s %s ", bundle.getString("every"), bundle.getString("second"));
                            }
                            if (timeFields.seconds instanceof On) {
                                if (isDefault((On) timeFields.seconds)) {
                                    return String.format("%s %s ", bundle.getString("every"), bundle.getString("minute"));
                                } else {
                                    return String.format("%s %s %s %s %02d", bundle.getString("every"), bundle.getString("minute"),
                                            bundle.getString("at"), bundle.getString("second"), ((On) timeFields.seconds).getTime());
                                }
                            }
                        }
                        return "";
                    }
                });

        //case: At minute x
        descriptions.add(
                new Function<TimeFields, String>() {
                    @Override
                    public String apply(TimeFields timeFields) {
                        if (timeFields.hours instanceof Always &&
                                timeFields.minutes instanceof On &&
                                timeFields.seconds instanceof On) {
                                if (isDefault((On) timeFields.seconds)) {
                                    return String.format("%s %s %s %s %s", bundle.getString("every"),
                                            bundle.getString("hour"), bundle.getString("at"),
                                            bundle.getString("minute"), ((On) timeFields.minutes).getTime());
                                } else {
                                    return String.format("%s %s %s %s %s %s %s %s", bundle.getString("every"),
                                            bundle.getString("hour"), bundle.getString("at"),
                                            bundle.getString("minute"), ((On) timeFields.minutes).getTime(),
                                            bundle.getString("and"), bundle.getString("second"),
                                            ((On) timeFields.seconds).getTime());
                                }
                            }
                        return "";
                    }
                });

        //case: 11:45
        descriptions.add(
                new Function<TimeFields, String>() {
                    @Override
                    public String apply(TimeFields timeFields) {
                        if (timeFields.hours instanceof On &&
                                timeFields.minutes instanceof On
                                && timeFields.seconds instanceof Always) {
                            return String.format("%s %s %s %02d:%02d", bundle.getString("every"),
                                    bundle.getString("second"), bundle.getString("at"),
                                    ((On) hours).getTime(), ((On) minutes).getTime());
                        }
                        return "";
                    }
                });

        //case: 11:30:45
        //case: 11:30:00 -> 11:30
        descriptions.add(
                new Function<TimeFields, String>() {
                    @Override
                    public String apply(TimeFields timeFields) {
                        if (timeFields.hours instanceof On &&
                                timeFields.minutes instanceof On
                                && timeFields.seconds instanceof On) {
                            if (isDefault((On) timeFields.seconds)) {
                                return String.format("%s %02d:%02d", bundle.getString("at"), ((On) hours).getTime(),
                                        ((On) minutes).getTime());
                            } else {
                                return String.format("%s %02d:%02d:%02d", bundle.getString("at"), ((On) hours).getTime(),
                                        ((On) minutes).getTime(), ((On) seconds).getTime());
                            }
                        }
                        return "";
                    }
                });

        //11 -> 11:00
        descriptions.add(
                new Function<TimeFields, String>() {
                    @Override
                    public String apply(TimeFields timeFields) {
                        if (timeFields.hours instanceof On &&
                                timeFields.minutes instanceof Always &&
                                timeFields.seconds instanceof Always) {
                            return String.format("%s %02d:00", bundle.getString("at"), ((On) hours).getTime());
                        }
                        return "";
                    }
                });

        //case: every minute between 11:00 and 11:10
        //case: every second between 11:00 and 11:10
        descriptions.add(
                new Function<TimeFields, String>() {
                    @Override
                    public String apply(TimeFields timeFields) {
                        if (timeFields.minutes instanceof Between &&
                                timeFields.hours instanceof On) {
                            if (timeFields.seconds instanceof On) {
                                return String.format("%s %s %s %02d:%02d %s %02d:%02d",
                                        bundle.getString("every"),
                                        bundle.getString("minute"),
                                        bundle.getString("between"),
                                        ((On) timeFields.hours).getTime(), ((Between) timeFields.minutes).getFrom(),
                                        bundle.getString("and"),
                                        ((On) timeFields.hours).getTime(), ((Between) timeFields.minutes).getTo());
                            }
                            if (timeFields.seconds instanceof Always) {
                                return String.format("%s %s %s %02d:%02d %s %02d:%02d",
                                        bundle.getString("every"),
                                        bundle.getString("second"),
                                        bundle.getString("between"),
                                        ((On) timeFields.hours).getTime(), ((Between) timeFields.minutes).getFrom(),
                                        bundle.getString("and"),
                                        ((On) timeFields.hours).getTime(), ((Between) timeFields.minutes).getTo());
                            }
                        }
                        return "";
                    }
                });

        //case: every x minutes
        descriptions.add(
                new Function<TimeFields, String>() {
                    @Override
                    public String apply(TimeFields timeFields) {
                        if (timeFields.hours instanceof Always &&
                                timeFields.minutes instanceof Every &&
                                timeFields.seconds instanceof On) {
                            if (isDefault((On) timeFields.seconds)) {
                                return String.format("%s %s %s ", bundle.getString("every"), ((Every) minutes).getTime(), bundle.getString("minutes"));
                            }
                        }
                        return "";
                    }
                });

        //case: every x hours
        descriptions.add(
                new Function<TimeFields, String>() {
                    @Override
                    public String apply(TimeFields timeFields) {
                        if (timeFields.hours instanceof Every &&
                                timeFields.minutes instanceof On &&
                                timeFields.seconds instanceof On) {
                            String result = String.format("%s %s %s %s %s %s ",
                                    bundle.getString("every"), ((Every) hours).getTime(), bundle.getString("hours"),
                                    bundle.getString("at"), bundle.getString("minute"), ((On) minutes).getTime());
                            if (isDefault((On) timeFields.seconds)) {
                                return result;
                            } else {
                                return String.format("%s %s %s", bundle.getString("and"),
                                        bundle.getString("second"), ((On) seconds).getTime());
                            }
                        }
                        return "";
                    }
                });
    }

    /**
     * Contains CronFieldExpression instances for hours, minutes and seconds.
     */
    class TimeFields {
        public CronFieldExpression seconds;
        public CronFieldExpression minutes;
        public CronFieldExpression hours;

        public TimeFields(CronFieldExpression hours, CronFieldExpression minutes, CronFieldExpression seconds) {
            this.hours = hours;
            this.minutes = minutes;
            this.seconds = seconds;
        }
    }

    /**
     * Checks if On instance has a default value.
     * @param on - On instance
     * @return boolean - true if time value matches a default; false otherwise.
     */
    private boolean isDefault(On on) {
        return on.getTime() == defaultSeconds;
    }
}
