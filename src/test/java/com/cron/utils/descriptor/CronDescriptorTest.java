package com.cron.utils.descriptor;

import com.cron.utils.CronFieldName;
import com.cron.utils.parser.field.*;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class CronDescriptorTest {

    private CronDescriptor descriptor;
    private FieldConstraints nullFieldConstraints;

    @Before
    public void setUp() throws Exception {
        descriptor = CronDescriptor.instance(Locale.UK);
        nullFieldConstraints = FieldConstraintsBuilder.instance().createConstraintsInstance();
    }

    @Test
    public void testDescribeEveryXTimeUnits() throws Exception {
        int time = 3;
        Every expression = new Every(nullFieldConstraints, "" + time);
        assertEquals(String.format("every %s seconds", time), descriptor.describe(
                        Lists.asList(new CronFieldParseResult(CronFieldName.SECOND, expression), new CronFieldParseResult[]{})
                )
        );
        assertEquals(String.format("every %s minutes", time), descriptor.describe(
                        Lists.asList(new CronFieldParseResult(CronFieldName.MINUTE, expression), new CronFieldParseResult[]{})
                )
        );
        List<CronFieldParseResult> params = Lists.newArrayList();
        params.add(new CronFieldParseResult(CronFieldName.HOUR, expression));
        params.add(new CronFieldParseResult(CronFieldName.MINUTE, new On(nullFieldConstraints, "" + time)));
        assertEquals(String.format("every %s hours at minute %s", time, time), descriptor.describe(params));
    }

    @Test
    public void testDescribeEveryXMinutesBetweenTime() throws Exception {
        int hour = 11;
        int start = 0;
        int end = 10;
        Between expression = new Between(nullFieldConstraints, "" + start, "" + end);
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronFieldName.MINUTE, expression));
        results.add(new CronFieldParseResult(CronFieldName.HOUR, new On(nullFieldConstraints, "" + hour)));
        assertEquals(String.format("every minute between %s:%02d and %s:%02d", hour, start, hour, end), descriptor.describe(results));
    }

    @Test
    public void testDescribeAtXTimeBetweenDaysOfWeek() throws Exception {
        int hour = 11;
        int minute = 30;
        int start = 2;
        int end = 6;
        Between expression = new Between(nullFieldConstraints, "" + start, "" + end);
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronFieldName.HOUR, new On(nullFieldConstraints, "" + hour)));
        results.add(new CronFieldParseResult(CronFieldName.MINUTE, new On(nullFieldConstraints, "" + minute)));
        results.add(new CronFieldParseResult(CronFieldName.DAY_OF_WEEK, expression));
        assertEquals(String.format("at %s:%s every day between Tuesday and Saturday", hour, minute), descriptor.describe(results));
    }

    @Test
    public void testDescribeAtXHours() throws Exception {
        int hour = 11;
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronFieldName.HOUR, new On(nullFieldConstraints, "" + hour)));
        results.add(new CronFieldParseResult(CronFieldName.MINUTE, new Always(nullFieldConstraints)));
        results.add(new CronFieldParseResult(CronFieldName.SECOND, new Always(nullFieldConstraints)));
        assertEquals(String.format("at %s:00", hour), descriptor.describe(results));
    }

    @Test
    public void testEverySecondInMonth() throws Exception {
        int month = 2;
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronFieldName.HOUR, new Always(nullFieldConstraints)));
        results.add(new CronFieldParseResult(CronFieldName.MINUTE, new Always(nullFieldConstraints)));
        results.add(new CronFieldParseResult(CronFieldName.SECOND, new Always(nullFieldConstraints)));
        results.add(new CronFieldParseResult(CronFieldName.MONTH, new On(nullFieldConstraints, "" + month)));
        assertEquals("every second at February month", descriptor.describe(results));
    }

    @Test
    public void testEveryMinuteBetweenMonths() throws Exception {
        int monthStart = 2;
        int monthEnd = 3;
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronFieldName.HOUR, new Always(nullFieldConstraints)));
        results.add(new CronFieldParseResult(CronFieldName.MINUTE, new Always(nullFieldConstraints)));
        results.add(new CronFieldParseResult(CronFieldName.MONTH, new Between(nullFieldConstraints, "" + monthStart, "" + monthEnd)));
        assertEquals("every minute every month between February and March", descriptor.describe(results));
    }

    @Test
    public void testLastDayOfWeekInMonth() throws Exception {
        int dayOfWeek = 2;
        int hour = 10;
        int minute = 15;
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronFieldName.HOUR, new On(nullFieldConstraints, "" + hour)));
        results.add(new CronFieldParseResult(CronFieldName.MINUTE, new On(nullFieldConstraints, "" + minute)));
        results.add(new CronFieldParseResult(CronFieldName.DAY_OF_WEEK, new On(nullFieldConstraints, String.format("%sL", dayOfWeek))));
        assertEquals(String.format("at %s:%s last Tuesday of every month", hour, minute), descriptor.describe(results));
    }

    @Test
    public void testNthDayOfWeekInMonth() throws Exception {
        int dayOfWeek = 2;
        int hour = 10;
        int minute = 15;
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronFieldName.HOUR, new On(nullFieldConstraints, "" + hour)));
        results.add(new CronFieldParseResult(CronFieldName.MINUTE, new On(nullFieldConstraints, "" + minute)));
        results.add(new CronFieldParseResult(CronFieldName.DAY_OF_WEEK, new On(nullFieldConstraints, String.format("%s#%s", dayOfWeek, dayOfWeek))));
        assertEquals(String.format("at %s:%s Tuesday %s of every month", hour, minute, dayOfWeek), descriptor.describe(results));
    }

    @Test
    public void testLastDayOfMonth() throws Exception {
        int hour = 10;
        int minute = 15;
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronFieldName.HOUR, new On(nullFieldConstraints, "" + hour)));
        results.add(new CronFieldParseResult(CronFieldName.MINUTE, new On(nullFieldConstraints, "" + minute)));
        results.add(new CronFieldParseResult(CronFieldName.DAY_OF_MONTH, new On(nullFieldConstraints, "L")));
        assertEquals(String.format("at %s:%s last day of the month", hour, minute), descriptor.describe(results));
    }

    @Test
    public void testNearestWeekdayToNthOfMonth() throws Exception {
        int dayOfMonth = 22;
        int hour = 10;
        int minute = 15;
        List<CronFieldParseResult> results = Lists.newArrayList();
        results.add(new CronFieldParseResult(CronFieldName.HOUR, new On(nullFieldConstraints, "" + hour)));
        results.add(new CronFieldParseResult(CronFieldName.MINUTE, new On(nullFieldConstraints, "" + minute)));
        results.add(new CronFieldParseResult(CronFieldName.DAY_OF_MONTH, new On(nullFieldConstraints, String.format("%sW", dayOfMonth))));
        assertEquals(String.format("at %s:%s the nearest weekday to the %s of the month", hour, minute, dayOfMonth), descriptor.describe(results));
    }
}