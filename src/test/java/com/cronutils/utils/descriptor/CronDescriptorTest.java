package com.cronutils.utils.descriptor;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
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
public class CronDescriptorTest {

    private CronDescriptor descriptor;
    private FieldConstraints nullFieldConstraints;
    @Mock
    private CronDefinition mockDefinition;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        descriptor = CronDescriptor.instance(Locale.UK);
        nullFieldConstraints =
                FieldConstraintsBuilder.instance()
                        .addHashSupport()
                        .addLSupport()
                        .addWSupport()
                        .createConstraintsInstance();
    }

    @Test
    public void testDescribeEveryXTimeUnits() throws Exception {
        int time = 3;
        Every expression = new Every(new IntegerFieldValue(time));
        assertEquals(String.format("every %s seconds", time), descriptor.describe(
                        new Cron(mockDefinition, Lists.asList(new CronField(CronFieldName.SECOND, expression, nullFieldConstraints), new CronField[]{}))
                )
        );
        assertEquals(String.format("every %s minutes", time), descriptor.describe(
                        new Cron(mockDefinition, Lists.asList(new CronField(CronFieldName.MINUTE, expression, nullFieldConstraints), new CronField[]{}))
                )
        );
        List<CronField> params = Lists.newArrayList();
        params.add(new CronField(CronFieldName.HOUR, expression, nullFieldConstraints));
        params.add(new CronField(CronFieldName.MINUTE, new On(new IntegerFieldValue(time)), nullFieldConstraints));
        assertEquals(String.format("every %s hours at minute %s", time, time), descriptor.describe(new Cron(mockDefinition, params)));
    }

    @Test
    public void testDescribeEveryXMinutesBetweenTime() throws Exception {
        int hour = 11;
        int start = 0;
        int end = 10;
        Between expression = new Between(new IntegerFieldValue(start), new IntegerFieldValue(end));
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.MINUTE, expression, nullFieldConstraints));
        results.add(new CronField(CronFieldName.HOUR, new On(new IntegerFieldValue(hour)), nullFieldConstraints));
        assertEquals(String.format("every minute between %s:%02d and %s:%02d", hour, start, hour, end), descriptor.describe(new Cron(mockDefinition, results)));
    }

    @Test
    public void testDescribeAtXTimeBetweenDaysOfWeek() throws Exception {
        int hour = 11;
        int minute = 30;
        int start = 2;
        int end = 6;
        Between expression = new Between(new IntegerFieldValue(start), new IntegerFieldValue(end));
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new On(new IntegerFieldValue(hour)), nullFieldConstraints));
        results.add(new CronField(CronFieldName.MINUTE, new On(new IntegerFieldValue(minute)), nullFieldConstraints));
        results.add(new CronField(CronFieldName.DAY_OF_WEEK, expression, nullFieldConstraints));
        assertEquals(String.format("at %s:%s every day between Tuesday and Saturday", hour, minute), descriptor.describe(new Cron(mockDefinition, results)));
    }

    @Test
    public void testDescribeAtXHours() throws Exception {
        int hour = 11;
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new On(new IntegerFieldValue(hour)), nullFieldConstraints));
        results.add(new CronField(CronFieldName.MINUTE, new Always(), nullFieldConstraints));
        results.add(new CronField(CronFieldName.SECOND, new Always(), nullFieldConstraints));
        assertEquals(String.format("at %s:00", hour), descriptor.describe(new Cron(mockDefinition, results)));
    }

    @Test
    public void testEverySecondInMonth() throws Exception {
        int month = 2;
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new Always(), nullFieldConstraints));
        results.add(new CronField(CronFieldName.MINUTE, new Always(), nullFieldConstraints));
        results.add(new CronField(CronFieldName.SECOND, new Always(), nullFieldConstraints));
        results.add(new CronField(CronFieldName.MONTH, new On(new IntegerFieldValue(month)), nullFieldConstraints));
        assertEquals("every second at February month", descriptor.describe(new Cron(mockDefinition, results)));
    }

    @Test
    public void testEveryMinuteBetweenMonths() throws Exception {
        int monthStart = 2;
        int monthEnd = 3;
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new Always(), nullFieldConstraints));
        results.add(new CronField(CronFieldName.MINUTE, new Always(), nullFieldConstraints));
        results.add(new CronField(CronFieldName.MONTH, new Between(new IntegerFieldValue(monthStart), new IntegerFieldValue(monthEnd)), nullFieldConstraints));
        assertEquals("every minute every month between February and March", descriptor.describe(new Cron(mockDefinition, results)));
    }

    @Test
    public void testLastDayOfWeekInMonth() throws Exception {
        int dayOfWeek = 2;
        int hour = 10;
        int minute = 15;
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new On(new IntegerFieldValue(hour)), nullFieldConstraints));
        results.add(new CronField(CronFieldName.MINUTE, new On(new IntegerFieldValue(minute)), nullFieldConstraints));
        results.add(new CronField(CronFieldName.DAY_OF_WEEK, new On(new IntegerFieldValue(dayOfWeek), new SpecialCharFieldValue(SpecialChar.L)), nullFieldConstraints));
        assertEquals(String.format("at %s:%s last Tuesday of every month", hour, minute), descriptor.describe(new Cron(mockDefinition, results)));
    }

    @Test
    public void testNthDayOfWeekInMonth() throws Exception {
        int dayOfWeek = 2;
        int hour = 10;
        int minute = 15;
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new On(new IntegerFieldValue(hour)), nullFieldConstraints));
        results.add(new CronField(CronFieldName.MINUTE, new On(new IntegerFieldValue(minute)), nullFieldConstraints));
        results.add(new CronField(CronFieldName.DAY_OF_WEEK, new On(new IntegerFieldValue(dayOfWeek), new SpecialCharFieldValue(SpecialChar.HASH), new IntegerFieldValue(dayOfWeek)), nullFieldConstraints));
        assertEquals(String.format("at %s:%s Tuesday %s of every month", hour, minute, dayOfWeek), descriptor.describe(new Cron(mockDefinition, results)));
    }

    @Test
    public void testLastDayOfMonth() throws Exception {
        int hour = 10;
        int minute = 15;
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new On(new IntegerFieldValue(hour)), nullFieldConstraints));
        results.add(new CronField(CronFieldName.MINUTE, new On(new IntegerFieldValue(minute)), nullFieldConstraints));
        results.add(new CronField(CronFieldName.DAY_OF_MONTH, new On(new SpecialCharFieldValue(SpecialChar.L)), nullFieldConstraints));
        assertEquals(String.format("at %s:%s last day of month", hour, minute), descriptor.describe(new Cron(mockDefinition, results)));
    }

    @Test
    public void testNearestWeekdayToNthOfMonth() throws Exception {
        int dayOfMonth = 22;
        int hour = 10;
        int minute = 15;
        List<CronField> results = Lists.newArrayList();
        results.add(new CronField(CronFieldName.HOUR, new On(new IntegerFieldValue(hour)), nullFieldConstraints));
        results.add(new CronField(CronFieldName.MINUTE, new On(new IntegerFieldValue(minute)), nullFieldConstraints));
        results.add(new CronField(CronFieldName.DAY_OF_MONTH, new On(new IntegerFieldValue(dayOfMonth), new SpecialCharFieldValue(SpecialChar.W)), nullFieldConstraints));
        assertEquals(String.format("at %s:%s the nearest weekday to the %s of the month", hour, minute, dayOfMonth), descriptor.describe(new Cron(mockDefinition, results)));
    }
}