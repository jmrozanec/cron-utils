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

import java.util.List;

import org.junit.Test;

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.value.IntegerFieldValue;

import static org.junit.Assert.assertTrue;

public class BetweenDayOfWeekValueGeneratorTest {

    @Test
    public void testGenerateCandidatesNotIncludingIntervalExtremes() {
        //cron expression: DoW 1-5,
        //on February 2016: 1-5, 8-12, 15-19, 22-26, 29
        final CronField cronField = new CronField(CronFieldName.DAY_OF_WEEK, new Between(new IntegerFieldValue(1), new IntegerFieldValue(5)),
                FieldConstraintsBuilder.instance().createConstraintsInstance());
        final BetweenDayOfWeekValueGenerator generator = new BetweenDayOfWeekValueGenerator(cronField, 2016, 2, new WeekDay(1, true));
        final List<Integer> values = generator.generateCandidates(1, 29);
        validateInterval(1, 5, values);
        validateInterval(8, 12, values);
        validateInterval(15, 19, values);
        validateInterval(22, 26, values);
        assertTrue(values.contains(29));
    }

    private void validateInterval(final int start, final int end, final List<Integer> values) {
        for (int j = start; j < end + 1; j++) {
            assertTrue(String.format("%s not contained in values", j), values.contains(j));
        }
    }
}
