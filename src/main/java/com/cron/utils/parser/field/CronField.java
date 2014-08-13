package com.cron.utils.parser.field;

import com.cron.utils.CronFieldName;
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;

import javax.print.attribute.standard.DateTimeAtCompleted;
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

/**
 * Represents a cron field.
 */
public class CronField {
    private CronFieldName field;
    private FieldParser parser;

    /**
     * Constructor
     * @param fieldName - CronFieldName instance
     * @param constraints - FieldConstraints instance, not null.
     *                    If null, a NullPointerException will be raised.
     */
    public CronField(CronFieldName fieldName, FieldConstraints constraints) {
        this.field = Validate.notNull(fieldName);
        this.parser = new FieldParser(constraints);
    }

    /**
     * Returns field name
     * @return CronFieldName, never null
     */
    public CronFieldName getField() {
        return field;
    }

    /**
     * Parses a String cron expression
     * @param expression - cron expression
     * @return parse result as CronFieldParseResult instance - never null.
     * May throw a RuntimeException if cron expression is bad.
     */
    public CronFieldParseResult parse(String expression) {
        return new CronFieldParseResult(field, parser.parse(expression));
    }

    /**
     * Create a Comparator that compares CronField instances using CronFieldName value.
     * @return Comparator<CronField> instance, never null.
     */
    public static Comparator<CronField> createFieldTypeComparator() {
        return new Comparator<CronField>() {
            @Override
            public int compare(CronField o1, CronField o2) {
                return o1.getField().getOrder() - o2.getField().getOrder();
            }
        };
    }
}
