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

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import com.cronutils.Function;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.utils.Preconditions;
import com.cronutils.utils.StringUtils;

import static com.cronutils.model.field.expression.FieldExpression.always;

/**
 * Strategy to provide a human readable description to hh:mm:ss variations.
 */
class TimeDescriptionStrategy extends DescriptionStrategy {

    private final FieldExpression hours;
    private final FieldExpression minutes;
    private final FieldExpression seconds;
    private final Set<Function<TimeFields, String>> descriptions;
    private static final int DEFAULTSECONDS = 0;

    private static final String EVERY = "every";
    private static final String SECOND = "second";
    private static final String MINUTE = "minute";
    private static final String EVERY_MINUTE_FORMAT = "%s %s ";

    /**
     * Constructor.
     *
     * @param bundle  - locale considered when creating the description
     * @param hours   - CronFieldExpression for hours. If no instance is provided, an Always instance is created.
     * @param minutes - CronFieldExpression for minutes. If no instance is provided, an Always instance is created.
     * @param seconds - CronFieldExpression for seconds. If no instance is provided, an On instance is created.
     */
    TimeDescriptionStrategy(final ResourceBundle bundle, final FieldExpression hours,
            final FieldExpression minutes, final FieldExpression seconds) {
        super(bundle);
        this.hours = ensureInstance(hours, always());
        this.minutes = ensureInstance(minutes, always());
        this.seconds = ensureInstance(seconds, new On(new IntegerFieldValue(DEFAULTSECONDS)));
        descriptions = new HashSet<>();
        registerFunctions();
    }

    /**
     * Give an expression instance, will return it if is not null. Otherwise will return the defaultExpression;
     *
     * @param expression        - CronFieldExpression instance; may be null
     * @param defaultExpression - CronFieldExpression, never null;
     * @return the given expression or the given defaultExpression in case the given expression is {@code null}
     */
    private FieldExpression ensureInstance(final FieldExpression expression, final FieldExpression defaultExpression) {
        Preconditions.checkNotNull(defaultExpression, "Default expression must not be null");
        if (expression != null) {
            return expression;
        } else {
            return defaultExpression;
        }
    }

    @Override
    public String describe() {
        final TimeFields fields = new TimeFields(hours, minutes, seconds);
        for (final Function<TimeFields, String> function : descriptions) {
            if (!"".equals(function.apply(fields))) {
                return function.apply(fields);
            }
        }
        String secondsDesc = "";
        String minutesDesc = "";
        final String hoursDesc = addTimeExpressions(describe(hours), bundle.getString("hour"), bundle.getString("hours"));
        if (!(seconds instanceof On && isDefault((On) seconds))) {
            secondsDesc = addTimeExpressions(describe(seconds), bundle.getString(SECOND), bundle.getString("seconds"));
        }
        if (!(minutes instanceof On && isDefault((On) minutes))) {
            minutesDesc = addTimeExpressions(describe(minutes), bundle.getString(MINUTE), bundle.getString("minutes"));
        }
        return String.format("%s %s %s", secondsDesc, minutesDesc, hoursDesc);
    }

    private String addTimeExpressions(final String description, final String singular, final String plural) {
        return description
                .replaceAll("%s", singular)
                .replaceAll("replace_plural", plural);
    }

