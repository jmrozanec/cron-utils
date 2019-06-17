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

package com.cronutils.model.time.generator;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.SpecialChar;

public class FieldValueGeneratorFactory {
    private FieldValueGeneratorFactory() {
    }

    public static FieldValueGenerator forCronField(final CronField cronField) {
        final FieldExpression fieldExpression = cronField.getExpression();
        if (fieldExpression instanceof Always) {
            return new AlwaysFieldValueGenerator(cronField);
        }
        if (fieldExpression instanceof And) {
            return new AndFieldValueGenerator(cronField);
        }
        if (fieldExpression instanceof Between) {
            return new BetweenFieldValueGenerator(cronField);
        }
        if (fieldExpression instanceof Every) {
            return new EveryFieldValueGenerator(cronField);
        }
        if (fieldExpression instanceof On) {
            final On on = (On) fieldExpression;
            if (!SpecialChar.NONE.equals(on.getSpecialChar().getValue())) {
                throw new IllegalArgumentException(String.format("Cannot create instance for On instance with %s value", on.getSpecialChar()));
            }
            return new OnFieldValueGenerator(cronField);
        }
        return new NullFieldValueGenerator(cronField);
    }

    public static FieldValueGenerator createDayOfYearValueGeneratorInstance(final CronField cronField, final int year) {
        return forCronField(cronField);
    }

    public static FieldValueGenerator createDayOfMonthValueGeneratorInstance(final CronField cronField, final int year, final int month) {
        final FieldExpression fieldExpression = cronField.getExpression();
        if (fieldExpression instanceof On) {
            final On on = (On) fieldExpression;
            if (!SpecialChar.NONE.equals(on.getSpecialChar().getValue())) {
                return new OnDayOfMonthValueGenerator(cronField, year, month);
            }
        }
        return forCronField(cronField);
    }

    public static FieldValueGenerator createDayOfWeekValueGeneratorInstance(final CronField cronField, final int year, final int month, final WeekDay mondayDoWValue) {
        final FieldExpression fieldExpression = cronField.getExpression();
        if (fieldExpression instanceof On) {
            return new OnDayOfWeekValueGenerator(cronField, year, month, mondayDoWValue);
        }
        // handle a range expression for day of week special
        if (fieldExpression instanceof Between) {
            return new BetweenDayOfWeekValueGenerator(cronField, year, month, mondayDoWValue);
        }
        // handle And expression for day of the week as a special case
        if (fieldExpression instanceof And) {
            return new AndDayOfWeekValueGenerator(cronField, year, month, mondayDoWValue);
        }
        if (fieldExpression instanceof Every) {
            return new EveryDayOfWeekValueGenerator(cronField, year, month, mondayDoWValue);
        }
        return forCronField(cronField);
    }

    public static FieldValueGenerator createYearValueGeneratorInstance(final CronField cronField) {
        return forCronField(cronField);
    }
}
