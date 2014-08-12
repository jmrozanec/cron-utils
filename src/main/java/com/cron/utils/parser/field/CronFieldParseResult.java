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
public class CronFieldParseResult {
    private CronParameter field;
    private CronFieldExpression expression;

    public CronFieldParseResult(CronParameter field, CronFieldExpression expression) {
        this.field = field;
        this.expression = expression;
    }

    public CronParameter getField() {
        return field;
    }

    public CronFieldExpression getExpression() {
        return expression;
    }

    public static Comparator<CronFieldParseResult> createFieldComparator() {
        return new Comparator<CronFieldParseResult>() {
            @Override
            public int compare(CronFieldParseResult o1, CronFieldParseResult o2) {
                return o1.getField().getOrder() - o2.getField().getOrder();
            }
        };
    }
}