    /**
     * Registers functions that map TimeFields to a human readable description.
     */
    private void registerFunctions() {
        // case: every second
        // case: every minute at x second
        descriptions.add(timeFields -> {
            if (timeFields.hours instanceof Always && timeFields.minutes instanceof Always) {
                if (timeFields.seconds instanceof Always) {
                    return String.format(EVERY_MINUTE_FORMAT, bundle.getString(EVERY), bundle.getString(SECOND));
                }
                if (timeFields.seconds instanceof On) {
                    if (TimeDescriptionStrategy.this.isDefault((On) timeFields.seconds)) {
                        return String.format(EVERY_MINUTE_FORMAT, bundle.getString(EVERY), bundle.getString(MINUTE));
                    } else {
                        return String.format("%s %s %s %s %02d", bundle.getString(EVERY), bundle.getString(MINUTE), bundle.getString("at"),
                                bundle.getString(SECOND), ((On) timeFields.seconds).getTime().getValue());
                    }
                }
            }
            return StringUtils.EMPTY;
        });

        // case: At minute x
        descriptions.add(timeFields -> {
            if (timeFields.hours instanceof Always && timeFields.minutes instanceof On && timeFields.seconds instanceof On) {
                if (TimeDescriptionStrategy.this.isDefault((On) timeFields.seconds)) {
                    if (TimeDescriptionStrategy.this.isDefault((On) timeFields.minutes)) {
                        return String.format(EVERY_MINUTE_FORMAT, bundle.getString(EVERY), bundle.getString("hour"));
                    }
                    return String.format("%s %s %s %s %s", bundle.getString(EVERY), bundle.getString("hour"), bundle.getString("at"),
                            bundle.getString(MINUTE), ((On) timeFields.minutes).getTime().getValue());
                } else {
                    return String.format("%s %s %s %s %s %s %s %s", bundle.getString(EVERY), bundle.getString("hour"), bundle.getString("at"),
                            bundle.getString(MINUTE), ((On) timeFields.minutes).getTime().getValue(), bundle.getString("and"), bundle.getString(SECOND),
                            ((On) timeFields.seconds).getTime().getValue());
                }
            }
            return StringUtils.EMPTY;
        });

        // case: 11:45
        descriptions.add(timeFields -> {
            if (timeFields.hours instanceof On && timeFields.minutes instanceof On && timeFields.seconds instanceof Always) {
                return String.format("%s %s %s %02d:%02d", bundle.getString(EVERY), bundle.getString(SECOND), bundle.getString("at"),
                        ((On) hours).getTime().getValue(), ((On) minutes).getTime().getValue());
            }
            return StringUtils.EMPTY;
        });

        // case: 11:30:45
        // case: 11:30:00 -> 11:30
        descriptions.add(timeFields -> {
            if (timeFields.hours instanceof On && timeFields.minutes instanceof On && timeFields.seconds instanceof On) {
                if (TimeDescriptionStrategy.this.isDefault((On) timeFields.seconds)) {
                    return String.format("%s %02d:%02d", bundle.getString("at"), ((On) hours).getTime().getValue(), ((On) minutes).getTime().getValue());
                } else {
                    return String.format("%s %02d:%02d:%02d", bundle.getString("at"), ((On) hours).getTime().getValue(), ((On) minutes).getTime().getValue(),
                            ((On) seconds).getTime().getValue());
                }
            }
            return StringUtils.EMPTY;
        });

        // 11 -> 11:00
        descriptions.add(timeFields -> {
            if (timeFields.hours instanceof On && timeFields.minutes instanceof Always && timeFields.seconds instanceof Always) {
                return String.format("%s %02d:00", bundle.getString("at"), ((On) hours).getTime().getValue());
            }
            return StringUtils.EMPTY;
        });

        // case: every minute between 11:00 and 11:10
        // case: every second between 11:00 and 11:10
        descriptions.add(timeFields -> {
            if (timeFields.hours instanceof On && timeFields.minutes instanceof Between) {
                if (timeFields.seconds instanceof On) {
                    return String.format("%s %s %s %02d:%02d %s %02d:%02d", bundle.getString(EVERY), bundle.getString(MINUTE), bundle.getString("between"),
                            ((On) timeFields.hours).getTime().getValue(), ((Between) timeFields.minutes).getFrom().getValue(), bundle.getString("and"),
                            ((On) timeFields.hours).getTime().getValue(), ((Between) timeFields.minutes).getTo().getValue());
                }
                if (timeFields.seconds instanceof Always) {
                    return String.format("%s %s %s %02d:%02d %s %02d:%02d", bundle.getString(EVERY), bundle.getString(SECOND), bundle.getString("between"),
                            ((On) timeFields.hours).getTime().getValue(), ((Between) timeFields.minutes).getFrom().getValue(), bundle.getString("and"),
                            ((On) timeFields.hours).getTime().getValue(), ((Between) timeFields.minutes).getTo().getValue());
                }
            }
            return StringUtils.EMPTY;
        });

        // case: every x minutes
        descriptions.add(timeFields -> {
            if (timeFields.hours instanceof Always && timeFields.minutes instanceof Every && timeFields.seconds instanceof On) {
                final Every minute = (Every) timeFields.minutes;
                String desc;
                if (minute.getPeriod().getValue() == 1 && TimeDescriptionStrategy.this.isDefault((On) timeFields.seconds)) {
                    desc = String.format(EVERY_MINUTE_FORMAT, bundle.getString(EVERY), bundle.getString(MINUTE));
                } else {
                    desc = String.format("%s %s %s ", bundle.getString(EVERY), minute.getPeriod().getValue(), bundle.getString("minutes"));
                }
                if (minute.getExpression() instanceof Between) {
                    return StringUtils.EMPTY;
                }
                return desc;
            }
            return StringUtils.EMPTY;
        });

        // case: every x hours
        descriptions.add(timeFields -> {
            if (timeFields.hours instanceof Every && timeFields.minutes instanceof On && timeFields.seconds instanceof On) {
                // every hour
                if (((On) timeFields.minutes).getTime().getValue() == 0 && ((On) timeFields.seconds).getTime().getValue() == 0) {
                    final Integer period = ((Every) timeFields.hours).getPeriod().getValue();
                    if (period == null || period == 1) {
                        return String.format(EVERY_MINUTE_FORMAT, bundle.getString(EVERY), bundle.getString("hour"));
                    }
                }
                final String result = String.format("%s %s %s %s %s %s ", bundle.getString(EVERY), ((Every) hours).getPeriod().getValue(),
                        bundle.getString("hours"), bundle.getString("at"), bundle.getString(MINUTE), ((On) minutes).getTime().getValue());
                if (TimeDescriptionStrategy.this.isDefault((On) timeFields.seconds)) {
                    return result;
                } else {
                    return String.format("%s %s %s", bundle.getString("and"), bundle.getString(SECOND), ((On) seconds).getTime().getValue());
                }
            }
            return StringUtils.EMPTY;
        });
    }

    /**
     * Contains CronFieldExpression instances for hours, minutes and seconds.
     */
    class TimeFields {
        private final FieldExpression seconds;
        private final FieldExpression minutes;
        private final FieldExpression hours;

        public TimeFields(final FieldExpression hours, final FieldExpression minutes, final FieldExpression seconds) {
            this.hours = hours;
            this.minutes = minutes;
            this.seconds = seconds;
        }
    }

    /**
     * Checks if On instance has a default value.
     *
     * @param on - On instance
     * @return boolean - true if time value matches a default; false otherwise.
     */
    private boolean isDefault(final On on) {
        return on.getTime().getValue() == DEFAULTSECONDS;
    }
}
