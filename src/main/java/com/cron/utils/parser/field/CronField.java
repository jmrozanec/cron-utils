package com.cron.utils.parser.field;

import com.cron.utils.CronParameter;

import java.util.Comparator;

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
public class CronField {
    private CronParameter field;
    private FieldParser parser;

    private CronField(CronParameter field) {
        this.field = field;
        this.parser =
                new FieldParser()
                        .withConstraints(FieldConstraints.forField(field));
    }

    public static CronField seconds() {
        return new CronField(CronParameter.SECOND);
    }

    public static CronField minutes() {
        return new CronField(CronParameter.MINUTE);
    }

    public static CronField hours() {
        return new CronField(CronParameter.HOUR);
    }

    public static CronField daysOfWeek() {
        return new CronField(CronParameter.DAY_OF_WEEK);
    }

    public static CronField daysOfMonth() {
        return new CronField(CronParameter.DAY_OF_MONTH);
    }

    public static CronField months() {
        return new CronField(CronParameter.MONTH);
    }

    public static CronField years() {
        return new CronField(CronParameter.YEAR);
    }

    public CronParameter getField() {
        return field;
    }

    public CronFieldParseResult parse(String expression) {
        return new CronFieldParseResult(field, parser.parse(expression));
    }

    public static Comparator<CronField> createFieldTypeComparator() {
        return new Comparator<CronField>() {
            @Override
            public int compare(CronField o1, CronField o2) {
                return o1.getField().getOrder() - o2.getField().getOrder();
            }
        };
    }
}
